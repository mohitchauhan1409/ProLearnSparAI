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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
private const val VOICE_TAG = "voiceImprovement"

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
    private var tutorSpeechJob: Job? = null
    private var tutorSpeechInterrupted = false
    private var lastTutorVoiceFailureText: String? = null

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
        Log.i(
            VOICE_TAG,
            "session_voice_start voiceId=${config.voiceId} voiceName=${config.voiceName} " +
                "sessionType=${config.sessionType} subject=${config.subject} chapter=${config.chapter}"
        )
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

        if (currentState is SparState.AiSpeaking || tutorOutputPreparing) {
            Log.i(VOICE_TAG, "barge_in_requested state=$currentState tutorOutputPreparing=$tutorOutputPreparing")
            interruptTutorForStudent()
        } else if (currentState is SparState.AiThinking ||
            currentState is SparState.AiEvaluating) {
            Log.w(TAG, "startListening() blocked — AI busy (state=$currentState)")
            Log.w(VOICE_TAG, "mic_start_blocked_ai_busy state=$currentState")
            return
        }
        if (recognizerActive) {
            Log.w(TAG, "startListening() blocked — recognizer already active")
            Log.w(VOICE_TAG, "mic_start_blocked_recognizer_active")
            return
        }

        _partialTranscript.value = ""
        _audioLevel.value = 0f
        _isRecognizerActive.value = true
        _state.value = SparState.StudentListening
        setStatus("Listening — speak your answer")
        Log.i(TAG, "→ StudentListening, launching SpeechRecognizer")
        Log.i(VOICE_TAG, "student_listening_start")

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
                Log.i(VOICE_TAG, "student_transcript_final chars=${result.length} blank=${result.isBlank()}")
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
                Log.e(VOICE_TAG, "student_transcription_error message=$errorMsg")
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
        Log.i(VOICE_TAG, "student_listening_stop_requested")
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
        lastTutorVoiceFailureText?.let { failedText ->
            Log.i(VOICE_TAG, "voice_retry_last_failed_tutor_text chars=${failedText.length}")
            lastTutorVoiceFailureText = null
            speakText(failedText)
            return
        }
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

    private fun speakText(text: String) {
        tutorSpeechJob?.cancel()
        tutorSpeechInterrupted = false
        Log.i(VOICE_TAG, "tutor_speech_job_start chars=${text.length}")
        tutorSpeechJob = viewModelScope.launch {
            speakTextInternal(text)
        }
    }

    private suspend fun speakTextInternal(text: String) {
        val voiceId = sessionConfig?.voiceId ?: VoiceIds.ARIA
        Log.i(TAG, "speakText() voiceId=$voiceId '${text.take(60)}'")
        val speechStartedAt = System.currentTimeMillis()
        tutorOutputPreparing = true
        _state.value = SparState.AiThinking
        setStatus("Preparing voice...")
        _audioLevel.value = 0f

        val spokenText = text.prepareForSpeech()
        val segments = spokenText.toSpeechSegments()
        Log.i(
            VOICE_TAG,
            "tutor_speech_prepare voiceId=$voiceId originalChars=${text.length} spokenChars=${spokenText.length} segments=${segments.size}"
        )
        if (segments.isEmpty()) {
            Log.w(VOICE_TAG, "tutor_speech_empty_after_normalization")
            tutorOutputPreparing = false
            _state.value = SparState.StudentListening
            setStatus("Your turn — tap mic to answer")
            return
        }

        try {
            coroutineScope {
                var nextAudio: Deferred<ByteArray>? = synthesizeSegmentAsync(
                    segments = segments,
                    index = 0,
                    voiceId = voiceId
                )

                segments.forEachIndexed { index, _ ->
                    Log.i(VOICE_TAG, "tutor_segment_wait index=${index + 1}/${segments.size}")
                    val audio = nextAudio?.await() ?: synthesizeSegmentAsync(segments, index, voiceId).await()
                    Log.i(VOICE_TAG, "tutor_segment_audio_received index=${index + 1}/${segments.size} bytes=${audio.size}")
                    nextAudio = if (index + 1 < segments.size) {
                        synthesizeSegmentAsync(segments, index + 1, voiceId)
                    } else {
                        null
                    }

                    playTutorAudioSegment(
                        bytes = audio,
                        fullText = text,
                        isFirstSegment = index == 0,
                        estimatedDurationMs = estimateSpeechDurationMs(spokenText)
                    )
                }
            }

            Log.d(TAG, "Tutor playback done → StudentListening")
            Log.i(VOICE_TAG, "tutor_speech_complete segments=${segments.size} totalMs=${System.currentTimeMillis() - speechStartedAt}")
            lastTutorVoiceFailureText = null
            tutorOutputPreparing = false
            streamingMessageJob?.cancel()
            finishStreamingAiMessage(text)
            _audioLevel.value = 0f
            _state.value = SparState.StudentListening
            setStatus("Your turn — tap mic to answer")
            haptics.tapLight()
            processNextQueuedInput()
        } catch (e: CancellationException) {
            Log.i(VOICE_TAG, "tutor_speech_cancelled interrupted=$tutorSpeechInterrupted")
            tutorOutputPreparing = false
            _audioLevel.value = 0f
            if (tutorSpeechInterrupted) return
            throw e
        } catch (e: Exception) {
            if (tutorSpeechInterrupted) {
                Log.i(TAG, "Tutor speech interrupted by student")
                tutorOutputPreparing = false
                _audioLevel.value = 0f
                return
            }
            Log.e(TAG, "ElevenLabs speech failed: ${e.message}")
            Log.e(VOICE_TAG, "tutor_speech_failed error=${e.message} totalMs=${System.currentTimeMillis() - speechStartedAt}", e)
            lastTutorVoiceFailureText = text
            tutorOutputPreparing = false
            streamingMessageJob?.cancel()
            _audioLevel.value = 0f
            _state.value = SparState.Error("Teacher voice connection failed. Tap to retry.")
            setStatus("Voice connection failed.")
            haptics.error()
        }
    }

    private fun CoroutineScope.synthesizeSegmentAsync(
        segments: List<String>,
        index: Int,
        voiceId: String
    ): Deferred<ByteArray> = async {
        val previousText = segments.take(index).joinToString(" ")
        val nextText = segments.drop(index + 1).joinToString(" ")
        Log.i(
            VOICE_TAG,
            "tutor_segment_tts_start index=${index + 1}/${segments.size} chars=${segments[index].length} " +
                "previousChars=${previousText.length} nextChars=${nextText.length}"
        )
        elevenLabsApi.synthesizeBytes(
            text = segments[index],
            voiceId = voiceId,
            previousText = previousText,
            nextText = nextText,
            languageCode = teacherTtsLanguageCode(voiceId)
        ).getOrThrow()
    }

    private suspend fun playTutorAudioSegment(
        bytes: ByteArray,
        fullText: String,
        isFirstSegment: Boolean,
        estimatedDurationMs: Int
    ) {
        val playbackDone = CompletableDeferred<Unit>()
        Log.i(VOICE_TAG, "tutor_segment_playback_start isFirst=$isFirstSegment bytes=${bytes.size}")
        audioPlayer.playAudio(
            bytes = bytes,
            onLevelUpdate = { _audioLevel.value = it },
            onStart = {
                Log.i(VOICE_TAG, "tutor_segment_playback_audio_started isFirst=$isFirstSegment durationMs=$it")
                if (isFirstSegment) {
                    tutorOutputPreparing = false
                    _state.value = SparState.AiSpeaking(fullText)
                    setStatus("Tutor is speaking. Tap mic to interrupt.")
                    startStreamingAiMessage(fullText, estimatedDurationMs)
                }
            },
            onError = { error ->
                Log.e(VOICE_TAG, "tutor_segment_playback_failed isFirst=$isFirstSegment error=${error.message}", error)
                playbackDone.completeExceptionally(error)
            },
            onComplete = {
                Log.i(VOICE_TAG, "tutor_segment_playback_complete isFirst=$isFirstSegment")
                playbackDone.complete(Unit)
            }
        )
        playbackDone.await()
    }

    private fun interruptTutorForStudent() {
        Log.i(TAG, "interruptTutorForStudent()")
        Log.i(VOICE_TAG, "barge_in_interrupting_tutor")
        tutorSpeechInterrupted = true
        tutorSpeechJob?.cancel()
        tutorSpeechJob = null
        tutorOutputPreparing = false
        streamingMessageJob?.cancel()
        streamingMessageId = null
        audioPlayer.stop()
        _audioLevel.value = 0f
        setStatus("Listening — go ahead")
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
        tutorSpeechJob?.cancel()
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

private fun teacherTtsLanguageCode(voiceId: String): String? = when (voiceId) {
    "LHJy3mhZWsvhUjy0zUM1",
    "MF4J4IDTRo0AxOO4dpFR" -> null
    else -> "en"
}

private fun String.prepareForSpeech(): String =
    replace("—", ", ")
        .replace("–", ", ")
        .replace("→", " gives ")
        .replace("=", " equals ")
        .replace("+", " plus ")
        .replace("-", " minus ")
        .replace("*", " times ")
        .replace("/", " divided by ")
        .replace(Regex("\\s+"), " ")
        .trim()

private fun String.toSpeechSegments(maxChars: Int = 105): List<String> {
    if (isBlank()) return emptyList()

    val sentenceParts = Regex("(?<=[.!?])\\s+").split(trim())
    val segments = mutableListOf<String>()
    val current = StringBuilder()

    fun flush() {
        val text = current.toString().trim()
        if (text.isNotBlank()) segments.add(text)
        current.clear()
    }

    sentenceParts.forEach { sentence ->
        val part = sentence.trim()
        if (part.isBlank()) return@forEach
        if (part.length > maxChars) {
            flush()
            splitLongSpeechPart(part, maxChars).forEach(segments::add)
            return@forEach
        }
        val candidateLength = current.length + part.length + 1
        if (candidateLength > maxChars) flush()
        if (current.isNotEmpty()) current.append(' ')
        current.append(part)
    }
    flush()
    return segments
}

private fun splitLongSpeechPart(text: String, maxChars: Int): List<String> {
    val chunks = mutableListOf<String>()
    val current = StringBuilder()
    text.split(Regex("(?<=,)\\s+|\\s+")).forEach { token ->
        if (token.isBlank()) return@forEach
        if (current.length + token.length + 1 > maxChars && current.isNotEmpty()) {
            chunks.add(current.toString().trim())
            current.clear()
        }
        if (current.isNotEmpty()) current.append(' ')
        current.append(token)
    }
    if (current.isNotBlank()) chunks.add(current.toString().trim())
    return chunks
}

private fun estimateSpeechDurationMs(text: String): Int {
    val words = Regex("\\S+").findAll(text).count()
    return (words * 330).coerceAtLeast(1400)
}
