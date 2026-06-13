package com.prolearn.spar.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.prolearn.spar.ui.screens.arena.ArenaScreen
import com.prolearn.spar.ui.screens.auth.LoginScreen
import com.prolearn.spar.ui.screens.auth.SignupScreen
import com.prolearn.spar.ui.screens.home.HomeScreen
import com.prolearn.spar.ui.screens.profile.ProfileScreen
import com.prolearn.spar.ui.screens.progress.ProgressScreen
import com.prolearn.spar.ui.screens.report.SessionReportScreen
import com.prolearn.spar.ui.screens.setup.SparSetupScreen
import com.prolearn.spar.ui.screens.spar.LiveSparScreen
import com.prolearn.spar.ui.screens.splash.SplashScreen
import com.prolearn.spar.ui.screens.video.VideoGeneratorScreen

@Composable
fun ProLearnNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route,
        enterTransition = { premiumEnterTransition() },
        exitTransition = { premiumExitTransition() },
        popEnterTransition = { premiumPopEnterTransition() },
        popExitTransition = { premiumPopExitTransition() }
    ) {

        // ─── Splash ───────────────────────────────────────────────────────────
        composable(Routes.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ─── Login ────────────────────────────────────────────────────────────
        composable(Routes.Login.route) {
            LoginScreen(
                onNavigateBack = { navController.popBackStack() },
                onLoginSuccess = {
                    navController.navigate(Routes.Home.route) {
                        // Clear entire back stack — no going back to login after login
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Routes.Signup.route)
                }
            )
        }

        // ─── Signup ───────────────────────────────────────────────────────────
        composable(Routes.Signup.route) {
            SignupScreen(
                onNavigateBack = { navController.popBackStack() },
                onSignupSuccess = {
                    navController.navigate(Routes.Home.route) {
                        // Clear entire back stack — no going back to signup after account creation
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ─── Home ─────────────────────────────────────────────────────────────
        composable(Routes.Home.route) {
            HomeScreen(
                onNavigateToSparSetup = { subject ->
                    navController.navigate(Routes.SparSetup.withArgs(subject = subject))
                },
                onNavigateToProgress = {
                    navController.navigate(Routes.Progress.route) {
                        popUpTo(Routes.Home.route) { saveState = true }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Routes.Profile.route) {
                        popUpTo(Routes.Home.route) { saveState = true }
                    }
                },
                onNavigateToArena = {
                    navController.navigate(Routes.Arena.route) {
                        popUpTo(Routes.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToVideoLessons = {
                    navController.navigate(Routes.VideoLessons.route)
                }
            )
        }

        // ─── Video Lessons ────────────────────────────────────────────────────
        composable(Routes.VideoLessons.route) {
            VideoGeneratorScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ─── Arena ───────────────────────────────────────────────────────────
        composable(Routes.Arena.route) {
            ArenaScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Routes.Profile.route) {
                        popUpTo(Routes.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // ─── SparSetup ────────────────────────────────────────────────────────
        composable(
            route = Routes.SparSetup.route,
            arguments = listOf(
                navArgument("subject") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("chapter") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            SparSetupScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartSpar = { config ->
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("sparConfig", config)
                    navController.navigate(Routes.LiveSpar.route)
                }
            )
        }

        // ─── LiveSpar ─────────────────────────────────────────────────────────
        composable(Routes.LiveSpar.route) {
            val config = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<com.prolearn.spar.domain.model.SparConfig>("sparConfig")
            if (config != null) {
                LiveSparScreen(
                    config = config,
                    onNavigateToReport = {
                        navController.navigate(Routes.SessionReport.route) {
                            popUpTo(Routes.Home.route)
                        }
                    }
                )
            }
        }

        // ─── SessionReport ────────────────────────────────────────────────────
        composable(Routes.SessionReport.route) {
            SessionReportScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // ─── Progress ─────────────────────────────────────────────────────────
        composable(Routes.Progress.route) {
            ProgressScreen()
        }

        // ─── Profile ──────────────────────────────────────────────────────────
        composable(Routes.Profile.route) {
            ProfileScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Home.route) { inclusive = true }
                    }
                },
                onNavigateToProgress = {
                    navController.navigate(Routes.Progress.route)
                },
                onNavigateToArena = {
                    navController.navigate(Routes.Arena.route) {
                        popUpTo(Routes.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    // Clear the entire back stack and restart from Splash
                    // This simulates a fresh app open for the now-logged-out user
                    navController.navigate(Routes.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

private const val NavMotionDuration = 360

private fun motionSpec() = tween<IntOffset>(
    durationMillis = NavMotionDuration,
    easing = FastOutSlowInEasing
)

private fun alphaSpec() = tween<Float>(
    durationMillis = 220,
    easing = FastOutSlowInEasing
)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.premiumEnterTransition(): EnterTransition {
    val direction = if (targetDepth() >= initialDepth()) {
        AnimatedContentTransitionScope.SlideDirection.Left
    } else {
        AnimatedContentTransitionScope.SlideDirection.Right
    }
    return slideIntoContainer(direction, animationSpec = motionSpec()) + fadeIn(alphaSpec())
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.premiumExitTransition(): ExitTransition {
    val direction = if (targetDepth() >= initialDepth()) {
        AnimatedContentTransitionScope.SlideDirection.Left
    } else {
        AnimatedContentTransitionScope.SlideDirection.Right
    }
    return slideOutOfContainer(direction, animationSpec = motionSpec()) + fadeOut(alphaSpec())
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.premiumPopEnterTransition(): EnterTransition {
    return slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = motionSpec()
    ) + fadeIn(alphaSpec())
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.premiumPopExitTransition(): ExitTransition {
    return slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = motionSpec()
    ) + fadeOut(alphaSpec())
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.initialDepth(): Int {
    return routeDepth(initialState.destination.route)
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.targetDepth(): Int {
    return routeDepth(targetState.destination.route)
}

private fun routeDepth(route: String?): Int {
    return when (route?.substringBefore("?")) {
        Routes.Splash.route -> 0
        Routes.Login.route -> 1
        Routes.Signup.route -> 2
        Routes.Home.route -> 10
        Routes.Arena.route -> 11
        Routes.Profile.route -> 12
        Routes.Progress.route -> 13
        Routes.VideoLessons.route -> 14
        Routes.SparSetup.route.substringBefore("?") -> 20
        Routes.LiveSpar.route -> 21
        Routes.SessionReport.route -> 22
        else -> 10
    }
}
