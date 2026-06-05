package com.prolearn.spar.domain.model

data class Message(
    val text: String,
    val role: String,
    val isHint: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
