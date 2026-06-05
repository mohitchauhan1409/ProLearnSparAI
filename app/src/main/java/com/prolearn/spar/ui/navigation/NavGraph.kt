package com.prolearn.spar.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.prolearn.spar.ui.screens.auth.LoginScreen
import com.prolearn.spar.ui.screens.auth.SignupScreen
import com.prolearn.spar.ui.screens.home.HomeScreen
import com.prolearn.spar.ui.screens.onboarding.OnboardingScreen
import com.prolearn.spar.ui.screens.profile.ProfileScreen
import com.prolearn.spar.ui.screens.progress.ProgressScreen
import com.prolearn.spar.ui.screens.report.SessionReportScreen
import com.prolearn.spar.ui.screens.setup.SparSetupScreen
import com.prolearn.spar.ui.screens.spar.LiveSparScreen
import com.prolearn.spar.ui.screens.splash.SplashScreen

@Composable
fun ProLearnNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.Splash.route) {

        // ─── Splash ───────────────────────────────────────────────────────────
        composable(Routes.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Routes.Onboarding.route) {
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

        // ─── Onboarding ───────────────────────────────────────────────────────
        composable(Routes.Onboarding.route) {
            OnboardingScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.Login.route)
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
                session = com.prolearn.spar.domain.model.Session(
                    id = "temp",
                    subject = "Physics",
                    chapter = "Kinematics",
                    difficulty = "JEE Advanced",
                    examTarget = "JEE Advanced",
                    questionCount = 8,
                    score = 72,
                    durationSeconds = 720,
                    timestamp = System.currentTimeMillis(),
                    conceptScores = listOf(
                        com.prolearn.spar.domain.model.ConceptScore("Kinematics", 85),
                        com.prolearn.spar.domain.model.ConceptScore("Newton's Laws", 60)
                    ),
                    aiInsight = "You have a solid grasp of kinematics. Focus on Newton's Laws and free body diagrams for better scores.",
                    hintsUsed = 1,
                    independentAnswers = 7
                ),
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
