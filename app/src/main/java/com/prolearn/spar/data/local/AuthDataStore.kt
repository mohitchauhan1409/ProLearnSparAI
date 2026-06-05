package com.prolearn.spar.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "prolearn_auth")

@Singleton
class AuthDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
        val LOGGED_IN_USER_ID_KEY = stringPreferencesKey("logged_in_user_id")
    }

    val isLoggedIn: Flow<Boolean> = context.authDataStore.data.map { prefs ->
        prefs[IS_LOGGED_IN_KEY] ?: false
    }

    val loggedInUserId: Flow<String?> = context.authDataStore.data.map { prefs ->
        prefs[LOGGED_IN_USER_ID_KEY]
    }

    suspend fun saveSession(userId: String) {
        context.authDataStore.edit { prefs ->
            prefs[IS_LOGGED_IN_KEY] = true
            prefs[LOGGED_IN_USER_ID_KEY] = userId
        }
    }

    suspend fun clearSession() {
        context.authDataStore.edit { prefs ->
            prefs[IS_LOGGED_IN_KEY] = false
            prefs.remove(LOGGED_IN_USER_ID_KEY)
        }
    }
}
