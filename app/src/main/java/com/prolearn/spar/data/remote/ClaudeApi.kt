package com.prolearn.spar.data.remote

import android.util.Log
import com.prolearn.spar.BuildConfig
import com.prolearn.spar.constants.buildVideoLessonSystemPrompt
import com.prolearn.spar.constants.buildVideoLessonUserPrompt
import com.prolearn.spar.constants.videoLessonSchema
import com.prolearn.spar.data.remote.dto.ClaudeFormat
import com.prolearn.spar.data.remote.dto.ClaudeMessage
import com.prolearn.spar.data.remote.dto.ClaudeOutputConfig
import com.prolearn.spar.data.remote.dto.ClaudeRequest
import com.prolearn.spar.data.remote.dto.ClaudeResponse
import com.prolearn.spar.domain.model.SceneVisual
import com.prolearn.spar.domain.model.VideoLesson
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ClaudeApi"

/**
 * Client for Anthropic's Claude models. Used to script AI explainer-video lessons.
 *
 * Uses raw HTTP via the shared Ktor client (consistent with [GeminiApi] /
 * [ElevenLabsApi]) rather than the heavyweight JVM SDK. The lesson is requested
 * with `output_config.format` (strict JSON schema) so the response is guaranteed
 * to be parseable JSON — no fragile text extraction.
 */
@Singleton
class ClaudeApi @Inject constructor(
    private val client: HttpClient
) {
    private companion object {
        const val MESSAGES_URL = "https://api.anthropic.com/v1/messages"
        const val ANTHROPIC_VERSION = "2023-06-01"
        const val MODEL = "claude-opus-4-8"
        const val MAX_TOKENS = 12000
    }

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /** Generate a fully scripted [VideoLesson] for [topic]. */
    suspend fun generateLesson(
        topic: String,
        teacherName: String = "Aria"
    ): Result<VideoLesson> = runCatching {
        Log.d(TAG, "generateLesson() topic='${topic.take(80)}'")
        require(BuildConfig.ANTHROPIC_API_KEY.isNotBlank()) {
            "ANTHROPIC_API_KEY is missing from local.properties"
        }

        val response = client.post(MESSAGES_URL) {
            header("x-api-key", BuildConfig.ANTHROPIC_API_KEY)
            header("anthropic-version", ANTHROPIC_VERSION)
            contentType(ContentType.Application.Json)
            setBody(
                ClaudeRequest(
                    model = MODEL,
                    maxTokens = MAX_TOKENS,
                    system = buildVideoLessonSystemPrompt(),
                    messages = listOf(
                        ClaudeMessage(
                            role = "user",
                            content = buildVideoLessonUserPrompt(topic, teacherName)
                        )
                    ),
                    outputConfig = ClaudeOutputConfig(
                        format = ClaudeFormat(type = "json_schema", schema = videoLessonSchema())
                    )
                )
            )
        }

        val status = response.status.value
        val bodyText = response.bodyAsText()
        Log.d(TAG, "generateLesson() HTTP $status body(200)=${bodyText.take(200)}")

        if (status != 200) {
            Log.e(TAG, "Claude error $status: $bodyText")
            throw IllegalStateException("Claude HTTP $status: ${bodyText.take(300)}")
        }

        val parsed = json.decodeFromString<ClaudeResponse>(bodyText)

        if (parsed.stopReason == "refusal") {
            val reason = parsed.stopDetails?.explanation ?: parsed.stopDetails?.category ?: "safety"
            throw IllegalStateException("Claude declined this topic ($reason). Try a different topic.")
        }

        // With output_config.format the first text block holds the JSON lesson.
        val lessonJson = parsed.content
            ?.firstOrNull { it.type == "text" && !it.text.isNullOrBlank() }
            ?.text
            ?: throw IllegalStateException("Claude returned no lesson content")

        val lesson = json.decodeFromString<VideoLesson>(lessonJson)
        sanitize(lesson, topic, teacherName)
    }

    /** Answer a student's live doubt about the lesson, in a warm teacher voice. */
    suspend fun answerDoubt(
        teacherName: String,
        lessonTitle: String,
        topic: String,
        currentlyLearning: String,
        question: String
    ): Result<String> = runCatching {
        require(BuildConfig.ANTHROPIC_API_KEY.isNotBlank()) { "ANTHROPIC_API_KEY is missing" }
        val system = """
You are $teacherName, a warm, patient teacher helping a complete beginner during a
lesson titled "$lessonTitle" (topic: $topic). The student just paused to ask a doubt.
Answer ONLY their question, simply and kindly, like a real teacher talking out loud.
Assume no prior knowledge, use plain everyday words, give a tiny example or analogy if
it helps, and keep it short — 2 to 4 sentences. No markdown, no lists, no emoji.
        """.trimIndent()
        val user = """
Right now I'm learning: "$currentlyLearning"

My question: $question
        """.trimIndent()

        val response = client.post(MESSAGES_URL) {
            header("x-api-key", BuildConfig.ANTHROPIC_API_KEY)
            header("anthropic-version", ANTHROPIC_VERSION)
            contentType(ContentType.Application.Json)
            setBody(
                ClaudeRequest(
                    model = MODEL,
                    maxTokens = 500,
                    system = system,
                    messages = listOf(ClaudeMessage(role = "user", content = user))
                )
            )
        }
        val bodyText = response.bodyAsText()
        if (response.status.value != 200) {
            throw IllegalStateException("Claude HTTP ${response.status.value}: ${bodyText.take(200)}")
        }
        val parsed = json.decodeFromString<ClaudeResponse>(bodyText)
        parsed.content
            ?.firstOrNull { it.type == "text" && !it.text.isNullOrBlank() }
            ?.text
            ?.trim()
            ?: throw IllegalStateException("No answer returned")
    }

    /** Guard against an empty or malformed lesson before we hand it to playback. */
    private fun sanitize(lesson: VideoLesson, topic: String, fallbackTeacher: String): VideoLesson {
        val scenes = lesson.scenes
            .map { scene ->
                val lines = scene.lines
                    .map { it.copy(say = it.say.trim(), write = it.write.trim()) }
                    .filter { it.say.isNotEmpty() }
                scene.copy(
                    visual = SceneVisual.normalize(scene.visual),
                    keyTerm = scene.keyTerm?.takeIf { it.isNotBlank() },
                    lines = lines,
                    diagram = scene.diagram?.takeIf { it.isPresent }
                )
            }
            .filter { it.narration.isNotBlank() }
        check(scenes.isNotEmpty()) { "Claude returned an empty lesson" }
        return lesson.copy(
            title = lesson.title.ifBlank { topic.take(48) },
            teacherName = lesson.teacherName.ifBlank { fallbackTeacher },
            scenes = scenes
        )
    }
}
