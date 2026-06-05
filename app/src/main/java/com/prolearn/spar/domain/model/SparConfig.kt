package com.prolearn.spar.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SparConfig(
    val subject: String,
    val chapter: String,
    val concepts: List<String>,
    val difficulty: String,
    val examTarget: String,
    val voiceId: String,
    val voiceName: String,
    val questionCount: Int,
    val durationMinutes: Int,
    val isGhostMode: Boolean = false,
    val ghostConceptGaps: List<String> = emptyList()
) : Parcelable {
    val durationSeconds: Int get() = durationMinutes * 60
}
