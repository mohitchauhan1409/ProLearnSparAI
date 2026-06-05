package com.prolearn.spar.ui.screens.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.prolearn.spar.ui.components.ui.FloatingLabelTextField
import com.prolearn.spar.ui.theme.BricolageGrotesqueFamily
import com.prolearn.spar.ui.theme.ProLearnColors
import kotlinx.coroutines.delay
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
    val errorMessage = (loginState as? AuthUiState.Error)?.message ?: ""

    // Staggered entrance animations
    val headerAlpha = remember { Animatable(0f) }
    val headerOffset = remember { Animatable(24f) }
    val formAlpha = remember { Animatable(0f) }
    val formOffset = remember { Animatable(32f) }
    val footerAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        headerAlpha.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
        headerOffset.animateTo(0f, tween(420, easing = FastOutSlowInEasing))
        delay(80)
        formAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        formOffset.animateTo(0f, tween(400, easing = FastOutSlowInEasing))
        delay(60)
        footerAlpha.animateTo(1f, tween(300))
    }

    // Shake on error
    LaunchedEffect(loginState) {
        if (loginState is AuthUiState.Error) {
            shakeAnim.animateTo(0f, keyframes {
                durationMillis = 400
                -10f at 50; 10f at 100; -10f at 150; 10f at 200
                -5f at 270; 5f at 330; 0f at 400
            })
        }
        if (loginState is AuthUiState.Success) {
            onLoginSuccess()
            viewModel.resetLoginState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ProLearnColors.White)
    ) {
        // Ambient depth gradients
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x0D111111), Color(0x00111111)),
                    center = Offset(size.width * 0.85f, size.height * 0.05f),
                    radius = size.width * 0.6f
                ),
                center = Offset(size.width * 0.85f, size.height * 0.05f),
                radius = size.width * 0.6f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x08111111), Color(0x00111111)),
                    center = Offset(size.width * 0.1f, size.height * 0.82f),
                    radius = size.width * 0.55f
                ),
                center = Offset(size.width * 0.1f, size.height * 0.82f),
                radius = size.width * 0.55f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 56.dp, bottom = 32.dp)
        ) {
            // Back button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(ProLearnColors.Surface)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onNavigateBack
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = ProLearnColors.Black,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.height(44.dp))

            // Header
            Column(
                modifier = Modifier
                    .alpha(headerAlpha.value)
                    .offset { IntOffset(0, headerOffset.value.roundToInt()) }
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(ProLearnColors.Surface)
                        .border(1.dp, ProLearnColors.Border, RoundedCornerShape(100.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "✦  ProLearn",
                        fontSize = 12.sp,
                        fontFamily = BricolageGrotesqueFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = ProLearnColors.MutedDark,
                        letterSpacing = 0.3.sp
                    )
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "Welcome back.",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = BricolageGrotesqueFamily,
                    color = ProLearnColors.Black,
                    letterSpacing = (-1.5).sp,
                    lineHeight = 38.sp
                )
                Spacer(Modifier.height(7.dp))
                Text(
                    text = "Sign in to continue sparring.",
                    fontSize = 15.sp,
                    fontFamily = BricolageGrotesqueFamily,
                    color = ProLearnColors.Muted,
                    letterSpacing = (-0.2).sp
                )
            }

            Spacer(Modifier.height(36.dp))

            // Form
            Column(
                modifier = Modifier
                    .alpha(formAlpha.value)
                    .offset { IntOffset(0, formOffset.value.roundToInt()) }
            ) {
                Column(
                    modifier = Modifier.offset { IntOffset(shakeAnim.value.roundToInt(), 0) },
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FloatingLabelTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (hasError) viewModel.resetLoginState()
                        },
                        label = "Email address",
                        isError = hasError
                    )
                    FloatingLabelTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (hasError) viewModel.resetLoginState()
                        },
                        label = "Password",
                        isPassword = true,
                        showPassword = passwordVisible,
                        onTogglePassword = { passwordVisible = !passwordVisible },
                        isError = hasError
                    )
                }

                // Error message
                if (hasError && errorMessage.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⚠", fontSize = 12.sp, color = ProLearnColors.Error)
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = errorMessage,
                            fontSize = 13.sp,
                            fontFamily = BricolageGrotesqueFamily,
                            color = ProLearnColors.Error
                        )
                    }
                } else {
                    Spacer(Modifier.height(10.dp))
                }

                // Forgot password
                Text(
                    text = "Forgot password?",
                    fontSize = 13.sp,
                    fontFamily = BricolageGrotesqueFamily,
                    fontWeight = FontWeight.Medium,
                    color = ProLearnColors.MutedDark,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                )

                Spacer(Modifier.height(24.dp))

                // Sign in CTA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isLoading) ProLearnColors.Disabled else ProLearnColors.Black)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = !isLoading
                        ) {
                            viewModel.login(email, password)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = ProLearnColors.White,
                            strokeWidth = 2.dp,
                            strokeCap = StrokeCap.Round
                        )
                    } else {
                        Text(
                            text = "Sign in",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = BricolageGrotesqueFamily,
                            color = ProLearnColors.White,
                            letterSpacing = (-0.1).sp
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.weight(1f).height(1.dp).background(ProLearnColors.Border))
                    Text(
                        text = "or continue with",
                        fontSize = 12.sp,
                        fontFamily = BricolageGrotesqueFamily,
                        color = ProLearnColors.Muted,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    Box(Modifier.weight(1f).height(1.dp).background(ProLearnColors.Border))
                }

                Spacer(Modifier.height(16.dp))

                // Google button — shell (no-op)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ProLearnColors.White)
                        .border(1.dp, ProLearnColors.Border, RoundedCornerShape(14.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "G",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = BricolageGrotesqueFamily,
                            color = ProLearnColors.Black
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Continue with Google",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = BricolageGrotesqueFamily,
                            color = ProLearnColors.Black
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(footerAlpha.value),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account?",
                    fontSize = 14.sp,
                    fontFamily = BricolageGrotesqueFamily,
                    color = ProLearnColors.Muted
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Sign up",
                    fontSize = 14.sp,
                    fontFamily = BricolageGrotesqueFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = ProLearnColors.Black,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onNavigateToSignup
                    )
                )
            }
        }
    }
}
