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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.ArrayDeque
import javax.inject.Inject

private const val TAG = "LiveSparVM"

private data class PendingStudyMaterial(
    val kind: String,
    val name: String,
    val mimeType: String = "",
    val bytes: ByteArray? = null,
    val transcript: String? = null,
    val sourceUrl: String? = null
)

private sealed class QueuedStudentInput(
    val id: Long,
    val displayText: String
) {
    class Typed(id: Long, val text: String) : QueuedStudentInput(id, text)
    class Image(id: Long, val name: String, val mimeType: String, val bytes: ByteArray) :
        QueuedStudentInput(id, "Shared image: $name")
    class Document(id: Long, val name: String, val mimeType: String, val bytes: ByteArray) :
        QueuedStudentInput(id, "Shared document: $name")
    class Youtube(id: Long, val url: String) : QueuedStudentInput(id, "Shared YouTube video: $url")
}

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
    private var completionStarted = false
    private var pendingStudyMaterial: PendingStudyMaterial? = null
    private val queuedInputs = ArrayDeque<QueuedStudentInput>()
    private var tutorOutputPreparing = false
    private var streamingMessageJob: Job? = null
    private var streamingMessageId: Long? = null

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
                "sessionType=${config.sessionType} difficulty=${config.difficulty} questions=${config.questionCount}")
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
                    text = "Open the live session now. Greet ${sessionConfig?.studentName?.ifBlank { "the student" } ?: "the student"} by name, then do the required conversational onboarding for this ${sessionConfig?.sessionType ?: "Learning"} session. Do not teach content yet. Never say hello everyone.",
                    role = "user"
                )
            )
            sparRepository.sendMessage(startMsg, systemPrompt)
                .onSuccess { response ->
                    Log.i(TAG, "Gemini response: '${response.take(100)}'")
                    when {
                        response == "[SESSION_COMPLETE]" -> {
                            _state.value = SparState.StudentListening
                            setStatus("Session can continue, or tap End when you're done.")
                        }
                        response.isBlank() -> {
                            Log.e(TAG, "Gemini returned blank — treating as error")
                            _state.value = SparState.Error("Got empty response. Tap to retry.")
                        }
                        else -> {
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

    fun submitTypedMessage(text: String) {
        val clean = text.trim()
        if (clean.isBlank()) return
        if (queueIfTutorSpeaking { id -> QueuedStudentInput.Typed(id, clean) }) return
        submitUserAnswer(clean)
    }

    fun submitImage(name: String, mimeType: String, bytes: ByteArray) {
        if (queueIfTutorSpeaking { id -> QueuedStudentInput.Image(id, name, mimeType, bytes) }) return
        submitImageNow(name, mimeType, bytes)
    }

    private fun submitImageNow(name: String, mimeType: String, bytes: ByteArray) {
        val prompt = """
            The student shared an image named "$name".
            Study the image carefully. Explain what you see, connect it to ${sessionConfig?.subject ?: "the session topic"}, and help the student learn from it.
            If it contains a question, solve it step by step. If it contains notes, summarize and teach the key concept.
        """.trimIndent()
        submitAttachment(
            visibleMessage = "Shared image: $name",
            prompt = prompt,
            mimeType = mimeType.ifBlank { "image/jpeg" },
            bytes = bytes
        )
    }

    fun submitDocument(name: String, mimeType: String, bytes: ByteArray) {
        if (queueIfTutorSpeaking { id -> QueuedStudentInput.Document(id, name, mimeType, bytes) }) return
        submitDocumentNow(name, mimeType, bytes)
    }

    private fun submitDocumentNow(name: String, mimeType: String, bytes: ByteArray) {
        val prompt = """
            The student shared a study document named "$name".
            Read the document and teach the most important parts for this ${sessionConfig?.sessionType ?: "Learning"} session.
            Start with a short summary, then explain the first useful concept, then ask one tiny check question.
        """.trimIndent()
        submitAttachment(
            visibleMessage = "Shared document: $name",
            prompt = prompt,
            mimeType = mimeType.ifBlank { "application/pdf" },
            bytes = bytes
        )
    }

    fun submitYoutubeLink(url: String) {
        val clean = url.trim()
        if (clean.isBlank()) return
        if (queueIfTutorSpeaking { id -> QueuedStudentInput.Youtube(id, clean) }) return
        submitYoutubeLinkNow(clean)
    }

    private fun submitYoutubeLinkNow(clean: String) {
        val history = _messages.value
        addUserMessage("Shared YouTube video: $clean")
        viewModelScope.launch {
            _state.value = SparState.AiEvaluating
            setStatus("Fetching video transcript...")
            sparRepository.fetchYoutubeTranscript(clean)
                .onSuccess { transcript ->
                    val material = PendingStudyMaterial(
                        kind = "YouTube",
                        name = clean,
                        transcript = transcript.text,
                        sourceUrl = clean
                    )
                    if (!shouldAnswerFromMaterialNow(history, material.kind)) {
                        pendingStudyMaterial = material
                        askMaterialFocus(material)
                        return@onSuccess
                    }
                    val prompt = """
                        The student wants to study from this YouTube video: $clean
                        I fetched the video transcript. Use it to teach, not just summarize.
                        First give the core idea, then explain the most important learning points, then ask one tiny check question.

                        Transcript language: ${transcript.language}
                        Transcript:
                        ${transcript.text.take(12000)}
                    """.trimIndent()
                    sendHiddenTutorPrompt(
                        history = history,
                        prompt = prompt,
                        status = "Studying video..."
                    )
                }
                .onFailure { e ->
                    Log.e(TAG, "YouTube transcript failed: ${e.message}")
                    val fallbackPrompt = """
                        The student shared this YouTube video: $clean
                        I could not fetch captions automatically, likely because captions are disabled or private.
                        Ask the student to paste the transcript, title, timestamp, or notes, and then help them study from it.
                    """.trimIndent()
                    sendHiddenTutorPrompt(
                        history = history,
                        prompt = fallbackPrompt,
                        status = "Preparing video help..."
                    )
                }
        }
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

    private fun submitAttachment(
        visibleMessage: String,
        prompt: String,
        mimeType: String,
        bytes: ByteArray
    ) {
        if (bytes.isEmpty()) {
            setStatus("Could not read that file.")
            return
        }
        if (bytes.size > 18 * 1024 * 1024) {
            addUserMessage(visibleMessage)
            addAiMessage("This file is too large for me to study directly here. Send a smaller file, screenshot, or the key page and I will explain it.")
            setStatus("File too large.")
            return
        }

        val history = _messages.value
        addUserMessage(visibleMessage)
        val material = PendingStudyMaterial(
            kind = if (mimeType.startsWith("image/")) "image" else "PDF",
            name = visibleMessage.removePrefix("Shared ").removePrefix("image: ").removePrefix("document: "),
            mimeType = mimeType,
            bytes = bytes
        )
        if (!shouldAnswerFromMaterialNow(history, material.kind)) {
            pendingStudyMaterial = material
            askMaterialFocus(material)
            return
        }
        viewModelScope.launch {
            _state.value = SparState.AiEvaluating
            setStatus("Studying attachment...")
            sparRepository.sendAttachmentMessage(history, systemPrompt, prompt, mimeType, bytes)
                .onSuccess { response ->
                    when {
                        response == "[SESSION_COMPLETE]" -> {
                            _state.value = SparState.StudentListening
                            setStatus("Session can continue, or tap End when you're done.")
                        }
                        response.isBlank() -> {
                            _state.value = SparState.Error("Attachment response was empty. Tap to retry.")
                        }
                        else -> {
                            speakText(response)
                        }
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "submitAttachment failed: ${e.message}")
                    _state.value = SparState.Error("Could not study that attachment. Tap to retry.")
                    haptics.error()
                }
        }
    }

    private fun queueIfTutorSpeaking(buildInput: (Long) -> QueuedStudentInput): Boolean {
        if (_state.value !is SparState.AiSpeaking && !tutorOutputPreparing) return false
        val id = System.currentTimeMillis()
        val input = buildInput(id)
        queuedInputs.add(input)
        addQueuedMessage(input.displayText, id)
        setStatus("Queued. Tutor will answer after speaking.")
        haptics.tapLight()
        Log.i(TAG, "Queued input while tutor speaking: ${input.displayText.take(80)}")
        return true
    }

    private fun processNextQueuedInput() {
        if (completionStarted || queuedInputs.isEmpty()) return
        val input = queuedInputs.removeFirst()
        removeQueuedMessage(input.id)
        setStatus("Sending queued input...")
        Log.i(TAG, "Processing queued input: ${input.displayText.take(80)}")
        when (input) {
            is QueuedStudentInput.Typed -> submitUserAnswer(input.text)
            is QueuedStudentInput.Image -> submitImageNow(input.name, input.mimeType, input.bytes)
            is QueuedStudentInput.Document -> submitDocumentNow(input.name, input.mimeType, input.bytes)
            is QueuedStudentInput.Youtube -> submitYoutubeLinkNow(input.url)
        }
    }

    private fun sendHiddenTutorPrompt(
        history: List<Message>,
        prompt: String,
        status: String
    ) {
        viewModelScope.launch {
            _state.value = SparState.AiEvaluating
            setStatus(status)
            sparRepository.sendMessage(history + Message(prompt, "user"), systemPrompt)
                .onSuccess { response ->
                    when {
                        response == "[SESSION_COMPLETE]" -> {
                            _state.value = SparState.StudentListening
                            setStatus("Session can continue, or tap End when you're done.")
                        }
                        response.isBlank() -> {
                            _state.value = SparState.Error("Response was empty. Tap to retry.")
                        }
                        else -> {
                            speakText(response)
                        }
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "sendHiddenTutorPrompt failed: ${e.message}")
                    _state.value = SparState.Error("Could not process this input. Tap to retry.")
                    haptics.error()
                }
        }
    }

    private fun askMaterialFocus(material: PendingStudyMaterial) {
        val label = when (material.kind.lowercase()) {
            "youtube" -> "video"
            "pdf" -> "PDF"
            else -> material.kind
        }
        val message = "Got it. What do you want to learn from this $label? You can ask for a summary, a doubt, an explanation, or a specific timestamp/page/question."
        viewModelScope.launch {
            speakText(message)
        }
    }

    private fun shouldAnswerFromMaterialNow(history: List<Message>, kind: String): Boolean {
        val recent = history.takeLast(5).joinToString(" ") { it.text }.lowercase()
        val materialWords = when (kind.lowercase()) {
            "youtube" -> listOf("video", "youtube", "link", "transcript")
            "pdf" -> listOf("pdf", "document", "notes", "file")
            else -> listOf("image", "photo", "screenshot", "picture")
        }
        val inviteWords = listOf(
            "send", "share", "upload", "attach", "add", "paste", "show me",
            "yes", "sure", "go ahead", "you can"
        )
        return materialWords.any { it in recent } && inviteWords.any { it in recent }
    }

    private fun handlePendingMaterialAnswer(answer: String, material: PendingStudyMaterial): Boolean {
        pendingStudyMaterial = null
        val history = _messages.value
        totalAnswers++
        addUserMessage(answer)
        Log.i(TAG, "handlePendingMaterialAnswer() kind=${material.kind} answer='${answer.take(80)}'")

        val prompt = """
            The student previously shared this ${material.kind}: ${material.name}
            They now asked: "$answer"

            Use the shared material as the main source. If they ask a direct question, answer it directly first.
            If they ask to learn, teach the core idea clearly, then ask one tiny check question.
            Keep the response aligned to ${sessionConfig?.sessionType ?: "Learning"} mode and ${sessionConfig?.difficulty ?: "Medium"} difficulty.
        """.trimIndent()

        val bytes = material.bytes
        if (bytes != null) {
            viewModelScope.launch {
                _state.value = SparState.AiEvaluating
                setStatus("Studying ${material.kind}...")
                sparRepository.sendAttachmentMessage(history, systemPrompt, prompt, material.mimeType, bytes)
                    .onSuccess { response ->
                        handleTutorResponse(response, emptyMessage = "Attachment response was empty. Tap to retry.")
                    }
                    .onFailure { e ->
                        Log.e(TAG, "pending attachment failed: ${e.message}")
                        _state.value = SparState.Error("Could not study that attachment. Tap to retry.")
                        haptics.error()
                    }
            }
        } else {
            val transcriptPrompt = """
                $prompt

                Video URL: ${material.sourceUrl.orEmpty()}
                Transcript:
                ${material.transcript.orEmpty().take(12000)}
            """.trimIndent()
            sendHiddenTutorPrompt(
                history = history,
                prompt = transcriptPrompt,
                status = "Studying video..."
            )
        }
        return true
    }

    private suspend fun handleTutorResponse(
        response: String,
        emptyMessage: String,
        countQuestion: Boolean = false
    ) {
        when {
            response == "[SESSION_COMPLETE]" -> {
                _state.value = SparState.StudentListening
                setStatus("Session can continue, or tap End when you're done.")
            }
            response.isBlank() -> {
                _state.value = SparState.Error(emptyMessage)
            }
            else -> {
                if (countQuestion) questionCount++
                speakText(response)
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

        pendingStudyMaterial?.let { material ->
            if (handlePendingMaterialAnswer(transcript, material)) return
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
                    handleTutorResponse(response, emptyMessage = "Empty response. Tap to retry.", countQuestion = true)
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
        tutorOutputPreparing = true
        _state.value = SparState.AiThinking
        setStatus("Preparing voice...")
        _audioLevel.value = 0f

        elevenLabsApi.synthesize(text, voiceId) { bytes ->
            Log.d(TAG, "ElevenLabs audio ready: ${bytes.size} bytes")
            audioPlayer.playAudio(
                bytes,
                onLevelUpdate = { _audioLevel.value = it },
                onStart = { durationMs ->
                    tutorOutputPreparing = false
                    _state.value = SparState.AiSpeaking(text)
                    setStatus("AI speaking...")
                    startStreamingAiMessage(text, durationMs)
                },
                onComplete = {
                    Log.d(TAG, "Playback done → StudentListening")
                    tutorOutputPreparing = false
                    streamingMessageJob?.cancel()
                    finishStreamingAiMessage(text)
                    _audioLevel.value = 0f
                    _state.value = SparState.StudentListening
                    setStatus("Your turn — tap mic to answer")
                    haptics.tapLight()
                    processNextQueuedInput()
                }
            )
        }.onFailure { e ->
            Log.e(TAG, "ElevenLabs failed (${e.message}), using device TTS")
            setStatus("Using device voice...")
            audioPlayer.fallbackSpeak(
                text = text,
                languageTag = teacherLanguageTag(voiceId),
                onStart = { durationMs ->
                    tutorOutputPreparing = false
                    _state.value = SparState.AiSpeaking(text)
                    setStatus("AI speaking...")
                    startStreamingAiMessage(text, durationMs)
                },
                onComplete = {
                    Log.d(TAG, "Fallback TTS done → StudentListening")
                    tutorOutputPreparing = false
                    streamingMessageJob?.cancel()
                    finishStreamingAiMessage(text)
                    _audioLevel.value = 0f
                    _state.value = SparState.StudentListening
                    setStatus("Your turn — tap mic to answer")
                    haptics.tapLight()
                    processNextQueuedInput()
                }
            )
        }
    }

    fun endSession() {
        if (completionStarted || _state.value == SparState.SessionComplete) return
        Log.i(TAG, "endSession() requested by student")
        completeSession(navigateImmediately = true)
    }

    // ─── Session Complete ─────────────────────────────────────────────────────

    private fun completeSession(navigateImmediately: Boolean = false) {
        if (completionStarted) return
        completionStarted = true
        Log.i(TAG, "completeSession() q=$questionCount answers=$totalAnswers hints=$hintsUsed")
        isTimerRunning = false
        tutorOutputPreparing = false
        streamingMessageJob?.cancel()
        streamingMessageId = null
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

            val provisionalSession = sparRepository.completeSession(
                subject = config.subject,
                chapter = config.chapter,
                difficulty = config.difficulty,
                examTarget = config.examTarget,
                questionCount = questionCount,
                score = estimatedScore,
                durationSeconds = _timer.value,
                conceptScores = emptyList(),
                aiInsight = "",
                hintsUsed = hintsUsed,
                independentAnswers = (totalAnswers - hintsUsed).coerceAtLeast(0),
                messages = _messages.value
            )

            sparRepository.generateReportForSession(
                session = provisionalSession,
                messages = _messages.value
            )
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
            }
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun addAiMessage(text: String, isHint: Boolean = false) {
        _messages.value = _messages.value + Message(text, "ai", isHint)
        Log.d(TAG, "addAiMessage() total=${_messages.value.size} isHint=$isHint")
    }

    private fun startStreamingAiMessage(text: String, durationMs: Int) {
        streamingMessageJob?.cancel()
        val messageId = System.currentTimeMillis()
        streamingMessageId = messageId
        _messages.value = _messages.value + Message("", "ai", timestamp = messageId)

        val tokens = Regex("\\S+\\s*").findAll(text).map { it.value }.toList()
        if (tokens.isEmpty()) {
            finishStreamingAiMessage(text)
            return
        }

        val perWordDelay = (durationMs.toLong() / tokens.size)
            .coerceIn(65L, 450L)

        streamingMessageJob = viewModelScope.launch {
            val visibleText = StringBuilder()
            tokens.forEach { token ->
                visibleText.append(token)
                updateAiMessage(messageId, visibleText.toString())
                delay(perWordDelay)
            }
            updateAiMessage(messageId, text)
        }
    }

    private fun finishStreamingAiMessage(text: String) {
        val messageId = streamingMessageId
        if (messageId == null) {
            addAiMessage(text)
            return
        }
        updateAiMessage(messageId, text)
        streamingMessageId = null
    }

    private fun updateAiMessage(id: Long, text: String) {
        _messages.value = _messages.value.map { message ->
            if (message.role == "ai" && message.timestamp == id) message.copy(text = text) else message
        }
    }

    private fun addUserMessage(text: String) {
        _messages.value = _messages.value + Message(text, "user")
        Log.d(TAG, "addUserMessage() total=${_messages.value.size}")
    }

    private fun addQueuedMessage(text: String, id: Long) {
        _messages.value = _messages.value + Message(text, "queued", timestamp = id)
        Log.d(TAG, "addQueuedMessage() total=${_messages.value.size}")
    }

    private fun removeQueuedMessage(id: Long) {
        _messages.value = _messages.value.filterNot { it.role == "queued" && it.timestamp == id }
        Log.d(TAG, "removeQueuedMessage() total=${_messages.value.size}")
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
        tutorOutputPreparing = false
        streamingMessageJob?.cancel()
        _isRecognizerActive.value = false
        speechService.destroy()
        audioPlayer.stop()
    }
}

private fun teacherLanguageTag(voiceId: String): String = when (voiceId) {
    "JTPrASXyK62cF3L7w8hv",
    "X5RWySWhCXiGdP9YIKck" -> "en-IN"
    else -> "en-IN"
}
