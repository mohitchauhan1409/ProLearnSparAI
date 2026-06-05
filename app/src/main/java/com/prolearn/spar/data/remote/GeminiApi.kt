package com.prolearn.spar.data.remote

import com.prolearn.spar.BuildConfig
import com.prolearn.spar.data.remote.dto.GeminiRequest
import com.prolearn.spar.data.remote.dto.GeminiResponse
import com.prolearn.spar.data.remote.dto.SystemInstruction
import com.prolearn.spar.data.remote.dto.Content
import com.prolearn.spar.data.remote.dto.InlineData
import com.prolearn.spar.data.remote.dto.Part
import com.prolearn.spar.data.remote.dto.GenerationConfig
import com.prolearn.spar.domain.model.Message
import com.prolearn.spar.domain.model.SessionAnalysis
import com.prolearn.spar.domain.model.ConceptScore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.double
import android.util.Base64
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GeminiApi"

@Singleton
class GeminiApi @Inject constructor(
    private val client: HttpClient
) {
    companion object {
        private const val GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"
    }

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun sendMessage(
        messages: List<Message>,
        systemPrompt: String
    ): Result<String> = runCatching {
        Log.d(TAG, "sendMessage() — ${messages.size} messages")
        val modelMessages = messages.filterNot { it.role == "queued" }
        sendContents(
            contents = modelMessages.map { msg ->
                Content(
                    role = if (msg.role == "ai") "model" else "user",
                    parts = listOf(Part(text = msg.text))
                )
            },
            systemPrompt = systemPrompt
        )
    }

    suspend fun sendAttachmentMessage(
        history: List<Message>,
        systemPrompt: String,
        prompt: String,
        mimeType: String,
        bytes: ByteArray
    ): Result<String> = runCatching {
        Log.d(TAG, "sendAttachmentMessage() mime=$mimeType bytes=${bytes.size}")
        val contents = history.filterNot { it.role == "queued" }.map { msg ->
            Content(
                role = if (msg.role == "ai") "model" else "user",
                parts = listOf(Part(text = msg.text))
            )
        } + Content(
            role = "user",
            parts = listOf(
                Part(text = prompt),
                Part(
                    inlineData = InlineData(
                        mimeType = mimeType,
                        data = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    )
                )
            )
        )
        sendContents(contents = contents, systemPrompt = systemPrompt, maxOutputTokens = 260)
    }

    private suspend fun sendContents(
        contents: List<Content>,
        systemPrompt: String,
        maxOutputTokens: Int = 200
    ): String {
        val response = client.post("$GEMINI_URL?key=${BuildConfig.GEMINI_API_KEY}") {
            contentType(ContentType.Application.Json)
            setBody(GeminiRequest(
                systemInstruction = SystemInstruction(listOf(Part(text = systemPrompt))),
                contents = contents,
                generationConfig = GenerationConfig(maxOutputTokens = maxOutputTokens)
            ))
        }
        val statusCode = response.status.value
        val bodyText = response.bodyAsText()
        Log.d(TAG, "sendMessage() HTTP $statusCode — body(200)=${bodyText.take(300)}")

        if (statusCode != 200) {
            Log.e(TAG, "Gemini error $statusCode: $bodyText")
            throw IllegalStateException("Gemini HTTP $statusCode: $bodyText")
        }

        val body = json.parseToJsonElement(bodyText)
        // Check for API-level error in response body
        body.jsonObject["error"]?.let { errObj ->
            val errMsg = errObj.jsonObject["message"]?.jsonPrimitive?.content ?: "Unknown error"
            Log.e(TAG, "Gemini API error in body: $errMsg")
            throw IllegalStateException("Gemini error: $errMsg")
        }

        val text = body.jsonObject["candidates"]?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.get("content")?.jsonObject
            ?.get("parts")?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.get("text")?.jsonPrimitive?.content ?: ""

        // Log finish reason to detect safety blocks or truncation
        val finishReason = body.jsonObject["candidates"]?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.get("finishReason")?.jsonPrimitive?.content
        Log.i(TAG, "sendMessage() finishReason=$finishReason text='${text.take(100)}'")

        if (text.isBlank()) {
            Log.w(TAG, "Empty text from Gemini — finishReason=$finishReason fullBody=$bodyText")
            throw IllegalStateException("Gemini returned empty response (finishReason=$finishReason)")
        }

        return if (text.contains("[SESSION_COMPLETE]")) "[SESSION_COMPLETE]" else text.trim()
    }

    suspend fun analyzeSession(transcript: List<Message>): Result<SessionAnalysis> = runCatching {
        val transcriptText = transcript.joinToString("\n") { "${it.role}: ${it.text}" }
        val prompt = com.prolearn.spar.constants.buildAnalysisPrompt(transcriptText)
        val response = client.post("$GEMINI_URL?key=${BuildConfig.GEMINI_API_KEY}") {
            contentType(ContentType.Application.Json)
            setBody(GeminiRequest(
                systemInstruction = SystemInstruction(listOf(Part(text =
                    "You are an expert tutor analyzing student performance. Return only valid JSON with no other text."
                ))),
                contents = listOf(Content(role = "user", parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(temperature = 0.3f, maxOutputTokens = 500)
            ))
        }
        val bodyText = response.bodyAsText()
        val body = json.parseToJsonElement(bodyText)
        val resultText = body.jsonObject["candidates"]?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.get("content")?.jsonObject
            ?.get("parts")?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.get("text")?.jsonPrimitive?.content ?: "{}"
        val cleanJson = resultText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val analysisJson = json.parseToJsonElement(cleanJson).jsonObject
        val scores = analysisJson["conceptScores"]?.jsonArray?.map { elem ->
            val obj = elem.jsonObject
            ConceptScore(
                name = obj["name"]?.jsonPrimitive?.content ?: "",
                score = obj["score"]?.jsonPrimitive?.int ?: 0
            )
        } ?: emptyList()
        SessionAnalysis(
            conceptScores = scores,
            aiInsight = analysisJson["aiInsight"]?.jsonPrimitive?.content ?: "",
            overallScore = analysisJson["overallScore"]?.jsonPrimitive?.int ?: 0
        )
    }

    suspend fun getHint(question: String): Result<String> = runCatching {
        val prompt = com.prolearn.spar.constants.buildHintPrompt(question)
        val response = client.post("$GEMINI_URL?key=${BuildConfig.GEMINI_API_KEY}") {
            contentType(ContentType.Application.Json)
            setBody(GeminiRequest(
                systemInstruction = SystemInstruction(listOf(Part(text =
                    "You give Socratic hints. Never reveal answers."
                ))),
                contents = listOf(Content(role = "user", parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(temperature = 0.5f, maxOutputTokens = 100)
            ))
        }
        val bodyText = response.bodyAsText()
        val body = json.parseToJsonElement(bodyText)
        body.jsonObject["candidates"]?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.get("content")?.jsonObject
            ?.get("parts")?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.get("text")?.jsonPrimitive?.content?.trim() ?: ""
    }
}
