package com.prolearn.spar.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prolearn.spar.data.repository.SparRepository
import com.prolearn.spar.domain.model.Session
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SessionReportViewModel @Inject constructor(
    sparRepository: SparRepository
) : ViewModel() {
    val session: StateFlow<Session?> = sparRepository.lastCompletedSession.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = sparRepository.lastCompletedSession.value
    )
}
