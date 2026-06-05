package com.prolearn.spar.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

// ─── Stored user model (persisted as JSON) ────────────────────────────────────

@Serializable
data class StoredUser(
    val id: String,
    val name: String,
    val email: String,
    val passwordHash: String,
    val examTarget: String,
    val createdAt: Long
)

// ─── Sealed result types ──────────────────────────────────────────────────────

sealed class SignupResult {
    data class Success(val user: StoredUser) : SignupResult()
    data object EmailAlreadyExists : SignupResult()
    data object InvalidEmail : SignupResult()
    data object WeakPassword : SignupResult()
}

sealed class LoginResult {
    data class Success(val user: StoredUser) : LoginResult()
    data object InvalidCredentials : LoginResult()
}

// ─── Separate DataStore instance for users DB ─────────────────────────────────

private val Context.userDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "prolearn_users_db")

@Singleton
class UserDatabase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val USERS_JSON_KEY = stringPreferencesKey("users_json")
        private val json = Json { ignoreUnknownKeys = true }
    }

    // ─── Read all users ───────────────────────────────────────────────────────

    private suspend fun readUsers(): List<StoredUser> {
        val prefs = context.userDataStore.data.first()
        val raw = prefs[USERS_JSON_KEY] ?: return emptyList()
        return try {
            json.decodeFromString<List<StoredUser>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun writeUsers(users: List<StoredUser>) {
        context.userDataStore.edit { prefs ->
            prefs[USERS_JSON_KEY] = json.encodeToString(users)
        }
    }

    // ─── Signup ───────────────────────────────────────────────────────────────

    suspend fun signup(
        name: String,
        email: String,
        password: String,
        examTarget: String
    ): SignupResult {
        // Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            return SignupResult.InvalidEmail
        }
        // Validate password
        if (password.length < 6) {
            return SignupResult.WeakPassword
        }

        val users = readUsers()

        // Check duplicate email (case-insensitive)
        if (users.any { it.email.equals(email.trim(), ignoreCase = true) }) {
            return SignupResult.EmailAlreadyExists
        }

        val newUser = StoredUser(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            email = email.trim().lowercase(),
            passwordHash = hashPassword(password),
            examTarget = examTarget,
            createdAt = System.currentTimeMillis()
        )

        writeUsers(users + newUser)
        return SignupResult.Success(newUser)
    }

    suspend fun login(email: String, password: String): LoginResult {
        val users = readUsers()
        val match = users.find {
            it.email.equals(email.trim(), ignoreCase = true) &&
                    it.passwordHash == hashPassword(password)
        }
        return if (match != null) LoginResult.Success(match)
        else LoginResult.InvalidCredentials
    }


    suspend fun getUserById(id: String): StoredUser? {
        return readUsers().find { it.id == id }
    }

    private fun hashPassword(password: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
