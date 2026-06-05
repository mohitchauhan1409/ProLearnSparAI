package com.prolearn.spar.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest(
    @SerialName("system_instruction") val systemInstruction: SystemInstruction,
    val contents: List<Content>,
    @SerialName("generationConfig") val generationConfig: GenerationConfig
)

@Serializable
data class SystemInstruction(val parts: List<Part>)

@Serializable
data class Content(
    val role: String,
    val parts: List<Part>
)

@Serializable
data class Part(val text: String)

@Serializable
data class GenerationConfig(
    val temperature: Float = 0.7f,
    @SerialName("maxOutputTokens") val maxOutputTokens: Int = 200
)

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null
)

@Serializable
data class Candidate(
    val content: GeminiContent? = null
)

@Serializable
data class GeminiContent(
    val parts: List<Part>? = null
)
