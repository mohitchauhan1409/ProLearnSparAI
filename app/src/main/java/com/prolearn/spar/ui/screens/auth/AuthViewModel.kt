package com.prolearn.spar.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prolearn.spar.data.local.LoginResult
import com.prolearn.spar.data.local.SignupResult
import com.prolearn.spar.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── UI state models ──────────────────────────────────────────────────────────

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val loginState: StateFlow<AuthUiState> = _loginState.asStateFlow()

    private val _signupState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val signupState: StateFlow<AuthUiState> = _signupState.asStateFlow()

    // ─── Login ────────────────────────────────────────────────────────────────

    fun login(email: String, password: String) {
        // Basic client-side guard first
        if (email.isBlank()) {
            _loginState.value = AuthUiState.Error("Please enter your email address.")
            return
        }
        if (password.isBlank()) {
            _loginState.value = AuthUiState.Error("Please enter your password.")
            return
        }

        viewModelScope.launch {
            _loginState.value = AuthUiState.Loading
            val result = authRepository.login(email.trim(), password)
            _loginState.value = when (result) {
                is LoginResult.Success -> AuthUiState.Success
                is LoginResult.InvalidCredentials ->
                    AuthUiState.Error("Incorrect email or password. Please try again.")
            }
        }
    }

    fun resetLoginState() {
        _loginState.value = AuthUiState.Idle
    }

    // ─── Signup ───────────────────────────────────────────────────────────────

    fun signup(name: String, email: String, password: String, examTarget: String) {
        if (name.isBlank()) {
            _signupState.value = AuthUiState.Error("Please enter your full name.")
            return
        }
        if (email.isBlank()) {
            _signupState.value = AuthUiState.Error("Please enter your email address.")
            return
        }
        if (password.length < 6) {
            _signupState.value = AuthUiState.Error("Password must be at least 6 characters.")
            return
        }

        viewModelScope.launch {
            _signupState.value = AuthUiState.Loading
            val result = authRepository.signup(name.trim(), email.trim(), password, examTarget)
            _signupState.value = when (result) {
                is SignupResult.Success -> AuthUiState.Success
                is SignupResult.EmailAlreadyExists ->
                    AuthUiState.Error("An account with this email already exists. Try signing in.")
                is SignupResult.InvalidEmail ->
                    AuthUiState.Error("Please enter a valid email address.")
                is SignupResult.WeakPassword ->
                    AuthUiState.Error("Password must be at least 6 characters.")
            }
        }
    }

    fun resetSignupState() {
        _signupState.value = AuthUiState.Idle
    }

    // ─── Logout ───────────────────────────────────────────────────────────────

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onComplete()
        }
    }
}
