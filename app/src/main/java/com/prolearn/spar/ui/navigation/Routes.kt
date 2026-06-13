package com.prolearn.spar.ui.navigation

sealed class Routes(val route: String) {
    data object Splash : Routes("splash")
    data object Login : Routes("login")
    data object Signup : Routes("signup")
    data object Home : Routes("home")
    data object Arena : Routes("arena")
    data object SparSetup : Routes("spar_setup?subject={subject}&chapter={chapter}") {
        fun withArgs(subject: String? = null, chapter: String? = null): String {
            val subj = subject ?: ""
            val chap = chapter ?: ""
            return "spar_setup?subject=$subj&chapter=$chap"
        }
    }
    data object LiveSpar : Routes("live_spar")
    data object SessionReport : Routes("session_report")
    data object Progress : Routes("progress")
    data object Profile : Routes("profile")
    data object VideoLessons : Routes("video_lessons")
}
