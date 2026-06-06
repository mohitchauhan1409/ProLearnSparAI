package com.prolearn.spar.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.prolearn.spar.ui.theme.BricolageGrotesqueFamily
import com.prolearn.spar.ui.theme.ProLearnColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SignupScreen(
    onNavigateBack: () -> Unit,
    onSignupSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedExam by remember { mutableStateOf("JEE Advanced") }
    val exams = remember { listOf("JEE Mains", "JEE Advanced", "NEET", "CUET") }

    val signupState by viewModel.signupState.collectAsState()
    val isLoading = signupState is AuthUiState.Loading
    val hasError = signupState is AuthUiState.Error
    val errorMessage = (signupState as? AuthUiState.Error)?.message.orEmpty()

    val strengthFraction = when {
        password.isEmpty() -> 0f
        password.length < 6 -> 0.28f
        password.length in 6..10 -> if (password.any { !it.isLetterOrDigit() }) 0.88f else 0.62f
        else -> 1f
    }
    val strengthLabel = when {
        password.isEmpty() -> ""
        password.length < 6 -> "Weak password"
        password.length in 6..10 -> if (password.any { !it.isLetterOrDigit() }) "Strong password" else "Fair password"
        else -> "Strong password"
    }
    val canSubmit = name.isNotBlank() && email.isNotBlank() && password.length >= 6 && !isLoading

    LaunchedEffect(signupState) {
        if (signupState is AuthUiState.Success) {
            onSignupSuccess()
            viewModel.resetSignupState()
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
                .padding(horizontal = 18.dp, vertical = 14.dp)
                .padding(bottom = 184.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AuthBackButton(onNavigateBack)
            }

            Spacer(Modifier.height(28.dp))

            AnimatedAuthBlock(index = 0) {
                AuthHero(
                    eyebrow = "Build your study profile",
                    title = "Start studying smarter.",
                    subtitle = "Create your account, choose your exam target, and let ProLearn AI tune each session around your goals."
                )
            }

            Spacer(Modifier.height(22.dp))

            AnimatedAuthBlock(index = 1) {
                AuthGlassPanel {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        AuthFeatureStrip(defaultAuthFeatureItems())

                        AuthTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                if (hasError) viewModel.resetSignupState()
                            },
                            label = "Full name",
                            icon = authPersonIcon()
                        )
                        AuthTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                if (hasError) viewModel.resetSignupState()
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
                                if (hasError) viewModel.resetSignupState()
                            },
                            label = "Password",
                            icon = authLockIcon(),
                            isPassword = true,
                            showPassword = passwordVisible,
                            onTogglePassword = { passwordVisible = !passwordVisible },
                            isError = hasError
                        )

                        PasswordStrengthMeter(
                            password = password,
                            fraction = strengthFraction,
                            label = strengthLabel
                        )

                        if (hasError && errorMessage.isNotEmpty()) {
                            AuthErrorBanner(errorMessage)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            AnimatedAuthBlock(index = 2) {
                AuthGlassPanel {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Exam target",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = AuthInk,
                                fontFamily = BricolageGrotesqueFamily
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                "Personalizes sessions",
                                fontSize = 11.sp,
                                color = ProLearnColors.Muted,
                                fontFamily = BricolageGrotesqueFamily
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            exams.forEach { exam ->
                                AuthExamChip(
                                    text = exam,
                                    selected = selectedExam == exam,
                                    onClick = { selectedExam = exam }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, AuthPageBg.copy(alpha = 0.94f), AuthPageBg)
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            AnimatedAuthBlock(index = 3) {
                AuthGlassPanel {
                    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        AuthPrimaryButton(
                            text = "Create account",
                            icon = Icons.Default.AutoAwesome,
                            loading = isLoading,
                            enabled = canSubmit,
                            onClick = { viewModel.signup(name, email, password, selectedExam) }
                        )
                        Text(
                            text = "By continuing, you agree to ProLearn's Terms of Service and Privacy Policy.",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = ProLearnColors.Muted,
                            fontFamily = BricolageGrotesqueFamily,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Already have an account?",
                                fontSize = 13.sp,
                                color = ProLearnColors.Muted,
                                fontFamily = BricolageGrotesqueFamily
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                "Sign in",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AuthInk,
                                fontFamily = BricolageGrotesqueFamily,
                                modifier = Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onNavigateBack
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
