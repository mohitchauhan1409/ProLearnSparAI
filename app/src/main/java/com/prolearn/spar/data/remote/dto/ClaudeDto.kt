package com.prolearn.spar.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * DTOs for the Anthropic Messages API (`POST /v1/messages`).
 * Raw HTTP via Ktor, matching the project's existing GeminiApi / ElevenLabsApi style.
 */
@Serializable
data class ClaudeRequest(
    val model: String,
    @SerialName("max_tokens") val maxTokens: Int,
    val system: String,
    val messages: List<ClaudeMessage>,
    @SerialName("output_config") val outputConfig: ClaudeOutputConfig? = null
)

@Serializable
data class ClaudeMessage(
    val role: String,
    val content: String
)

@Serializable
data class ClaudeOutputConfig(
    val format: ClaudeFormat
)

@Serializable
data class ClaudeFormat(
    // NOTE: no default value — kotlinx.serialization omits default-valued fields
    // (encodeDefaults=false), and Anthropic requires output_config.format.type.
    val type: String,
    val schema: JsonObject
)

@Serializable
data class ClaudeResponse(
    val content: List<ClaudeContentBlock>? = null,
    @SerialName("stop_reason") val stopReason: String? = null,
    @SerialName("stop_details") val stopDetails: ClaudeStopDetails? = null
)

@Serializable
data class ClaudeContentBlock(
    val type: String,
    val text: String? = null
)

@Serializable
data class ClaudeStopDetails(
    val category: String? = null,
    val explanation: String? = null
)
