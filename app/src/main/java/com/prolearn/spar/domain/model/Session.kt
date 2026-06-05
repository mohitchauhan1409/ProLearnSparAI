package com.prolearn.spar.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Session(
    val id: String,
    val subject: String,
    val chapter: String,
    val difficulty: String,
    val examTarget: String,
    val questionCount: Int,
    val score: Int,
    val durationSeconds: Int,
    val timestamp: Long,
    val conceptScores: List<ConceptScore>,
    val aiInsight: String,
    val hintsUsed: Int,
    val independentAnswers: Int,
    val voiceMetrics: VoiceMetrics? = null
)

@Serializable
data class ConceptScore(
    val name: String,
    val score: Int
)

@Serializable
data class VoiceMetrics(
    val avgResponseLatencyMs: Long = 0,
    val hesitationCount: Int = 0,
    val avgResponseLengthWords: Float = 0f,
    val trailingOffCount: Int = 0
) {
    val confidenceScore: Int
        get() {
            val latencyPenalty = (avgResponseLatencyMs / 50).toInt().coerceIn(0, 30)
            val hesitationPenalty = (hesitationCount * 5).coerceIn(0, 25)
            val lengthBonus = (avgResponseLengthWords * 3).toInt().coerceIn(0, 15)
            return (100 - latencyPenalty - hesitationPenalty + lengthBonus).coerceIn(0, 100)
        }
}
