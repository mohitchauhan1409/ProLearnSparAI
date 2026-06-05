package com.prolearn.spar.ui.screens.spar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prolearn.spar.constants.buildSparSystemPrompt
import com.prolearn.spar.data.remote.ElevenLabsApi
import com.prolearn.spar.data.remote.VoiceIds
import com.prolearn.spar.data.repository.SparRepository
import com.prolearn.spar.domain.model.Message
import com.prolearn.spar.domain.model.SparConfig
import com.prolearn.spar.domain.model.SparState
import com.prolearn.spar.service.AudioPlaybackService
import com.prolearn.spar.service.HapticsManager
import com.prolearn.spar.service.SpeechRecognitionService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "LiveSparVM"

@HiltViewModel
class LiveSparViewModel @Inject constructor(
    private val sparRepository: SparRepository,
    private val elevenLabsApi: ElevenLabsApi,
    private val audioPlayer: AudioPlaybackService,
    private val speechService: SpeechRecognitionService,
    private val haptics: HapticsManager
) : ViewModel() {

    // ─── Public state ─────────────────────────────────────────────────────────
    private val _state = MutableStateFlow<SparState>(SparState.Idle)
    val state: StateFlow<SparState> = _state.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private val _timer = MutableStateFlow(0)
    val timerFormatted: StateFlow<String> = _timer.map { s ->
        "%02d:%02d".format(s / 60, s % 60)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "00:00")

    // Whether SpeechRecognizer.startListening() has been called and we're waiting for a result
    private val _isRecognizerActive = MutableStateFlow(false)
    val isRecognizerActive: StateFlow<Boolean> = _isRecognizerActive.asStateFlow()

    // Live partial transcript while user is speaking
    private val _partialTranscript = MutableStateFlow("")
    val partialTranscript: StateFlow<String> = _partialTranscript.asStateFlow()

    // Status message shown below mic button
    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    // ─── Private vars ─────────────────────────────────────────────────────────
    private var questionCount = 0
    private var hintsUsed = 0
    private var totalAnswers = 0
    private var sessionConfig: SparConfig? = null
    private var isTimerRunning = false
    private var systemPrompt = ""
    private var sessionStarted = false

    // ─── Session Lifecycle ────────────────────────────────────────────────────

    fun startSession(config: SparConfig) {
        if (sessionStarted) {
            Log.w(TAG, "startSession() called again — ignoring (already started)")
            return
        }
        sessionStarted = true
        sessionConfig = config
        systemPrompt = buildSparSystemPrompt(config)
        Log.i(TAG, "startSession() subject=${config.subject} chapter=${config.chapter} " +
                "difficulty=${config.difficulty} questions=${config.questionCount}")
        haptics.sessionStart()
        startTimer()
        askFirstQuestion()
    }

    private fun askFirstQuestion() {
        viewModelScope.launch {
            Log.d(TAG, "askFirstQuestion() — calling Gemini")
            setStatus("Starting session...")
            _state.value = SparState.AiThinking

            val startMsg = listOf(
                Message(
                    text = "Begin the spar session. Ask your first question.",
                    role = "user"
                )
            )
            sparRepository.sendMessage(startMsg, systemPrompt)
                .onSuccess { response ->
                    Log.i(TAG, "Gemini response: '${response.take(100)}'")
                    when {
                        response == "[SESSION_COMPLETE]" -> completeSession()
                        response.isBlank() -> {
                            Log.e(TAG, "Gemini returned blank — treating as error")
                            _state.value = SparState.Error("Got empty response. Tap to retry.")
                        }
                        else -> {
                            addAiMessage(response)
                            speakText(response)
                        }
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "askFirstQuestion() failed: ${e.message}")
                    setStatus("Connection failed.")
                    _state.value = SparState.Error("Failed to connect. Tap to retry.")
                }
        }
    }

    // ─── Mic control ──────────────────────────────────────────────────────────

    /**
     * Called when user taps mic to START listening.
     * Guard: only allowed when AI is not busy and recognizer is not already running.
     */
    fun startListening() {
        val currentState = _state.value
        val recognizerActive = _isRecognizerActive.value
        Log.d(TAG, "startListening() currentState=$currentState recognizerActive=$recognizerActive")

        if (currentState is SparState.AiThinking ||
            currentState is SparState.AiSpeaking ||
            currentState is SparState.AiEvaluating) {
            Log.w(TAG, "startListening() blocked — AI busy (state=$currentState)")
            return
        }
        if (recognizerActive) {
            Log.w(TAG, "startListening() blocked — recognizer already active")
            return
        }

        _partialTranscript.value = ""
        _audioLevel.value = 0f
        _isRecognizerActive.value = true
        _state.value = SparState.StudentListening
        setStatus("Listening — speak your answer")
        Log.i(TAG, "→ StudentListening, launching SpeechRecognizer")

        // Safety timeout — if nothing happens within 5s (e.g. bind failure),
        // reset recognizer state so user can tap mic again
        viewModelScope.launch {
            kotlinx.coroutines.delay(5000)
            if (_isRecognizerActive.value &&
                _state.value == SparState.StudentListening) {
                Log.w(TAG, "Safety timeout — no speech activity after 5s, resetting")
                _isRecognizerActive.value = false
                setStatus("Tap mic to answer")
            }
        }

        speechService.startListening(
            onPartialResult = { partial ->
                // "…" is the live recording heartbeat — only log occasionally to avoid spam
                if (partial != "…") {
                    Log.d(TAG, "Partial: '$partial'")
                }
                // "Transcribing..." is post-stop phase
                if (partial == "Transcribing...") {
                    _partialTranscript.value = ""
                    setStatus("Transcribing your answer...")
                } else {
                    _partialTranscript.value = partial
                    _state.value = SparState.StudentSpeaking
                }
            },
            onFinalResult = { result ->
                Log.i(TAG, "Final transcript: '$result'")
                _partialTranscript.value = ""
                _isRecognizerActive.value = false
                if (result.isBlank()) {
                    Log.w(TAG, "Blank final result — prompting retry")
                    _state.value = SparState.StudentListening
                    setStatus("Didn't catch that. Tap mic to try again.")
                } else {
                    _state.value = SparState.AiEvaluating
                    setStatus("Evaluating your answer...")
                    submitUserAnswer(result)
                }
            },
            onAudioLevel = { level ->
                _audioLevel.value = level
                if (_state.value == SparState.StudentListening) {
                    _state.value = SparState.StudentSpeaking
                }
            },
            onError = { errorMsg ->
                Log.e(TAG, "SpeechRecognizer error: '$errorMsg'")
                _partialTranscript.value = ""
                _audioLevel.value = 0f
                _isRecognizerActive.value = false
                _state.value = SparState.StudentListening
                // Empty string = silent cancel (stopped with no speech) — no error UI
                if (errorMsg.isNotBlank()) {
                    setStatus(errorMsg)
                    haptics.error()
                } else {
                    setStatus("Tap mic to answer")
                }
            }
        )
    }

    /**
     * Called when user taps mic to STOP early.
     * For SpeechRecognizer (Mode A): stops and waits for onResults.
     * For AudioRecord (Mode B): stops recording and triggers STT.
     */
    fun stopListening() {
        Log.d(TAG, "stopListening() — recognizerActive=${_isRecognizerActive.value}")
        if (!_isRecognizerActive.value) {
            Log.w(TAG, "stopListening() ignored — recognizer not active")
            return
        }
        // Don't clear _isRecognizerActive yet — we're waiting for STT to come back
        // It will be cleared in onFinalResult / onError callbacks
        setStatus("Processing...")
        speechService.stopListening()
    }

    fun retryConnection() {
        Log.d(TAG, "retryConnection()")
        val lastAiMessage = _messages.value.lastOrNull { it.role == "ai" }
        if (lastAiMessage == null) {
            Log.d(TAG, "retryConnection() — no messages, restarting session")
            sessionStarted = false
            sessionConfig?.let { startSession(it) }
            return
        }
        viewModelScope.launch {
            setStatus("Reconnecting...")
            _state.value = SparState.AiThinking
            sparRepository.sendMessage(_messages.value, systemPrompt)
                .onSuccess { response ->
                    Log.i(TAG, "Retry OK: '${response.take(80)}'")
                    if (response.isNotBlank()) {
                        addAiMessage(response)
                        speakText(response)
                    } else {
                        _state.value = SparState.Error("Still getting empty response. Check API key.")
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "Retry failed: ${e.message}")
                    _state.value = SparState.Error("Still can't connect. Check internet.")
                }
        }
    }

    fun requestHint() {
        if (hintsUsed >= 2) {
            Log.w(TAG, "requestHint() — no hints left")
            return
        }
        val lastAiMsg = _messages.value.lastOrNull { it.role == "ai" } ?: return
        Log.i(TAG, "requestHint() hintsUsed=$hintsUsed q='${lastAiMsg.text.take(60)}'")
        hintsUsed++
        haptics.hintSpend()
        setStatus("Getting hint...")

        viewModelScope.launch {
            sparRepository.getHint(lastAiMsg.text)
                .onSuccess { hint ->
                    Log.d(TAG, "Hint: '$hint'")
                    addAiMessage(hint, isHint = true)
                    setStatus("")
                }
                .onFailure { e ->
                    Log.e(TAG, "getHint failed: ${e.message}")
                    hintsUsed-- // refund
                    setStatus("Hint failed. Try again.")
                }
        }
    }

    // ─── Answer Processing ────────────────────────────────────────────────────

    private fun submitUserAnswer(transcript: String) {
        if (transcript.isBlank()) {
            Log.w(TAG, "submitUserAnswer() blank — prompting retry")
            _state.value = SparState.StudentListening
            setStatus("Didn't catch that. Tap mic to try again.")
            return
        }

        totalAnswers++
        addUserMessage(transcript)
        Log.i(TAG, "submitUserAnswer() #$totalAnswers: '${transcript.take(80)}'")

        viewModelScope.launch {
            _state.value = SparState.AiEvaluating
            setStatus("Evaluating...")
            sparRepository.sendMessage(_messages.value, systemPrompt)
                .onSuccess { response ->
                    Log.i(TAG, "Eval response: '${response.take(100)}'")
                    when {
                        response == "[SESSION_COMPLETE]" -> completeSession()
                        response.isBlank() -> {
                            Log.e(TAG, "Blank eval response")
                            _state.value = SparState.Error("Empty response. Tap to retry.")
                        }
                        else -> {
                            questionCount++
                            addAiMessage(response)
                            speakText(response)
                        }
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "submitUserAnswer Gemini failed: ${e.message}")
                    _state.value = SparState.Error("Connection error. Tap to retry.")
                    haptics.error()
                }
        }
    }

    // ─── TTS ──────────────────────────────────────────────────────────────────

    private suspend fun speakText(text: String) {
        val voiceId = sessionConfig?.voiceId ?: VoiceIds.ARIA
        Log.i(TAG, "speakText() voiceId=$voiceId '${text.take(60)}'")
        _state.value = SparState.AiSpeaking(text)
        setStatus("AI speaking...")
        _audioLevel.value = 0f

        elevenLabsApi.synthesize(text, voiceId) { bytes ->
            Log.d(TAG, "ElevenLabs audio ready: ${bytes.size} bytes")
            audioPlayer.playAudio(
                bytes,
                onLevelUpdate = { _audioLevel.value = it },
                onComplete = {
                    Log.d(TAG, "Playback done → StudentListening")
                    _audioLevel.value = 0f
                    _state.value = SparState.StudentListening
                    setStatus("Your turn — tap mic to answer")
                    haptics.tapLight()
                }
            )
        }.onFailure { e ->
            Log.e(TAG, "ElevenLabs failed (${e.message}), using device TTS")
            setStatus("Using device voice...")
            audioPlayer.fallbackSpeak(text) {
                Log.d(TAG, "Fallback TTS done → StudentListening")
                _audioLevel.value = 0f
                _state.value = SparState.StudentListening
                setStatus("Your turn — tap mic to answer")
                haptics.tapLight()
            }
        }
    }

    // ─── Session Complete ─────────────────────────────────────────────────────

    private fun completeSession() {
        Log.i(TAG, "completeSession() q=$questionCount answers=$totalAnswers hints=$hintsUsed")
        isTimerRunning = false
        _isRecognizerActive.value = false
        speechService.destroy()
        audioPlayer.stop()
        haptics.sessionComplete()
        setStatus("Session complete!")

        viewModelScope.launch {
            val config = sessionConfig ?: run {
                Log.e(TAG, "completeSession() sessionConfig null!")
                _state.value = SparState.SessionComplete
                return@launch
            }
            val estimatedScore = if (totalAnswers > 0)
                ((totalAnswers - hintsUsed).coerceAtLeast(0) * 100 / totalAnswers).coerceIn(0, 100)
            else 0

            sparRepository.analyzeSession(_messages.value)
                .onSuccess { analysis ->
                    Log.i(TAG, "Analysis: score=${analysis.overallScore}")
                    sparRepository.completeSession(
                        subject = config.subject, chapter = config.chapter,
                        difficulty = config.difficulty, examTarget = config.examTarget,
                        questionCount = questionCount, score = analysis.overallScore,
                        durationSeconds = _timer.value, conceptScores = analysis.conceptScores,
                        aiInsight = analysis.aiInsight, hintsUsed = hintsUsed,
                        independentAnswers = (totalAnswers - hintsUsed).coerceAtLeast(0),
                        messages = _messages.value
                    )
                }
                .onFailure { e ->
                    Log.e(TAG, "analyzeSession failed: ${e.message}, using estimated=$estimatedScore")
                    sparRepository.completeSession(
                        subject = config.subject, chapter = config.chapter,
                        difficulty = config.difficulty, examTarget = config.examTarget,
                        questionCount = questionCount, score = estimatedScore,
                        durationSeconds = _timer.value, conceptScores = emptyList(),
                        aiInsight = "Great effort! Keep practicing to strengthen your fundamentals.",
                        hintsUsed = hintsUsed,
                        independentAnswers = (totalAnswers - hintsUsed).coerceAtLeast(0),
                        messages = _messages.value
                    )
                }
            _state.value = SparState.SessionComplete
        }
    }

    // ─── Timer ────────────────────────────────────────────────────────────────

    private fun startTimer() {
        isTimerRunning = true
        Log.d(TAG, "startTimer()")
        viewModelScope.launch {
            while (isTimerRunning) {
                delay(1000)
                _timer.value++
                val maxSec = sessionConfig?.durationSeconds ?: 600
                if (_timer.value >= maxSec) {
                    Log.i(TAG, "Timer expired at ${_timer.value}s")
                    completeSession()
                    break
                }
            }
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun addAiMessage(text: String, isHint: Boolean = false) {
        _messages.value = _messages.value + Message(text, "ai", isHint)
        Log.d(TAG, "addAiMessage() total=${_messages.value.size} isHint=$isHint")
    }

    private fun addUserMessage(text: String) {
        _messages.value = _messages.value + Message(text, "user")
        Log.d(TAG, "addUserMessage() total=${_messages.value.size}")
    }

    private fun setStatus(msg: String) {
        _statusMessage.value = msg
        if (msg.isNotBlank()) Log.d(TAG, "Status: '$msg'")
    }

    // ─── Computed Properties ──────────────────────────────────────────────────

    val questionCounter: String
        get() = "Q ${questionCount + 1}"

    val hintsRemaining: Int
        get() = (2 - hintsUsed).coerceAtLeast(0)

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared()")
        isTimerRunning = false
        _isRecognizerActive.value = false
        speechService.destroy()
        audioPlayer.stop()
    }
}
