package com.prolearn.spar.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prolearn.spar.constants.Curriculum
import com.prolearn.spar.data.repository.AuthRepository
import com.prolearn.spar.data.repository.SparRepository
import com.prolearn.spar.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeakConcept(val name: String, val mastery: Int)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: SparRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val streak: StateFlow<Int> = repository.streak
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val bestStreak: StateFlow<Int> = repository.bestStreak
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val last7Days: StateFlow<String> = repository.last7Days
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val totalSessions: StateFlow<Int> = repository.totalSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalQuestions: StateFlow<Int> = repository.totalQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val subjects: List<String> = Curriculum.subjects

    val weakConcepts: List<WeakConcept> = listOf(
        WeakConcept("Kinematics", 72),
        WeakConcept("Newton's Laws", 48),
        WeakConcept("Electrostatics", 31)
    )

    // ─── Current logged-in user ───────────────────────────────────────────────

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.loggedInUserId.collect { userId ->
                if (userId != null) {
                    _currentUser.value = authRepository.getCurrentUser(userId)
                } else {
                    _currentUser.value = null
                }
            }
        }
    }

    fun getChapterCount(subject: String): Int {
        return Curriculum.getChapters(subject).size
    }
}
