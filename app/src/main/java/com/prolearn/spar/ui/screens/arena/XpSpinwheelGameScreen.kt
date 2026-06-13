package com.prolearn.spar.ui.screens.arena

import androidx.compose.runtime.Composable

@Composable
fun XpSpinwheelGameScreen(
    subject: String,
    onExit: () -> Unit
) {
    XpSpinWheel(subject, onExit)
}
