package com.prolearn.spar.domain.model

sealed class SparState {
    data object Idle : SparState()
    data object AiThinking : SparState()
    data class AiSpeaking(val text: String) : SparState()
    data object StudentListening : SparState()
    data object StudentSpeaking : SparState()
    data object AiEvaluating : SparState()
    data class AiResponding(val text: String) : SparState()
    data object SessionComplete : SparState()
    data class Error(val message: String) : SparState()
}
