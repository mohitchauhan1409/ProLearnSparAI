package com.prolearn.spar.ui.screens.setup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prolearn.spar.constants.Curriculum
import com.prolearn.spar.data.remote.ElevenLabsApi
import com.prolearn.spar.data.repository.AuthRepository
import com.prolearn.spar.service.AudioPlaybackService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val VOICE_TAG = "voiceImprovement"

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val elevenLabsApi: ElevenLabsApi,
    private val audioPlayer: AudioPlaybackService
) : ViewModel() {

    private val _examTarget = MutableStateFlow(Curriculum.defaultTarget)
    val examTarget: StateFlow<String> = _examTarget.asStateFlow()

    private val _previewingVoiceId = MutableStateFlow<String?>(null)
    val previewingVoiceId: StateFlow<String?> = _previewingVoiceId.asStateFlow()

    private val _selectedSubject = MutableStateFlow<String?>(null)
    val selectedSubject: StateFlow<String?> = _selectedSubject.asStateFlow()

    private val _selectedChapter = MutableStateFlow<String?>(null)
    val selectedChapter: StateFlow<String?> = _selectedChapter.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow("Medium")
    val selectedDifficulty: StateFlow<String> = _selectedDifficulty.asStateFlow()

    private val _selectedVoice = MutableStateFlow(Teachers.options.first().voiceId)
    val selectedVoice: StateFlow<String> = _selectedVoice.asStateFlow()

    private val _selectedVoiceName = MutableStateFlow(Teachers.options.first().name)
    val selectedVoiceName: StateFlow<String> = _selectedVoiceName.asStateFlow()

    private val _studentFirstName = MutableStateFlow("")
    val studentFirstName: StateFlow<String> = _studentFirstName.asStateFlow()

    private var previewJob: Job? = null
    private val previewAudioCache = mutableMapOf<String, List<ByteArray>>()

    val isGhostModeAvailable = false

    init {
        viewModelScope.launch {
            authRepository.loggedInUserId.collect { userId ->
                val user = userId?.let { authRepository.getCurrentUser(it) }
                val target = user?.examTarget
                    ?: Curriculum.defaultTarget
                _studentFirstName.value = user?.firstName.orEmpty()
                _examTarget.value = target
                if (_selectedSubject.value !in subjects) {
                    _selectedSubject.value = null
                    _selectedChapter.value = null
                }
            }
        }
    }

    val subjects: List<String>
        get() = Curriculum.subjectsForTarget(_examTarget.value)

    fun selectSubject(subject: String) {
        _selectedSubject.value = subject
        _selectedChapter.value = null
    }

    fun selectChapter(chapter: String) {
        _selectedChapter.value = chapter
    }

    fun selectDifficulty(difficulty: String) {
        _selectedDifficulty.value = difficulty
    }

    fun selectVoice(voiceId: String, name: String) {
        cancelPreview()
        Log.i(VOICE_TAG, "setup_voice_selected voiceId=$voiceId name=$name")
        _selectedVoice.value = voiceId
        _selectedVoiceName.value = name
    }

    fun previewTeacher(teacher: TeacherOption) {
        if (_previewingVoiceId.value == teacher.voiceId) {
            Log.i(VOICE_TAG, "setup_voice_preview_cancel_by_retap voiceId=${teacher.voiceId}")
            cancelPreview()
            return
        }

        previewJob?.cancel()
        previewJob = viewModelScope.launch {
            audioPlayer.stop()
            _previewingVoiceId.value = teacher.voiceId
            Log.i(
                VOICE_TAG,
                "setup_voice_preview_start voiceId=${teacher.voiceId} name=${teacher.name} chars=${teacher.previewText.length}"
            )
            runCatching {
                val cachedAudio = previewAudioCache[teacher.voiceId]
                if (cachedAudio != null) {
                    Log.i(VOICE_TAG, "setup_voice_preview_cache_hit voiceId=${teacher.voiceId} chunks=${cachedAudio.size}")
                    playPreviewChunks(teacher, cachedAudio)
                } else {
                    Log.i(VOICE_TAG, "setup_voice_preview_cache_miss voiceId=${teacher.voiceId}")
                    generateAndPlayPreview(teacher)
                }
            }.onFailure {
                if (it is CancellationException) throw it
                Log.e(VOICE_TAG, "setup_voice_preview_tts_failed voiceId=${teacher.voiceId} error=${it.message}", it)
                _previewingVoiceId.value = null
            }
        }
    }

    private suspend fun generateAndPlayPreview(teacher: TeacherOption) {
        val segments = teacher.previewText.toPreviewSegments()
        val generatedAudio = mutableListOf<ByteArray>()
        Log.i(
            VOICE_TAG,
            "setup_voice_preview_segments voiceId=${teacher.voiceId} segments=${segments.size} lengths=${segments.map { it.length }}"
        )
        if (segments.isEmpty()) {
            Log.w(VOICE_TAG, "setup_voice_preview_empty voiceId=${teacher.voiceId}")
            _previewingVoiceId.value = null
            return
        }

        coroutineScope {
            var nextAudio: Deferred<ByteArray>? = synthesizePreviewSegmentAsync(teacher, segments, 0)
            segments.forEachIndexed { index, _ ->
                val audio = nextAudio?.await() ?: synthesizePreviewSegmentAsync(teacher, segments, index).await()
                generatedAudio.add(audio)
                Log.i(
                    VOICE_TAG,
                    "setup_voice_preview_chunk_ready voiceId=${teacher.voiceId} index=${index + 1}/${segments.size} bytes=${audio.size}"
                )
                nextAudio = if (index + 1 < segments.size) {
                    synthesizePreviewSegmentAsync(teacher, segments, index + 1)
                } else {
                    null
                }
                playPreviewChunk(teacher, audio, index + 1, segments.size)
            }
        }

        previewAudioCache[teacher.voiceId] = generatedAudio.toList()
        Log.i(VOICE_TAG, "setup_voice_preview_cached voiceId=${teacher.voiceId} chunks=${generatedAudio.size}")
        _previewingVoiceId.value = null
    }

    private fun CoroutineScope.synthesizePreviewSegmentAsync(
        teacher: TeacherOption,
        segments: List<String>,
        index: Int
    ): Deferred<ByteArray> = async {
        val previousText = segments.take(index).joinToString(" ")
        val nextText = segments.drop(index + 1).joinToString(" ")
        Log.i(
            VOICE_TAG,
            "setup_voice_preview_tts_start voiceId=${teacher.voiceId} index=${index + 1}/${segments.size} chars=${segments[index].length}"
        )
        elevenLabsApi.synthesizeBytes(
            text = segments[index],
            voiceId = teacher.voiceId,
            previousText = previousText,
            nextText = nextText,
            languageCode = teacher.previewLanguageCode
        ).getOrThrow()
    }

    private suspend fun playPreviewChunks(teacher: TeacherOption, chunks: List<ByteArray>) {
        chunks.forEachIndexed { index, bytes ->
            playPreviewChunk(teacher, bytes, index + 1, chunks.size)
        }
        Log.i(VOICE_TAG, "setup_voice_preview_complete voiceId=${teacher.voiceId} source=cache")
        _previewingVoiceId.value = null
    }

    private suspend fun playPreviewChunk(
        teacher: TeacherOption,
        bytes: ByteArray,
        index: Int,
        total: Int
    ) {
        val done = CompletableDeferred<Unit>()
        Log.i(VOICE_TAG, "setup_voice_preview_play_start voiceId=${teacher.voiceId} index=$index/$total bytes=${bytes.size}")
        audioPlayer.playAudio(
            bytes = bytes,
            onLevelUpdate = {},
            onError = {
                Log.e(
                    VOICE_TAG,
                    "setup_voice_preview_playback_failed voiceId=${teacher.voiceId} index=$index/$total error=${it.message}",
                    it
                )
                done.completeExceptionally(it)
            },
            onComplete = {
                Log.i(VOICE_TAG, "setup_voice_preview_play_complete voiceId=${teacher.voiceId} index=$index/$total")
                done.complete(Unit)
            }
        )
        done.await()
    }

    fun getChapters(): List<String> {
        val chapters = _selectedSubject.value?.let {
            Curriculum.getChapters(it, _examTarget.value)
        } ?: emptyList()
        return listOf("Generic") + chapters
    }

    fun getConcepts(): List<String> {
        val subject = _selectedSubject.value ?: return emptyList()
        val chapter = _selectedChapter.value ?: return emptyList()
        if (chapter == "Generic") return emptyList()
        return Curriculum.getConcepts(subject, chapter, _examTarget.value)
    }

    val isValid: Boolean
        get() = _selectedSubject.value != null && _selectedChapter.value != null

    override fun onCleared() {
        cancelPreview()
        super.onCleared()
    }

    private fun cancelPreview() {
        Log.d(VOICE_TAG, "setup_voice_preview_cancel current=${_previewingVoiceId.value}")
        previewJob?.cancel()
        previewJob = null
        audioPlayer.stop()
        _previewingVoiceId.value = null
    }
}

