package com.prolearn.spar.ui.screens.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.scale
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
    val exams = listOf("JEE Mains", "JEE Advanced", "NEET", "CUET")

    val signupState by viewModel.signupState.collectAsState()
    val isLoading = signupState is AuthUiState.Loading
    val hasError = signupState is AuthUiState.Error
    val errorMessage = (signupState as? AuthUiState.Error)?.message ?: ""

    // Password strength
    val strengthFraction = when {
        password.isEmpty() -> 0f
        password.length < 6 -> 0.28f
        password.length in 6..10 -> if (password.any { !it.isLetterOrDigit() }) 1f else 0.6f
        else -> 1f
    }
    val strengthLabel = when {
        password.isEmpty() -> ""
        password.length < 6 -> "Weak password"
        password.length in 6..10 -> if (password.any { !it.isLetterOrDigit() }) "Strong password" else "Fair password"
        else -> "Strong password"
    }
    val strengthBarAlpha = when {
        password.isEmpty() -> 0f
        password.length < 6 -> 0.35f
        password.length in 6..10 -> if (password.any { !it.isLetterOrDigit() }) 1f else 0.65f
        else -> 1f
    }
    val strengthBarAnim = remember { Animatable(0f) }
    LaunchedEffect(password) {
        strengthBarAnim.animateTo(strengthFraction, tween(320, easing = FastOutSlowInEasing))
    }

    // Staggered entrance
    val headerAlpha = remember { Animatable(0f) }
    val headerOffset = remember { Animatable(24f) }
    val formAlpha = remember { Animatable(0f) }
    val formOffset = remember { Animatable(32f) }
    LaunchedEffect(Unit) {
        headerAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        headerOffset.animateTo(0f, tween(400, easing = FastOutSlowInEasing))
        delay(80)
        formAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        formOffset.animateTo(0f, tween(400, easing = FastOutSlowInEasing))
    }

    // Navigate on success
    LaunchedEffect(signupState) {
        if (signupState is AuthUiState.Success) {
            onSignupSuccess()
            viewModel.resetSignupState()
        }
    }

    val canSubmit = name.isNotBlank() && email.isNotBlank() && password.length >= 6 && !isLoading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ProLearnColors.White)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x0D111111), Color(0x00111111)),
                    center = Offset(size.width * 0.15f, size.height * 0.06f),
                    radius = size.width * 0.5f
                ),
                center = Offset(size.width * 0.15f, size.height * 0.06f),
                radius = size.width * 0.5f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x08111111), Color(0x00111111)),
                    center = Offset(size.width * 0.9f, size.height * 0.88f),
                    radius = size.width * 0.55f
                ),
                center = Offset(size.width * 0.9f, size.height * 0.88f),
                radius = size.width * 0.55f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 56.dp, bottom = 44.dp)
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
                    text = "Create account.",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = BricolageGrotesqueFamily,
                    color = ProLearnColors.Black,
                    letterSpacing = (-1.5).sp,
                    lineHeight = 38.sp
                )
                Spacer(Modifier.height(7.dp))
                Text(
                    text = "Set up your profile to start sparring.",
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
                    .offset { IntOffset(0, formOffset.value.roundToInt()) },
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingLabelTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (hasError) viewModel.resetSignupState()
                    },
                    label = "Full name"
                )
                FloatingLabelTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (hasError) viewModel.resetSignupState()
                    },
                    label = "Email address",
                    isError = hasError
                )
                FloatingLabelTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (hasError) viewModel.resetSignupState()
                    },
                    label = "Password",
                    isPassword = true,
                    showPassword = passwordVisible,
                    onTogglePassword = { passwordVisible = !passwordVisible }
                )

                // Password strength bar
                if (password.isNotEmpty()) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(ProLearnColors.Border)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(strengthBarAnim.value)
                                    .height(2.dp)
                                    .background(
                                        ProLearnColors.Black.copy(alpha = strengthBarAlpha),
                                        RoundedCornerShape(1.dp)
                                    )
                            )
                        }
                        Spacer(Modifier.height(5.dp))
                        Text(
                            text = strengthLabel,
                            fontSize = 12.sp,
                            fontFamily = BricolageGrotesqueFamily,
                            fontWeight = FontWeight.Medium,
                            color = ProLearnColors.MutedDark,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }

            // Error message
            if (hasError && errorMessage.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.alpha(formAlpha.value),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "⚠", fontSize = 12.sp, color = ProLearnColors.Error)
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = errorMessage,
                        fontSize = 13.sp,
                        fontFamily = BricolageGrotesqueFamily,
                        color = ProLearnColors.Error,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // Exam target
            Column(modifier = Modifier.alpha(formAlpha.value)) {
                Text(
                    text = "EXAM TARGET",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = BricolageGrotesqueFamily,
                    color = ProLearnColors.Muted,
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    exams.forEach { exam ->
                        ExamChip(
                            text = exam,
                            selected = selectedExam == exam,
                            onClick = { selectedExam = exam }
                        )
                    }
                }
            }

            Spacer(Modifier.height(36.dp))

            // CTA
            Box(
                modifier = Modifier
                    .alpha(formAlpha.value)
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (canSubmit) ProLearnColors.Black else ProLearnColors.Disabled)
                    .then(
                        if (canSubmit) Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            viewModel.signup(name, email, password, selectedExam)
                        } else Modifier
                    ),
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
                        text = "Create account",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = BricolageGrotesqueFamily,
                        color = if (canSubmit) ProLearnColors.White else ProLearnColors.Muted,
                        letterSpacing = (-0.1).sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "By continuing, you agree to ProLearn's Terms of Service and Privacy Policy.",
                fontSize = 12.sp,
                fontFamily = BricolageGrotesqueFamily,
                color = ProLearnColors.Muted,
                lineHeight = 18.sp,
                modifier = Modifier
                    .alpha(formAlpha.value)
                    .padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun ExamChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "chipScale"
    )
    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(100.dp))
            .background(if (selected) ProLearnColors.Black else ProLearnColors.White)
            .border(
                1.dp,
                if (selected) ProLearnColors.Black else ProLearnColors.Border,
                RoundedCornerShape(100.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontFamily = BricolageGrotesqueFamily,
            fontWeight = FontWeight.Medium,
            color = if (selected) ProLearnColors.White else ProLearnColors.MutedDark
        )
    }
}
