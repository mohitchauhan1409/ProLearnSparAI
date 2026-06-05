package com.prolearn.spar.service

import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AudioPlaybackSvc"

@Singleton
class AudioPlaybackService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null
    private var tts: TextToSpeech? = null
    private var levelJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun playAudio(
        bytes: ByteArray,
        onLevelUpdate: (Float) -> Unit,
        onComplete: () -> Unit
    ) {
        Log.d(TAG, "playAudio() — ${bytes.size} bytes")
        stop()

        val file = try {
            File.createTempFile("tts_", ".mp3", context.cacheDir).also {
                it.writeBytes(bytes)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write audio file", e)
            onComplete()
            return
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                setOnCompletionListener {
                    Log.d(TAG, "MediaPlayer onCompletion")
                    levelJob?.cancel()
                    file.delete()
                    releasePlayer()
                    // Always call back on Main thread
                    scope.launch { onComplete() }
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what extra=$extra")
                    levelJob?.cancel()
                    file.delete()
                    releasePlayer()
                    scope.launch { onComplete() }
                    true
                }
                start()
                Log.d(TAG, "MediaPlayer started, duration=${duration}ms")
            }

            // Simulate waveform levels on IO, but dispatch update on Main
            levelJob = scope.launch(Dispatchers.IO) {
                while (mediaPlayer?.isPlaying == true) {
                    val level = (Math.random() * 0.7 + 0.3).toFloat()
                    scope.launch { onLevelUpdate(level) }
                    delay(60)
                }
                Log.d(TAG, "Level simulation loop ended")
            }
        } catch (e: Exception) {
            Log.e(TAG, "MediaPlayer setup failed", e)
            file.delete()
            releasePlayer()
            onComplete()
        }
    }

    fun fallbackSpeak(
        text: String,
        languageTag: String = "en-IN",
        onComplete: () -> Unit
    ) {
        Log.d(TAG, "fallbackSpeak() — using Android TTS language=$languageTag")
        releaseTts()
        var ttsReady = false
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true
                tts?.language = Locale.forLanguageTag(languageTag)
                tts?.setSpeechRate(0.9f)
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "fallback_tts")
                Log.d(TAG, "TTS speaking: '${text.take(40)}...'")
            } else {
                Log.e(TAG, "TTS init failed with status=$status")
            }
        }
        // Estimate duration and complete — TTS doesn't give us a clean completion callback easily
        scope.launch(Dispatchers.IO) {
            val estimatedMs = (text.split(" ").size * 350L).coerceAtLeast(1500L)
            Log.d(TAG, "TTS estimated duration: ${estimatedMs}ms")
            delay(estimatedMs)
            releaseTts()
            scope.launch { onComplete() }
        }
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true

    fun stop() {
        Log.d(TAG, "stop() called")
        levelJob?.cancel()
        levelJob = null
        releasePlayer()
        releaseTts()
    }

    private fun releasePlayer() {
        mediaPlayer?.apply {
            try {
                if (isPlaying) stop()
                reset()
                release()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing MediaPlayer", e)
            }
        }
        mediaPlayer = null
    }

    private fun releaseTts() {
        tts?.apply {
            stop()
            shutdown()
        }
        tts = null
    }
}
