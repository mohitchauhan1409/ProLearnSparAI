package com.prolearn.spar.ui.screens.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prolearn.spar.constants.Curriculum
import com.prolearn.spar.data.remote.ElevenLabsApi
import com.prolearn.spar.data.repository.AuthRepository
import com.prolearn.spar.service.AudioPlaybackService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    private val _selectedVoiceName = MutableStateFlow(Teachers.options.first().displayName)
    val selectedVoiceName: StateFlow<String> = _selectedVoiceName.asStateFlow()

    private var previewJob: Job? = null

    val isGhostModeAvailable = false

    init {
        viewModelScope.launch {
            authRepository.loggedInUserId.collect { userId ->
                val target = userId?.let { authRepository.getCurrentUser(it)?.examTarget }
                    ?: Curriculum.defaultTarget
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
        _selectedVoice.value = voiceId
        _selectedVoiceName.value = name
    }

    fun previewTeacher(teacher: TeacherOption) {
        if (_previewingVoiceId.value == teacher.voiceId) {
            cancelPreview()
            return
        }

        previewJob?.cancel()
        previewJob = viewModelScope.launch {
            audioPlayer.stop()
            _previewingVoiceId.value = teacher.voiceId
            val text = teacher.previewText
            elevenLabsApi.synthesize(text, teacher.voiceId) { bytes ->
                if (previewJob?.isActive == true && _previewingVoiceId.value == teacher.voiceId) {
                    audioPlayer.playAudio(
                        bytes = bytes,
                        onLevelUpdate = {},
                        onComplete = { _previewingVoiceId.value = null }
                    )
                }
            }.onFailure {
                if (it is CancellationException) throw it
                if (previewJob?.isActive == true) {
                    audioPlayer.fallbackSpeak(text) {
                        _previewingVoiceId.value = null
                    }
                }
            }
        }
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
    val previewText: String
) {
    val displayName: String get() = "$name ($style)"
}

object Teachers {
    val options = listOf(
        TeacherOption(
            voiceId = "JTPrASXyK62cF3L7w8hv",
            style = "Clear",
            name = "P k anil",
            language = "Hindi",
            specialty = "Expert at simplifying tough ideas",
            previewText = "नमस्ते! मैं पी के अनिल हूं। मुश्किल कॉन्सेप्ट को बिल्कुल आसान भाषा में, स्टेप बाय स्टेप समझाएंगे। चलिए शुरू करते हैं।"
        ),
        TeacherOption(
            voiceId = "X5RWySWhCXiGdP9YIKck",
            style = "Warn",
            name = "Tripti",
            language = "Hindi",
            specialty = "Expert at focus and exam discipline",
            previewText = "नमस्ते, मैं तृप्ति हूं। आज हम ध्यान भटकाने वाली चीजों को साइड में रखेंगे, स्मार्ट प्रैक्टिस करेंगे, और आपकी तैयारी को तेज बनाएंगे।"
        ),
        TeacherOption(
            voiceId = "2BsEFcU7jUhLaUwV4h7l",
            style = "Calm",
            name = "Manav",
            language = "English",
            specialty = "Expert at confidence building",
            previewText = "Hi, I'm Manav. We'll slow things down, understand the idea clearly, and build your confidence one answer at a time."
        ),
        TeacherOption(
            voiceId = "P0TQBmxaqqw6qfDmK2xb",
            style = "Soft",
            name = "Simran",
            language = "English",
            specialty = "Expert at student psychology",
            previewText = "Hi, I'm Simran. I'll keep the session gentle, focused, and encouraging so you can think clearly and grow without pressure."
        )
    )
}
