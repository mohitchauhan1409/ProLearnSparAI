package com.prolearn.spar.ui.screens.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.prolearn.spar.ui.theme.BricolageGrotesqueFamily
import com.prolearn.spar.ui.theme.ProLearnColors
import kotlin.math.roundToInt

@Composable
fun LoginScreen(
    onNavigateBack: () -> Unit,
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val loginState by viewModel.loginState.collectAsState()
    val shakeAnim = remember { Animatable(0f) }

    val isLoading = loginState is AuthUiState.Loading
    val hasError = loginState is AuthUiState.Error
    val errorMessage = (loginState as? AuthUiState.Error)?.message.orEmpty()

    LaunchedEffect(loginState) {
        if (loginState is AuthUiState.Error) {
            shakeAnim.animateTo(0f, keyframes {
                durationMillis = 420
                -10f at 50
                10f at 100
                -10f at 150
                8f at 220
                -5f at 300
                0f at 420
            })
        }
        if (loginState is AuthUiState.Success) {
            onLoginSuccess()
            viewModel.resetLoginState()
        }
    }

    Box(Modifier.fillMaxSize()) {
        AuthBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AuthBackButton(onNavigateBack)
            }

            Spacer(Modifier.height(34.dp))

            AnimatedAuthBlock(index = 0) {
                AuthHero(
                    eyebrow = "Welcome back to ProLearn AI",
                    title = "Continue your edge.",
                    subtitle = "Sign in and jump straight back into voice-first study sessions that challenge how you think."
                )
            }

            Spacer(Modifier.height(22.dp))

            AnimatedAuthBlock(index = 1) {
                AuthGlassPanel {
                    Column(
                        modifier = Modifier.offset { IntOffset(shakeAnim.value.roundToInt(), 0) },
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AuthFeatureStrip(defaultAuthFeatureItems())

                        AuthTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                if (hasError) viewModel.resetLoginState()
                            },
                            label = "Email address",
                            icon = authMailIcon(),
                            keyboardType = KeyboardType.Email,
                            isError = hasError
                        )
                        AuthTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                if (hasError) viewModel.resetLoginState()
                            },
                            label = "Password",
                            icon = authLockIcon(),
                            isPassword = true,
                            showPassword = passwordVisible,
                            onTogglePassword = { passwordVisible = !passwordVisible },
                            isError = hasError
                        )

                        if (hasError && errorMessage.isNotEmpty()) {
                            AuthErrorBanner(errorMessage)
                        }

                        AuthPrimaryButton(
                            text = "Sign in",
                            icon = Icons.Default.LockOpen,
                            loading = isLoading,
                            enabled = !isLoading,
                            onClick = { viewModel.login(email, password) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            AnimatedAuthBlock(index = 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "New to ProLearn?",
                        fontSize = 14.sp,
                        fontFamily = BricolageGrotesqueFamily,
                        color = ProLearnColors.Muted
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "Create account",
                        fontSize = 14.sp,
                        fontFamily = BricolageGrotesqueFamily,
                        fontWeight = FontWeight.Bold,
                        color = AuthInk,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onNavigateToSignup
                        )
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
        }
    }
}