data class TeacherOption(
    val voiceId: String,
    val style: String,
    val name: String,
    val language: String,
    val specialty: String,
    val previewText: String,
    val previewLanguageCode: String? = null
) {
    val displayName: String get() = "$name ($style)"
}

object Teachers {
    val options = listOf(
        TeacherOption(
            voiceId = "LHJy3mhZWsvhUjy0zUM1",
            style = "Clear",
            name = "Amit",
            language = "Hinglish",
            specialty = "Expert at simplifying tough ideas",
            previewText = "Hello, welcome to ProLearn. Chahe aap kisi tough question par atke ho ya kisi new topic ko explore karna chahte ho, main help karne ke liye yahan hoon. Bas apna question poochiye, aur hum milkar usse samajhte hain."
        ),
        TeacherOption(
            voiceId = "MF4J4IDTRo0AxOO4dpFR",
            style = "Warm",
            name = "Neha",
            language = "Hinglish",
            specialty = "Expert at focus and exam discipline",
            previewText = "Namaste, welcome to ProLearn. Agar kisi topic mein confusion ho ya koi question difficult lag raha ho, toh bas mujhse pooch lijiye. Main concepts ko simple language mein samjhaungi aur har step par aapki help karungi."
        ),
        TeacherOption(
            voiceId = "eUKPwd15VeaPJ9bDZ6iM",
            style = "Calm",
            name = "Ravi",
            language = "English",
            specialty = "Expert at confidence building",
            previewText = "Hello, and welcome to ProLearn. You can ask questions, upload images, or simply speak to me. I'll explain concepts clearly, break down complex problems, and help you understand the reasoning behind every answer.",
            previewLanguageCode = "en"
        ),
        TeacherOption(
            voiceId = "P7vsEyTOpZ6YUTulin8m",
            style = "Soft",
            name = "Tara",
            language = "English",
            specialty = "Expert at student psychology",
            previewText = "Hi, welcome to ProLearn. I'm here to make learning feel a little easier. Whether you're studying for an exam, reviewing a concept, or solving a difficult question, I'll guide you step by step and help you build confidence along the way.",
            previewLanguageCode = "en"
        )
    )
}

private fun String.toPreviewSegments(maxChars: Int = 48): List<String> {
    if (isBlank()) return emptyList()
    val segments = mutableListOf<String>()
    val current = StringBuilder()

    Regex("(?<=[.!?])\\s+").split(trim()).forEach { sentence ->
        val part = sentence.trim()
        if (part.isBlank()) return@forEach
        val candidateLength = current.length + part.length + 1
        if (candidateLength > maxChars && current.isNotEmpty()) {
            segments.add(current.toString().trim())
            current.clear()
        }
        if (current.isNotEmpty()) current.append(' ')
        current.append(part)
    }

    if (current.isNotBlank()) segments.add(current.toString().trim())
    return segments
}
