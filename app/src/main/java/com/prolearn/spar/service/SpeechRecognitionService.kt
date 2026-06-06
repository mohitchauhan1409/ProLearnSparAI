package com.prolearn.spar.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.prolearn.spar.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

private const val TAG = "SpeechRecognitionSvc"
private const val VOICE_TAG = "voiceImprovement"
private const val SAMPLE_RATE = 16000
private const val CHANNEL_CFG = AudioFormat.CHANNEL_IN_MONO
private const val AUDIO_FMT   = AudioFormat.ENCODING_PCM_16BIT

/**
 * Dual-mode STT:
 *
 * Mode A (primary)   — Android SpeechRecognizer → Google STT cloud
 *   Works on emulator and device. Tap to start, auto-stops on silence.
 *   Stopping early via stopListening() waits for onResults/onError naturally.
 *
 * Mode B (fallback)  — AudioRecord → ElevenLabs Scribe v1
 *   Used when SpeechRecognizer unavailable (AOSP builds etc.)
 *   Tap to start, tap again to stop → WAV sent to ElevenLabs STT.
 */
@Singleton
class SpeechRecognitionService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: HttpClient
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.IO)
    private val json  = Json { ignoreUnknownKeys = true }

    // ── Mode A state ──────────────────────────────────────────────────────────
    private var androidRecognizer: SpeechRecognizer? = null
    private var androidRecognizerActive = false
    private var userStoppedEarly = false

    // ── Mode B state ──────────────────────────────────────────────────────────
    private val keepRecording = AtomicBoolean(false)
    private var recordingJob: Job? = null

    private val useAndroidRecognizer: Boolean
        // Some devices report SpeechRecognizer.isRecognitionAvailable=true, then fail with
        // "bind to recognition service failed" and never call RecognitionListener. Keep the
        // reliable AudioRecord + ElevenLabs path as primary for live sparring.
        get() = false

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    fun startListening(
        onPartialResult: (String) -> Unit,
        onFinalResult: (String) -> Unit,
        onAudioLevel: (Float) -> Unit,
        onError: (String) -> Unit
    ) {
        if (useAndroidRecognizer) {
            Log.i(TAG, "startListening() → Mode A (Android SpeechRecognizer)")
            Log.i(VOICE_TAG, "stt_start mode=android_realtime recognitionAvailable=true")
            startModeA(onPartialResult, onFinalResult, onAudioLevel, onError)
        } else {
            Log.i(TAG, "startListening() → Mode B (AudioRecord + ElevenLabs STT)")
            Log.i(
                VOICE_TAG,
                "stt_start mode=elevenlabs_batch recognitionAvailable=${SpeechRecognizer.isRecognitionAvailable(context)} reason=android_bind_unreliable"
            )
            startModeB(onPartialResult, onFinalResult, onAudioLevel, onError)
        }
    }

    fun stopListening() {
        Log.d(TAG, "stopListening() androidActive=$androidRecognizerActive " +
                "audioRecordActive=${keepRecording.get()}")
        Log.i(
            VOICE_TAG,
            "stt_stop_requested androidActive=$androidRecognizerActive audioRecordActive=${keepRecording.get()}"
        )
        when {
            androidRecognizerActive -> {
                userStoppedEarly = true
                mainHandler.post { androidRecognizer?.stopListening() }
            }
            keepRecording.get() -> keepRecording.set(false)
        }
    }

    fun destroy() {
        Log.d(TAG, "destroy()")
        destroyModeA()
        keepRecording.set(false)
        recordingJob?.cancel()
        recordingJob = null
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mode A — Android SpeechRecognizer
    // ─────────────────────────────────────────────────────────────────────────

    private fun startModeA(
        onPartialResult: (String) -> Unit,
        onFinalResult: (String) -> Unit,
        onAudioLevel: (Float) -> Unit,
        onError: (String) -> Unit
    ) {
        mainHandler.post {
            destroyModeA()
            userStoppedEarly = false
            // 250ms delay after destroy prevents ERROR_CLIENT (code 11) on rapid retaps
            mainHandler.postDelayed({
                createAndStartRecognizer(onPartialResult, onFinalResult, onAudioLevel, onError)
            }, 250)
        }
    }

    private fun createAndStartRecognizer(
        onPartialResult: (String) -> Unit,
        onFinalResult: (String) -> Unit,
        onAudioLevel: (Float) -> Unit,
        onError: (String) -> Unit
    ) {
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        androidRecognizer = recognizer
        androidRecognizerActive = false

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2500L)
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                androidRecognizerActive = true
                Log.i(TAG, "A: onReadyForSpeech ✓ — mic open, speak now")
                Log.i(VOICE_TAG, "stt_android_ready_for_speech")
            }

            override fun onBeginningOfSpeech() {
                Log.i(TAG, "A: onBeginningOfSpeech ✓ — voice detected")
                Log.i(VOICE_TAG, "stt_android_beginning_of_speech")
            }

            override fun onRmsChanged(rmsdB: Float) {
                val level = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                onAudioLevel(level)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                Log.i(TAG, "A: onEndOfSpeech — waiting for result")
                androidRecognizerActive = false
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}

            override fun onPartialResults(partialResults: Bundle) {
                val partial = partialResults
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull { it.isNotBlank() } ?: return
                Log.d(TAG, "A: partial='$partial'")
                Log.d(VOICE_TAG, "stt_android_partial chars=${partial.length}")
                onPartialResult(partial)
            }

            override fun onResults(results: Bundle) {
                androidRecognizerActive = false
                val result = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull { it.isNotBlank() }
                Log.i(TAG, "A: onResults='$result' userStoppedEarly=$userStoppedEarly")
                Log.i(VOICE_TAG, "stt_android_final chars=${result?.length ?: 0} userStoppedEarly=$userStoppedEarly")
                if (!result.isNullOrBlank()) {
                    onFinalResult(result)
                } else {
                    onError("Didn't catch that. Tap mic to try again.")
                }
            }

            override fun onError(error: Int) {
                androidRecognizerActive = false
                Log.e(TAG, "A: onError code=$error userStoppedEarly=$userStoppedEarly")
                Log.e(VOICE_TAG, "stt_android_error code=$error userStoppedEarly=$userStoppedEarly")

                // If user tapped stop early with no speech — silent cancel, not an error
                if (userStoppedEarly && error == SpeechRecognizer.ERROR_NO_MATCH) {
                    Log.d(TAG, "A: user-stopped NO_MATCH — silent cancel")
                    onError("") // empty = silent cancel, ViewModel handles this quietly
                    return
                }

                val msg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH ->
                        "Didn't catch that. Tap mic to try again."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                        "No speech detected. Tap mic when ready."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                        destroyModeA() // Clear busy state
                        "Tap mic to try again."
                    }
                    SpeechRecognizer.ERROR_CLIENT -> {
                        // Code 11 — recognizer not ready yet. Happens if tapped too fast.
                        Log.w(TAG, "A: ERROR_CLIENT — will be ready shortly")
                        "Tap mic to try again."
                    }
                    SpeechRecognizer.ERROR_NETWORK ->
                        "No internet for speech recognition."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                        "Microphone permission needed."
                    else -> "Speech error ($error). Tap mic to retry."
                }
                Log.e(TAG, "A: reporting error: '$msg'")
                onError(msg)
            }
        })

        Log.d(TAG, "A: calling startListening()")
        recognizer.startListening(intent)
    }

    private fun destroyModeA() {
        androidRecognizer?.apply {
            try { cancel() } catch (_: Exception) {}
            try { destroy() } catch (_: Exception) {}
        }
        androidRecognizer = null
        androidRecognizerActive = false
        Log.d(TAG, "A: recognizer destroyed")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mode B — AudioRecord + ElevenLabs STT
    // ─────────────────────────────────────────────────────────────────────────

    @SuppressLint("MissingPermission")
    private fun startModeB(
        onPartialResult: (String) -> Unit,
        onFinalResult: (String) -> Unit,
        onAudioLevel: (Float) -> Unit,
        onError: (String) -> Unit
    ) {
        if (keepRecording.get()) {
            keepRecording.set(false)
            recordingJob?.cancel()
        }

        val minBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CFG, AUDIO_FMT)
            .coerceAtLeast(4096)

        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CFG, AUDIO_FMT, minBuf * 4
        )

        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "B: AudioRecord init failed")
            recorder.release()
            onError("Microphone unavailable. Check permission.")
            return
        }

        keepRecording.set(true)
        val pcmOut = ByteArrayOutputStream()
        val recordingStartedAt = System.currentTimeMillis()

        recordingJob = scope.launch {
            recorder.startRecording()
            Log.i(TAG, "B: ● Recording started")
            Log.i(VOICE_TAG, "stt_elevenlabs_recording_started sampleRate=$SAMPLE_RATE buffer=$minBuf")

            val buf = ShortArray(minBuf / 2)
            var totalSamples = 0L

            try {
                while (isActive && keepRecording.get()) {
                    val read = recorder.read(buf, 0, buf.size)
                    if (read <= 0) continue
                    totalSamples += read

                    val bytes = ByteArray(read * 2)
                    for (i in 0 until read) {
                        bytes[i * 2]     = (buf[i].toInt() and 0xFF).toByte()
                        bytes[i * 2 + 1] = (buf[i].toInt() shr 8 and 0xFF).toByte()
                    }
                    pcmOut.write(bytes)

                    val rms   = sqrt(buf.take(read).sumOf { it.toLong() * it }.toDouble() / read)
                    val level = (rms / 32768.0).toFloat().coerceIn(0f, 1f)
                    withContext(Dispatchers.Main) {
                        onAudioLevel(level)
                        onPartialResult("…")
                    }
                }
            } finally {
                try { recorder.stop() } catch (_: Exception) {}
                recorder.release()
                Log.d(TAG, "B: stopped totalSamples=$totalSamples")
            }

            val pcmBytes = pcmOut.toByteArray()
            Log.i(
                VOICE_TAG,
                "stt_elevenlabs_recording_stopped pcmBytes=${pcmBytes.size} durationMs=${System.currentTimeMillis() - recordingStartedAt}"
            )
            if (pcmBytes.size < SAMPLE_RATE) {
                withContext(Dispatchers.Main) {
                    onError("Too short. Tap mic and speak your full answer.")
                }
                return@launch
            }

            withContext(Dispatchers.Main) { onPartialResult("Transcribing...") }
            transcribeWithElevenLabs(pcmToWav(pcmBytes), onFinalResult, onError)
        }
    }

    private suspend fun transcribeWithElevenLabs(
        wavBytes: ByteArray,
        onFinalResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        Log.d(TAG, "B: transcribeWithElevenLabs() ${wavBytes.size} bytes")
        val startedAt = System.currentTimeMillis()
        Log.i(VOICE_TAG, "stt_elevenlabs_upload_start wavBytes=${wavBytes.size} model=scribe_v1")
        runCatching {
            val response = httpClient.post("https://api.elevenlabs.io/v1/speech-to-text") {
                header("xi-api-key", BuildConfig.ELEVENLABS_API_KEY)
                setBody(MultiPartFormDataContent(formData {
                    append("model_id", "scribe_v1")
                    append("language_code", "en")
                    append(
                        key = "file",
                        value = wavBytes,
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, "audio/wav")
                            append(HttpHeaders.ContentDisposition, "filename=\"audio.wav\"")
                        }
                    )
                }))
            }
            val status = response.status.value
            val body   = response.bodyAsText()
            Log.d(TAG, "B: STT HTTP $status body=${body.take(200)}")
            Log.i(VOICE_TAG, "stt_elevenlabs_response status=$status elapsedMs=${System.currentTimeMillis() - startedAt}")
            if (status != 200) throw IllegalStateException("STT HTTP $status: $body")

            val text = json.parseToJsonElement(body)
                .jsonObject["text"]?.jsonPrimitive?.content?.trim() ?: ""
            Log.i(TAG, "B: transcript='$text'")
            Log.i(VOICE_TAG, "stt_elevenlabs_final chars=${text.length} elapsedMs=${System.currentTimeMillis() - startedAt}")

            // Filter non-speech audio events like "(static sound)", "(beeping)"
            if (text.matches(Regex("^\\(.*\\)$"))) "" else text
        }.onSuccess { transcript ->
            withContext(Dispatchers.Main) {
                if (transcript.isNotBlank()) onFinalResult(transcript)
                else onError("Didn't catch that. Tap mic to try again.")
            }
        }.onFailure { e ->
            Log.e(TAG, "B: STT failed: ${e.message}")
            Log.e(VOICE_TAG, "stt_elevenlabs_failed error=${e.message} elapsedMs=${System.currentTimeMillis() - startedAt}", e)
            withContext(Dispatchers.Main) {
                onError("Transcription failed. Tap mic to retry.")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PCM → WAV header
    // ─────────────────────────────────────────────────────────────────────────

    private fun pcmToWav(pcm: ByteArray, sampleRate: Int = SAMPLE_RATE, channels: Int = 1): ByteArray {
        val bitsPerSample = 16
        val byteRate      = sampleRate * channels * bitsPerSample / 8
        val blockAlign    = channels * bitsPerSample / 8
        val out = ByteArrayOutputStream(44 + pcm.size)
        val d   = DataOutputStream(out)
        fun i16(v: Int) { d.write(v and 0xFF); d.write(v shr 8 and 0xFF) }
        fun i32(v: Int) { i16(v and 0xFFFF); i16(v shr 16 and 0xFFFF) }
        d.writeBytes("RIFF"); i32(36 + pcm.size)
        d.writeBytes("WAVE"); d.writeBytes("fmt "); i32(16)
        i16(1); i16(channels); i32(sampleRate); i32(byteRate)
        i16(blockAlign); i16(bitsPerSample)
        d.writeBytes("data"); i32(pcm.size); d.write(pcm); d.flush()
        return out.toByteArray()
    }
}
