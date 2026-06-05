package com.prolearn.spar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.prolearn.spar.ui.navigation.ProLearnNavGraph
import com.prolearn.spar.ui.theme.ProLearnColors
import com.prolearn.spar.ui.theme.ProLearnTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Must be called before super.onCreate — replaces the splash theme with the app theme
        // and eliminates the white window flash on launch
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProLearnTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ProLearnColors.White
                ) {
                    val navController = rememberNavController()
                    ProLearnNavGraph(navController)
                }
            }
        }
    }
}
