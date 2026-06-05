package com.prolearn.spar.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "prolearn_prefs")

@Singleton
class SessionDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val SESSIONS_JSON_KEY = stringPreferencesKey("sessions_json")
        val STREAK_KEY = intPreferencesKey("streak_days")
        val BEST_STREAK_KEY = intPreferencesKey("best_streak_days")
        val HAS_LAUNCHED_KEY = booleanPreferencesKey("has_launched")
        val LAST_SPAR_DATE_KEY = stringPreferencesKey("last_spar_date")
        val CONCEPT_MASTERY_KEY = stringPreferencesKey("concept_mastery_json")
        val SELECTED_VOICE_KEY = stringPreferencesKey("selected_voice")
        val LAST_7_DAYS_KEY = stringPreferencesKey("last_7_days_json")
        val TOTAL_SESSIONS_KEY = intPreferencesKey("total_sessions")
        val TOTAL_QUESTIONS_KEY = intPreferencesKey("total_questions")
    }

    val hasLaunched: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[HAS_LAUNCHED_KEY] ?: false
    }

    val streak: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[STREAK_KEY] ?: 0
    }

    val bestStreak: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[BEST_STREAK_KEY] ?: 0
    }

    val lastSparDate: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LAST_SPAR_DATE_KEY] ?: ""
    }

    val last7Days: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LAST_7_DAYS_KEY] ?: ""
    }

    val totalSessions: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[TOTAL_SESSIONS_KEY] ?: 0
    }

    val totalQuestions: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[TOTAL_QUESTIONS_KEY] ?: 0
    }

    val selectedVoice: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_VOICE_KEY] ?: "EXAVITQu4vr4xnSDxMaL"
    }

    suspend fun setHasLaunched() {
        context.dataStore.edit { prefs -> prefs[HAS_LAUNCHED_KEY] = true }
    }

    suspend fun updateStreak(date: String) {
        context.dataStore.edit { prefs ->
            val lastDate = prefs[LAST_SPAR_DATE_KEY] ?: ""
            val currentStreak = prefs[STREAK_KEY] ?: 0
            val bestStreak = prefs[BEST_STREAK_KEY] ?: 0

            val yesterday = getYesterdayDate()
            val newStreak = when {
                date == lastDate -> currentStreak
                lastDate == yesterday || lastDate.isEmpty() -> currentStreak + 1
                else -> 1
            }
            prefs[STREAK_KEY] = newStreak
            prefs[LAST_SPAR_DATE_KEY] = date
            if (newStreak > bestStreak) {
                prefs[BEST_STREAK_KEY] = newStreak
            }

            val last7Days = parseLast7Days(prefs[LAST_7_DAYS_KEY] ?: "")
            val updated = last7Days.toMutableList().apply {
                if (size >= 7) removeAt(0)
                add(true)
            }
            while (updated.size < 7) updated.add(false)
            prefs[LAST_7_DAYS_KEY] = updated.joinToString(",") { it.toString() }
        }
    }

    suspend fun incrementTotalSessions() {
        context.dataStore.edit { prefs ->
            prefs[TOTAL_SESSIONS_KEY] = (prefs[TOTAL_SESSIONS_KEY] ?: 0) + 1
        }
    }

    suspend fun addQuestions(count: Int) {
        context.dataStore.edit { prefs ->
            prefs[TOTAL_QUESTIONS_KEY] = (prefs[TOTAL_QUESTIONS_KEY] ?: 0) + count
        }
    }

    suspend fun setConceptMastery(json: String) {
        context.dataStore.edit { prefs -> prefs[CONCEPT_MASTERY_KEY] = json }
    }

    suspend fun getConceptMastery(): String {
        var result = ""
        context.dataStore.edit { prefs ->
            result = prefs[CONCEPT_MASTERY_KEY] ?: ""
        }
        return result
    }

    private fun getYesterdayDate(): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        return "${cal.get(java.util.Calendar.DAY_OF_MONTH)}-${cal.get(java.util.Calendar.MONTH) + 1}-${cal.get(java.util.Calendar.YEAR)}"
    }

    private fun parseLast7Days(json: String): List<Boolean> {
        if (json.isEmpty()) return emptyList()
        return json.split(",").map { it.toBoolean() }
    }
}
