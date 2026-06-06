package com.prolearn.spar.service

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AudioPlaybackSvc"
private const val VOICE_TAG = "voiceImprovement"

@Singleton
class AudioPlaybackService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null
    private var levelJob: Job? = null
    private var currentAudioFile: File? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun playAudio(
        bytes: ByteArray,
        onLevelUpdate: (Float) -> Unit,
        onStart: (durationMs: Int) -> Unit = {},
        onError: (Throwable) -> Unit = {},
        onComplete: () -> Unit
    ) {
        Log.d(TAG, "playAudio() — ${bytes.size} bytes")
        val startedAt = System.currentTimeMillis()
        Log.i(VOICE_TAG, "playback_prepare_start bytes=${bytes.size}")
        stop()

        val file = try {
            File.createTempFile("tts_", ".mp3", context.cacheDir).also {
                it.writeBytes(bytes)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write audio file", e)
            Log.e(VOICE_TAG, "playback_file_write_failed bytes=${bytes.size} error=${e.message}", e)
            onError(e)
            return
        }
        currentAudioFile = file
        Log.d(VOICE_TAG, "playback_temp_file_ready path=${file.name} bytes=${file.length()}")

        try {
            mediaPlayer = MediaPlayer().apply {
                setOnCompletionListener {
                    Log.d(TAG, "MediaPlayer onCompletion")
                    Log.i(VOICE_TAG, "playback_complete elapsedMs=${System.currentTimeMillis() - startedAt}")
                    levelJob?.cancel()
                    deleteCurrentAudioFile()
                    releasePlayer()
                    scope.launch { onComplete() }
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what extra=$extra")
                    Log.e(
                        VOICE_TAG,
                        "playback_media_error what=$what extra=$extra elapsedMs=${System.currentTimeMillis() - startedAt}"
                    )
                    levelJob?.cancel()
                    deleteCurrentAudioFile()
                    releasePlayer()
                    scope.launch { onError(IllegalStateException("MediaPlayer error what=$what extra=$extra")) }
                    true
                }
                setOnPreparedListener { player ->
                    player.start()
                    Log.d(TAG, "MediaPlayer started, duration=${player.duration}ms")
                    Log.i(
                        VOICE_TAG,
                        "playback_started durationMs=${player.duration} prepareMs=${System.currentTimeMillis() - startedAt}"
                    )
                    onStart(player.duration)
                    startLevelUpdates(onLevelUpdate)
                }
                setDataSource(file.absolutePath)
                prepareAsync()
                Log.d(VOICE_TAG, "playback_prepare_async_called")
            }
        } catch (e: Exception) {
            Log.e(TAG, "MediaPlayer setup failed", e)
            Log.e(VOICE_TAG, "playback_setup_failed error=${e.message}", e)
            deleteCurrentAudioFile()
            releasePlayer()
            onError(e)
        }
    }

    private fun startLevelUpdates(onLevelUpdate: (Float) -> Unit) {
        levelJob?.cancel()
        levelJob = scope.launch(Dispatchers.IO) {
            while (mediaPlayer?.isPlaying == true) {
                val level = (Math.random() * 0.7 + 0.3).toFloat()
                scope.launch { onLevelUpdate(level) }
                delay(60)
            }
            Log.d(TAG, "Level simulation loop ended")
        }
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true

    fun stop() {
        Log.d(TAG, "stop() called")
        Log.d(VOICE_TAG, "playback_stop_requested mediaPlaying=${mediaPlayer?.isPlaying == true}")
        levelJob?.cancel()
        levelJob = null
        releasePlayer()
        deleteCurrentAudioFile()
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

    private fun deleteCurrentAudioFile() {
        currentAudioFile?.delete()
        currentAudioFile = null
    }

}
