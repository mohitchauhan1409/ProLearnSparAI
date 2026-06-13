package com.prolearn.spar.ui.screens.arena

import androidx.compose.runtime.Composable

@Composable
fun ClueFlipGameScreen(
    subject: String,
    difficulty: String,
    onExit: () -> Unit
) {
    ClueFlipChallenge(subject, difficulty, onExit)
}
