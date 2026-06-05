package com.prolearn.spar.data.remote

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

private const val YOUTUBE_TAG = "YoutubeTranscriptApi"
private const val SOCIALKIT_TRANSCRIPT_URL = "https://api.socialkit.dev/youtube/transcript"
private const val SOCIALKIT_ACCESS_KEY = "wq1ydliXRjWHsj"

@Singleton
class YoutubeTranscriptApi @Inject constructor(
    private val client: HttpClient
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchTranscript(url: String): Result<YoutubeTranscript> = runCatching {
        val cleanUrl = url.trim()
        require(cleanUrl.isNotBlank()) { "YouTube URL is blank." }

        Log.d(YOUTUBE_TAG, "fetchTranscript() via SocialKit url=$cleanUrl")
        val bodyText = client.get(SOCIALKIT_TRANSCRIPT_URL) {
            parameter("access_key", SOCIALKIT_ACCESS_KEY)
            parameter("url", cleanUrl)
            parameter("cache", "true")
            parameter("cache_ttl", "3600")
        }.bodyAsText()

        val root = json.parseToJsonElement(bodyText).jsonObject
        val success = root["success"]?.jsonPrimitive?.contentOrNull.equals("true", ignoreCase = true)
        if (!success) {
            val message = root["message"]?.jsonPrimitive?.contentOrNull
                ?: root["error"]?.jsonPrimitive?.contentOrNull
                ?: "Transcript API failed."
            throw IllegalStateException(message)
        }

        val data = root["data"]?.jsonObject
            ?: throw IllegalStateException("Transcript API returned no data.")
        val transcript = data["transcript"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
        if (transcript.isBlank()) {
            throw IllegalStateException("Transcript API returned an empty transcript.")
        }

        val segments = data["transcriptSegments"]?.jsonArray
            ?.take(40)
            ?.mapNotNull { segment ->
                val obj = segment as? JsonObject ?: return@mapNotNull null
                val timestamp = obj["timestamp"]?.jsonPrimitive?.contentOrNull.orEmpty()
                val text = obj["text"]?.jsonPrimitive?.contentOrNull.orEmpty()
                if (text.isBlank()) null else "[$timestamp] $text"
            }
            ?.joinToString("\n")
            .orEmpty()

        YoutubeTranscript(
            videoId = data["url"]?.jsonPrimitive?.contentOrNull ?: cleanUrl,
            language = "auto",
            text = buildString {
                append(transcript)
                if (segments.isNotBlank()) {
                    append("\n\nTimestamped segments:\n")
                    append(segments)
                }
            }
        )
    }
}

data class YoutubeTranscript(
    val videoId: String,
    val language: String,
    val text: String
)
