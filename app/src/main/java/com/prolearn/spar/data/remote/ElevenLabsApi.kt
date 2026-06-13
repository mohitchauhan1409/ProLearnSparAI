package com.prolearn.spar.data.remote

import android.util.Log
import com.prolearn.spar.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.readBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ElevenLabsApi"
private const val VOICE_TAG = "voiceImprovement"

object VoiceIds {
    const val ARIA  = "EXAVITQu4vr4xnSDxMaL"
    const val ROHAN = "TX3LPaxmHKxFdv7VOQHJ"
}

// Proper serializable DTOs — avoids "collections of different element types" crash
@Serializable
data class ElevenLabsRequest(
    val text: String,
    @SerialName("model_id") val modelId: String = "eleven_flash_v2_5",
    @SerialName("voice_settings") val voiceSettings: VoiceSettings = VoiceSettings(),
    @SerialName("language_code") val languageCode: String? = null,
    @SerialName("previous_text") val previousText: String? = null,
    @SerialName("next_text") val nextText: String? = null,
    @SerialName("apply_text_normalization") val applyTextNormalization: String = "auto"
)

@Serializable
data class VoiceSettings(
    val stability: Float = 0.42f,
    @SerialName("similarity_boost") val similarityBoost: Float = 0.82f,
    val style: Float = 0.16f,
    @SerialName("use_speaker_boost") val useSpeakerBoost: Boolean = false,
    // ElevenLabs playback speed (0.7 slow … 1.2 fast). Null = provider default.
    val speed: Float? = null
)

@Singleton
class ElevenLabsApi @Inject constructor(
    private val client: HttpClient
) {
    suspend fun synthesize(
        text: String,
        voiceId: String,
        onAudioReady: (ByteArray) -> Unit
    ): Result<Unit> = synthesizeBytes(text, voiceId).map { bytes ->
        onAudioReady(bytes)
    }

    suspend fun synthesizeBytes(
        text: String,
        voiceId: String,
        previousText: String? = null,
        nextText: String? = null,
        languageCode: String? = null,
        speed: Float? = null
    ): Result<ByteArray> = runCatching {
        Log.d(TAG, "synthesizeBytes() voiceId=$voiceId text='${text.take(60)}'")
        val startedAt = System.currentTimeMillis()
        Log.i(
            VOICE_TAG,
            "tts_request_start voiceId=$voiceId model=eleven_flash_v2_5 chars=${text.length} " +
                "hasPrevious=${!previousText.isNullOrBlank()} hasNext=${!nextText.isNullOrBlank()} " +
                "languageCode=${languageCode ?: "auto"}"
        )
        val response = client.post(
            "https://api.elevenlabs.io/v1/text-to-speech/$voiceId/stream"
        ) {
            header("xi-api-key", BuildConfig.ELEVENLABS_API_KEY)
            parameter("output_format", "mp3_44100_64")
            contentType(ContentType.Application.Json)
            setBody(
                ElevenLabsRequest(
                    text = text,
                    voiceSettings = VoiceSettings(speed = speed),
                    previousText = previousText?.takeLast(480),
                    nextText = nextText?.take(480),
                    languageCode = languageCode
                )
            )
        }

        val statusCode = response.status.value
        Log.d(TAG, "synthesizeBytes() HTTP $statusCode")
        Log.i(VOICE_TAG, "tts_response_headers status=$statusCode elapsedMs=${System.currentTimeMillis() - startedAt}")

        if (statusCode != 200) {
            val errorBody = response.bodyAsText()
            Log.e(TAG, "ElevenLabs error $statusCode: $errorBody")
            Log.e(
                VOICE_TAG,
                "tts_request_failed status=$statusCode elapsedMs=${System.currentTimeMillis() - startedAt} " +
                    "body=${errorBody.take(500)}"
            )
            throw IllegalStateException("ElevenLabs HTTP $statusCode: $errorBody")
        }

        val bytes = response.readBytes()
        Log.d(TAG, "synthesizeBytes() got ${bytes.size} bytes of audio")
        Log.i(
            VOICE_TAG,
            "tts_audio_ready bytes=${bytes.size} elapsedMs=${System.currentTimeMillis() - startedAt}"
        )
        bytes
    }
}
