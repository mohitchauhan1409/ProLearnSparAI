package com.prolearn.spar.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prolearn.spar.data.local.AuthDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Splash routing outcomes:
 *  - LOADING          → still reading from disk (initial state)
 *  - SHOW_HOME        → returning user, currently logged in
 *  - SHOW_LOGIN       → not logged in
 */
enum class SplashDestination {
    LOADING,
    SHOW_HOME,
    SHOW_LOGIN
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authDataStore: AuthDataStore
) : ViewModel() {

    private val _destination = MutableStateFlow(SplashDestination.LOADING)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    init {
        // Read auth state immediately on VM creation — don't wait for a trigger.
        // Using .first() is a one-shot suspend read: it waits for the first emission
        // from DataStore (which includes the persisted on-disk value) and returns.
        // This is the correct, race-free way to read DataStore for routing decisions.
        viewModelScope.launch {
            val isLoggedIn  = authDataStore.isLoggedIn.first()

            _destination.value = when {
                isLoggedIn   -> SplashDestination.SHOW_HOME
                else         -> SplashDestination.SHOW_LOGIN
            }
        }
    }
}
