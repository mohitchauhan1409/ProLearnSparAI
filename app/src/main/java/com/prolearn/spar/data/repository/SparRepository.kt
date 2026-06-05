package com.prolearn.spar.data.repository

import com.prolearn.spar.data.local.SessionDataStore
import com.prolearn.spar.data.remote.GeminiApi
import com.prolearn.spar.data.remote.VoiceIds
import com.prolearn.spar.domain.model.Message
import com.prolearn.spar.domain.model.Session
import com.prolearn.spar.domain.model.SessionAnalysis
import com.prolearn.spar.domain.model.ConceptScore
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SparRepository @Inject constructor(
    private val geminiApi: GeminiApi,
    private val sessionDataStore: SessionDataStore
) {
    val hasLaunched: Flow<Boolean> = sessionDataStore.hasLaunched
    val streak: Flow<Int> = sessionDataStore.streak
    val bestStreak: Flow<Int> = sessionDataStore.bestStreak
    val lastSparDate: Flow<String> = sessionDataStore.lastSparDate
    val last7Days: Flow<String> = sessionDataStore.last7Days
    val totalSessions: Flow<Int> = sessionDataStore.totalSessions
    val totalQuestions: Flow<Int> = sessionDataStore.totalQuestions
    val selectedVoice: Flow<String> = sessionDataStore.selectedVoice

    suspend fun sendMessage(messages: List<Message>, systemPrompt: String): Result<String> {
        return geminiApi.sendMessage(messages, systemPrompt)
    }

    suspend fun getHint(question: String): Result<String> {
        return geminiApi.getHint(question)
    }

    suspend fun analyzeSession(transcript: List<Message>): Result<SessionAnalysis> {
        return geminiApi.analyzeSession(transcript)
    }

    suspend fun completeSession(
        subject: String,
        chapter: String,
        difficulty: String,
        examTarget: String,
        questionCount: Int,
        score: Int,
        durationSeconds: Int,
        conceptScores: List<ConceptScore>,
        aiInsight: String,
        hintsUsed: Int,
        independentAnswers: Int,
        messages: List<Message>
    ): Session {
        val dateFormat = SimpleDateFormat("d-M-yyyy", Locale.getDefault())
        val today = dateFormat.format(Date())

        sessionDataStore.updateStreak(today)
        sessionDataStore.incrementTotalSessions()
        sessionDataStore.addQuestions(questionCount)

        return Session(
            id = UUID.randomUUID().toString(),
            subject = subject,
            chapter = chapter,
            difficulty = difficulty,
            examTarget = examTarget,
            questionCount = questionCount,
            score = score,
            durationSeconds = durationSeconds,
            timestamp = System.currentTimeMillis(),
            conceptScores = conceptScores,
            aiInsight = aiInsight,
            hintsUsed = hintsUsed,
            independentAnswers = independentAnswers
        )
    }

    suspend fun setHasLaunched() {
        sessionDataStore.setHasLaunched()
    }

    suspend fun getConceptMastery(): String {
        return sessionDataStore.getConceptMastery()
    }

    suspend fun setConceptMastery(json: String) {
        sessionDataStore.setConceptMastery(json)
    }
}
