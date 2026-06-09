package com.prolearn.spar.ui.screens.arena

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.prolearn.spar.ui.components.navigation.MainTab
import com.prolearn.spar.ui.components.navigation.ProLearnBottomNav
import com.prolearn.spar.ui.screens.arena.battleground.BattlegroundScreen

private enum class ArenaPage { Home, Battleground, Challenges, Games }

@Composable
fun ArenaScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var page by rememberSaveable { mutableStateOf(ArenaPage.Home) }
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(enabled = page != ArenaPage.Home) {
        page = ArenaPage.Home
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = PageBg,
        bottomBar = {
            if (page == ArenaPage.Home) {
                ProLearnBottomNav(
                    selected = MainTab.Arena,
                    onHome = onNavigateToHome,
                    onArena = { page = ArenaPage.Home },
                    onProfile = onNavigateToProfile
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(PageBg, Color(0xFFFBFAF5), Color(0xFFF1EFE8)),
                        start = Offset.Zero,
                        end = Offset(900f, 1600f)
                    )
                )
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            ArenaAmbientBackdrop()
            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    val direction = if (targetState.ordinal >= initialState.ordinal) {
                        AnimatedContentTransitionScope.SlideDirection.Left
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.Right
                    }
                    (slideIntoContainer(
                        direction,
                        animationSpec = tween(340, easing = FastOutSlowInEasing)
                    ) + fadeIn(tween(180))) togetherWith
                        (slideOutOfContainer(
                            direction,
                            animationSpec = tween(340, easing = FastOutSlowInEasing)
                        ) + fadeOut(tween(180)))
                },
                label = "arenaPageTransition"
            ) { targetPage ->
                when (targetPage) {
                    ArenaPage.Home -> ArenaHome(
                        onBattleground = { page = ArenaPage.Battleground },
                        onChallenges = { page = ArenaPage.Challenges },
                        onGames = { page = ArenaPage.Games }
                    )

                    ArenaPage.Battleground -> BattlegroundScreen(
                        onBack = { page = ArenaPage.Home },
                        snackbarHostState = snackbarHostState
                    )

                    ArenaPage.Challenges -> ChallengesScreen(
                        onBack = { page = ArenaPage.Home },
                        snackbarHostState = snackbarHostState
                    )

                    ArenaPage.Games -> GamesScreen(
                        onBack = { page = ArenaPage.Home },
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }
}
