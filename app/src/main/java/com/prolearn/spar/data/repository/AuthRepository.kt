package com.prolearn.spar.data.repository

import com.prolearn.spar.data.local.AuthDataStore
import com.prolearn.spar.data.local.LoginResult
import com.prolearn.spar.data.local.SignupResult
import com.prolearn.spar.data.local.StoredUser
import com.prolearn.spar.data.local.UserDatabase
import com.prolearn.spar.domain.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val userDatabase: UserDatabase,
    private val authDataStore: AuthDataStore
) {
    val isLoggedIn: Flow<Boolean> = authDataStore.isLoggedIn
    val loggedInUserId: Flow<String?> = authDataStore.loggedInUserId

    // ─── Signup ───────────────────────────────────────────────────────────────

    suspend fun signup(
        name: String,
        email: String,
        password: String,
        examTarget: String
    ): SignupResult {
        val result = userDatabase.signup(name, email, password, examTarget)
        if (result is SignupResult.Success) {
            authDataStore.saveSession(result.user.id)
        }
        return result
    }

    // ─── Login ────────────────────────────────────────────────────────────────

    suspend fun login(email: String, password: String): LoginResult {
        val result = userDatabase.login(email, password)
        if (result is LoginResult.Success) {
            authDataStore.saveSession(result.user.id)
        }
        return result
    }

    // ─── Logout ───────────────────────────────────────────────────────────────

    suspend fun logout() {
        authDataStore.clearSession()
    }

    // ─── Get current user domain model ───────────────────────────────────────

    suspend fun getCurrentUser(userId: String): User? {
        val stored = userDatabase.getUserById(userId) ?: return null
        return stored.toDomainUser()
    }

    suspend fun updateExamTarget(userId: String, examTarget: String): User? {
        return userDatabase.updateExamTarget(userId, examTarget)?.toDomainUser()
    }
}

// ─── Extension: StoredUser → domain User ─────────────────────────────────────

fun StoredUser.toDomainUser(): User = User(
    id = id,
    name = name,
    firstName = name.split(" ").first(),
    email = email,
    examTarget = examTarget,
    avatarInitials = name.split(" ").take(2).joinToString("") { it.first().uppercase() }
)
