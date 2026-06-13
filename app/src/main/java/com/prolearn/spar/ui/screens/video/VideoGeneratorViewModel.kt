package com.prolearn.spar.ui.screens.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prolearn.spar.data.repository.VideoLessonRepository
import com.prolearn.spar.domain.model.PlayableScene
import com.prolearn.spar.domain.model.VideoLesson
import com.prolearn.spar.service.AudioPlaybackService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class GenPhase { IDLE, SCRIPTING, VOICING, READY, ERROR }

data class VideoUiState(
    val phase: GenPhase = GenPhase.IDLE,
    val topic: String = "",
    val lesson: VideoLesson? = null,
    val scenes: List<PlayableScene> = emptyList(),
    val voiceDone: Int = 0,
    val voiceTotal: Int = 0,
    val error: String? = null,
    // playback
    val currentScene: Int = 0,
    val isPlaying: Boolean = false,
    val audioLevel: Float = 0f,
    val revealedLines: Int = 0,
    /** Per-line write duration (ms) for the current scene — paces the chalk writing to speech. */
    val lineDurations: List<Int> = emptyList(),
    val sceneDurationMs: Int = 0,
    val finished: Boolean = false
) {
    val sceneCount: Int get() = scenes.size
}

@HiltViewModel
class VideoGeneratorViewModel @Inject constructor(
    private val repository: VideoLessonRepository,
    private val audio: AudioPlaybackService
) : ViewModel() {

    private val _uiState = MutableStateFlow(VideoUiState())
    val uiState: StateFlow<VideoUiState> = _uiState.asStateFlow()

    private var revealJob: Job? = null

    fun onTopicChange(value: String) {
        _uiState.update { it.copy(topic = value) }
    }

    fun generate(topic: String = _uiState.value.topic) {
        val cleaned = topic.trim()
        if (cleaned.length < 3 || _uiState.value.phase == GenPhase.SCRIPTING ||
            _uiState.value.phase == GenPhase.VOICING
        ) return

        stopPlayback()
        _uiState.update {
            it.copy(
                phase = GenPhase.SCRIPTING,
                topic = cleaned,
                lesson = null,
                scenes = emptyList(),
                voiceDone = 0,
                voiceTotal = 0,
                error = null,
                currentScene = 0,
                revealedLines = 0,
                lineDurations = emptyList(),
                finished = false
            )
        }

        viewModelScope.launch {
            repository.generate(
                topic = cleaned,
                onScriptReady = { lesson ->
                    _uiState.update {
                        it.copy(
                            phase = GenPhase.VOICING,
                            lesson = lesson,
                            voiceTotal = lesson.scenes.size
                        )
                    }
                },
                onVoiceProgress = { done, total ->
                    _uiState.update { it.copy(voiceDone = done, voiceTotal = total) }
                }
            ).onSuccess { (lesson, scenes) ->
                _uiState.update {
                    it.copy(phase = GenPhase.READY, lesson = lesson, scenes = scenes)
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(phase = GenPhase.ERROR, error = e.message ?: "Something went wrong")
                }
            }
        }
    }

    // ─── Playback ─────────────────────────────────────────────────────────────

    fun togglePlay() {
        val s = _uiState.value
        if (s.scenes.isEmpty()) return
        when {
            s.finished -> replay()
            s.isPlaying -> pause()
            audio.currentPositionMs() > 0 -> resume()
            else -> playScene(s.currentScene)
        }
    }

    fun start() {
        if (_uiState.value.scenes.isNotEmpty()) playScene(0)
    }

    fun next() {
        val s = _uiState.value
        if (s.currentScene + 1 < s.scenes.size) playScene(s.currentScene + 1)
    }

    fun previous() {
        val s = _uiState.value
        playScene((s.currentScene - 1).coerceAtLeast(0))
    }

    fun seekToScene(index: Int) {
        if (index in _uiState.value.scenes.indices) playScene(index)
    }

    fun replay() = playScene(0)

    private fun playScene(index: Int) {
        val scenes = _uiState.value.scenes
        val scene = scenes.getOrNull(index) ?: return
        revealJob?.cancel()
        _uiState.update {
            it.copy(
                currentScene = index,
                isPlaying = true,
                revealedLines = 0,
                lineDurations = emptyList(),
                finished = false,
                audioLevel = 0f
            )
        }
        audio.playAudio(
            bytes = scene.audio,
            onLevelUpdate = { level -> _uiState.update { it.copy(audioLevel = level) } },
            onStart = { durationMs ->
                _uiState.update { it.copy(sceneDurationMs = durationMs) }
                startLineReveal(index, durationMs, 0)
            },
            onError = { _uiState.update { it.copy(audioLevel = 0f) } },
            onComplete = { onSceneComplete(index) }
        )
    }

    private fun onSceneComplete(index: Int) {
        val scenes = _uiState.value.scenes
        if (index + 1 < scenes.size) {
            playScene(index + 1)
        } else {
            revealJob?.cancel()
            _uiState.update { it.copy(isPlaying = false, finished = true, audioLevel = 0f) }
        }
    }

    private fun pause() {
        audio.pause()
        revealJob?.cancel()
        _uiState.update { it.copy(isPlaying = false, audioLevel = 0f) }
    }

    private fun resume() {
        audio.resume()
        _uiState.update { it.copy(isPlaying = true) }
        val s = _uiState.value
        startLineReveal(s.currentScene, s.sceneDurationMs, audio.currentPositionMs())
    }

    /**
     * Reveals each explanation line at the moment its sentence is spoken, by splitting
     * the scene's audio duration across lines in proportion to how long each sentence is.
     * Also publishes each line's allotted duration so the chalk writing is paced to speech.
     */
    private fun startLineReveal(index: Int, durationMs: Int, startMs: Int) {
        revealJob?.cancel()
        val lines = _uiState.value.scenes.getOrNull(index)?.scene?.lines ?: emptyList()
        val n = lines.size
        if (n == 0 || durationMs <= 0) {
            _uiState.update { it.copy(revealedLines = n, lineDurations = emptyList()) }
            return
        }

        val weights = lines.map { it.say.length.coerceAtLeast(1) }
        val totalW = weights.sum()
        val durs = weights.map { (durationMs.toLong() * it / totalW).toInt().coerceAtLeast(250) }
        val starts = IntArray(n)
        var acc = 0
        for (i in 0 until n) { starts[i] = acc; acc += durs[i] }

        val alreadyShown = (0 until n).count { starts[it] <= startMs }
        _uiState.update { it.copy(lineDurations = durs, revealedLines = alreadyShown) }

        revealJob = viewModelScope.launch {
            var sim = startMs
            for (k in alreadyShown until n) {
                val wait = (starts[k] - sim).toLong()
                if (wait > 0) delay(wait)
                sim = starts[k]
                _uiState.update { it.copy(revealedLines = k + 1) }
            }
        }
    }

    private fun stopPlayback() {
        revealJob?.cancel()
        audio.stop()
    }

    /** Discard the current lesson and return to the topic screen. */
    fun reset() {
        stopPlayback()
        _uiState.value = VideoUiState()
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
    }
}
