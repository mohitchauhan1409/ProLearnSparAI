package com.prolearn.spar.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SessionAnalysis(
    val conceptScores: List<ConceptScore>,
    val aiInsight: String,
    val overallScore: Int,
    val reportDetails: SessionReportDetails = SessionReportDetails()
)
