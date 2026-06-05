package com.prolearn.spar.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.prolearn.spar.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val destination by viewModel.destination.collectAsState()

    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.6f) }

    // Start logo animation immediately — runs for the full splash duration
    LaunchedEffect(Unit) {
        launch {
            alpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        }
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }

        // Always show splash for at least 1400ms, THEN wait for the destination
        // to be resolved (DataStore read). In practice the read takes ~50-100ms
        // so the delay covers it. If for some reason it takes longer, we wait.
        delay(1400)

        // Spin here until the ViewModel has resolved a real destination.
        // This is the only place navigation is triggered — no second LaunchedEffect.
        var resolved = destination
        while (resolved == SplashDestination.LOADING) {
            delay(50)
            resolved = viewModel.destination.value
        }

        when (resolved) {
            SplashDestination.SHOW_ONBOARDING -> onNavigateToOnboarding()
            SplashDestination.SHOW_HOME       -> onNavigateToHome()
            SplashDestination.SHOW_LOGIN      -> onNavigateToLogin()
            SplashDestination.LOADING         -> { /* unreachable */ }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.prolearn_logo),
            contentDescription = "ProLearn",
            modifier = Modifier
                .fillMaxWidth()
                .alpha(alpha.value)
                .scale(scale.value)
        )
    }
}
