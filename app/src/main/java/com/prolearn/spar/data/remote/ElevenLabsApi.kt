package com.prolearn.spar.data.remote

import android.util.Log
import com.prolearn.spar.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.header
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

object VoiceIds {
    const val ARIA  = "EXAVITQu4vr4xnSDxMaL"
    const val ROHAN = "TX3LPaxmHKxFdv7VOQHJ"
}

// Proper serializable DTOs — avoids "collections of different element types" crash
@Serializable
data class ElevenLabsRequest(
    val text: String,
    @SerialName("model_id") val modelId: String = "eleven_turbo_v2",
    @SerialName("voice_settings") val voiceSettings: VoiceSettings = VoiceSettings()
)

@Serializable
data class VoiceSettings(
    val stability: Float = 0.5f,
    @SerialName("similarity_boost") val similarityBoost: Float = 0.75f,
    val style: Float = 0.0f,
    @SerialName("use_speaker_boost") val useSpeakerBoost: Boolean = true
)

@Singleton
class ElevenLabsApi @Inject constructor(
    private val client: HttpClient
) {
    suspend fun synthesize(
        text: String,
        voiceId: String,
        onAudioReady: (ByteArray) -> Unit
    ): Result<Unit> = runCatching {
        Log.d(TAG, "synthesize() voiceId=$voiceId text='${text.take(60)}'")

        val response = client.post(
            "https://api.elevenlabs.io/v1/text-to-speech/$voiceId"
        ) {
            header("xi-api-key", BuildConfig.ELEVENLABS_API_KEY)
            contentType(ContentType.Application.Json)
            setBody(ElevenLabsRequest(text = text))
        }

        val statusCode = response.status.value
        Log.d(TAG, "synthesize() HTTP $statusCode")

        if (statusCode != 200) {
            val errorBody = response.bodyAsText()
            Log.e(TAG, "ElevenLabs error $statusCode: $errorBody")
            throw IllegalStateException("ElevenLabs HTTP $statusCode: $errorBody")
        }

        val bytes = response.readBytes()
        Log.d(TAG, "synthesize() got ${bytes.size} bytes of audio")
        onAudioReady(bytes)
    }
}
