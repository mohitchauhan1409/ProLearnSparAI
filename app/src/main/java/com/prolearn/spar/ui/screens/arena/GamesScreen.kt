package com.prolearn.spar.ui.screens.arena

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.SnackbarHostState

private enum class GamesPage { Home, ClueFlip, XpSpinwheel }

@Composable
fun GamesScreen(
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var page by rememberSaveable { mutableStateOf(GamesPage.Home) }
    var selectedSubject by rememberSaveable { mutableStateOf("Physics") }
    var selectedDifficulty by rememberSaveable { mutableStateOf("Medium") }

    AnimatedContent(
        targetState = page,
        transitionSpec = {
            if (targetState.ordinal > initialState.ordinal) {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(260)
                ) togetherWith slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(260)
                )
            } else {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(260)
                ) togetherWith slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(260)
                )
            }
        },
        label = "gamesPageTransition"
    ) { activePage ->
        when (activePage) {
            GamesPage.Home -> GamesHomeScreen(
                snackbarHostState = snackbarHostState,
                selectedSubject = selectedSubject,
                onSubject = { selectedSubject = it },
                selectedDifficulty = selectedDifficulty,
                onDifficulty = { selectedDifficulty = it },
                onBack = onBack,
                onClueFlip = { page = GamesPage.ClueFlip },
                onXpSpinwheel = { page = GamesPage.XpSpinwheel }
            )

            GamesPage.ClueFlip -> ClueFlipGameScreen(
                subject = selectedSubject,
                difficulty = selectedDifficulty,
                onExit = { page = GamesPage.Home }
            )

            GamesPage.XpSpinwheel -> XpSpinwheelGameScreen(
                subject = selectedSubject,
                onExit = { page = GamesPage.Home }
            )
        }
    }
}
