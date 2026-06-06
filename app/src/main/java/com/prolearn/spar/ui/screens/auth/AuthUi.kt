package com.prolearn.spar.ui.screens.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.R
import com.prolearn.spar.ui.theme.BricolageGrotesqueFamily
import com.prolearn.spar.ui.theme.ProLearnColors
import kotlinx.coroutines.delay

internal val AuthPageBg = Color(0xFFF8FAF7)
internal val AuthInk = Color(0xFF151616)
internal val AuthMoss = Color(0xFF4E7D68)
internal val AuthLimeMist = Color(0xFFEAF6D8)
internal val AuthSkyMist = Color(0xFFEAF3FF)
internal val AuthBlushMist = Color(0xFFFFEFF3)
internal val AuthLilacMist = Color(0xFFF2EEFF)
internal val AuthSoftBorder = Color(0xFFDDE5DC)
internal val AuthGlassStroke = Color(0x88FFFFFF)

@Composable
internal fun BoxScope.AuthBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "authAmbient")
    val drift by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(3200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "authAmbientDrift"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(AuthPageBg, Color(0xFFF2F7F4), Color(0xFFFFFBF5)),
                    start = Offset.Zero,
                    end = Offset(900f, 1500f)
                )
            )
    )
    Box(
        Modifier
            .size(248.dp)
            .offset(x = (-88).dp, y = (72 + drift).dp)
            .clip(CircleShape)
            .background(AuthLimeMist.copy(alpha = 0.58f))
    )
    Box(
        Modifier
            .size(220.dp)
            .align(Alignment.TopEnd)
            .offset(x = 78.dp, y = (14 - drift).dp)
            .clip(CircleShape)
            .background(AuthSkyMist.copy(alpha = 0.68f))
    )
    Box(
        Modifier
            .size(188.dp)
            .align(Alignment.BottomEnd)
            .offset(x = 54.dp, y = (-82).dp)
            .clip(CircleShape)
            .background(AuthBlushMist.copy(alpha = 0.58f))
    )
    Box(
        Modifier
            .fillMaxWidth()
            .height(190.dp)
            .align(Alignment.BottomCenter)
            .background(Brush.verticalGradient(listOf(Color.Transparent, AuthPageBg.copy(alpha = 0.86f))))
    )
}

@Composable
internal fun AuthBackButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .shadow(10.dp, CircleShape, ambientColor = AuthMoss.copy(alpha = 0.12f), spotColor = AuthMoss.copy(alpha = 0.12f))
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.76f))
            .border(1.dp, AuthGlassStroke, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Default.ArrowBack,
            contentDescription = "Back",
            tint = AuthInk,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
internal fun AnimatedAuthBlock(
    index: Int,
    content: @Composable () -> Unit
) {
    val alpha = remember { Animatable(0f) }
    val y = remember { Animatable(24f) }

    LaunchedEffect(Unit) {
        delay(index * 80L)
        alpha.animateTo(1f, tween(460, easing = FastOutSlowInEasing))
        y.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 180f))
    }

    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha.value
            translationY = y.value
        }
    ) {
        content()
    }
}

@Composable
internal fun AuthHero(
    eyebrow: String,
    title: String,
    subtitle: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(Color.White.copy(alpha = 0.72f))
                .border(1.dp, AuthGlassStroke, RoundedCornerShape(100.dp))
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AutoAwesome, null, tint = AuthMoss, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                eyebrow,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ProLearnColors.MutedDark,
                fontFamily = BricolageGrotesqueFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            title,
            fontSize = 39.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Italic,
            color = AuthInk,
            textAlign = TextAlign.Center,
            fontFamily = BricolageGrotesqueFamily
        )
        Spacer(Modifier.height(10.dp))
        Text(
            subtitle,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = ProLearnColors.MutedDark,
            textAlign = TextAlign.Center,
            fontFamily = BricolageGrotesqueFamily,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
    }
}

@Composable
internal fun AuthGlassPanel(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(22.dp, RoundedCornerShape(28.dp), ambientColor = AuthMoss.copy(alpha = 0.1f), spotColor = AuthMoss.copy(alpha = 0.1f))
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.76f))
            .border(1.dp, AuthGlassStroke, RoundedCornerShape(28.dp))
            .padding(14.dp)
    ) {
        content()
    }
}

@Composable
internal fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    isError: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }
    val isActive = isFocused || value.isNotEmpty()
    val progress by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "authLabelFloat"
    )
    val borderColor by animateColorAsState(
        when {
            isError -> ProLearnColors.Error
            isFocused -> AuthMoss
            else -> AuthSoftBorder
        },
        label = "authFieldBorder"
    )
    val iconFill by animateColorAsState(
        if (isFocused) AuthLimeMist.copy(alpha = 0.88f) else Color.White.copy(alpha = 0.78f),
        label = "authIconFill"
    )
    val labelTop = lerp(19.dp, 8.dp, progress)
    val labelSize = (15f - 4f * progress).sp
    val labelColor = lerp(
        ProLearnColors.Muted,
        if (isError) ProLearnColors.Error else AuthInk,
        progress
    )
    val visualTransformation = if (isPassword && !showPassword) {
        PasswordVisualTransformation()
    } else {
        VisualTransformation.None
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.72f))
            .border(if (isFocused || isError) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(18.dp))
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp)
                .size(38.dp)
                .clip(CircleShape)
                .background(iconFill)
                .border(1.dp, AuthGlassStroke, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                null,
                tint = if (isFocused) AuthMoss else ProLearnColors.MutedDark,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = label,
            fontSize = labelSize,
            fontFamily = BricolageGrotesqueFamily,
            fontWeight = if (progress > 0.5f) FontWeight.SemiBold else FontWeight.Normal,
            color = labelColor,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 62.dp, top = labelTop, end = if (isPassword) 52.dp else 16.dp)
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 62.dp, end = if (isPassword) 52.dp else 16.dp, bottom = 11.dp)
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
            textStyle = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = BricolageGrotesqueFamily,
                color = AuthInk
            ),
            singleLine = true,
            cursorBrush = SolidColor(AuthMoss),
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType)
        )
        if (isPassword && onTogglePassword != null) {
            IconButton(
                onClick = onTogglePassword,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 5.dp)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (showPassword) "Hide password" else "Show password",
                    tint = ProLearnColors.Muted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
internal fun AuthPrimaryButton(
    text: String,
    icon: ImageVector = Icons.Default.ArrowForward,
    loading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.982f else 1f,
        spring(dampingRatio = 0.72f),
        label = "authButtonScale"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(20.dp))
            .background(if (enabled) AuthInk else ProLearnColors.Disabled)
            .border(1.dp, if (enabled) AuthLimeMist.copy(alpha = 0.28f) else Color.Transparent, RoundedCornerShape(20.dp))
            .then(
                if (enabled) {
                    Modifier.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.dp,
                strokeCap = StrokeCap.Round
            )
        } else {
            Text(
                text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = BricolageGrotesqueFamily,
                color = if (enabled) Color.White else ProLearnColors.Muted
            )
            Spacer(Modifier.width(9.dp))
            Icon(
                icon,
                null,
                tint = if (enabled) Color.White else ProLearnColors.Muted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
internal fun AuthErrorBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ProLearnColors.ErrorSurface.copy(alpha = 0.92f))
            .border(1.dp, ProLearnColors.Error.copy(alpha = 0.16f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("!", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ProLearnColors.Error)
        Spacer(Modifier.width(8.dp))
        Text(
            message,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = ProLearnColors.Error,
            fontFamily = BricolageGrotesqueFamily
        )
    }
}

@Composable
internal fun AuthDivider(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(1f).height(1.dp).background(AuthSoftBorder))
        Text(
            text,
            fontSize = 12.sp,
            fontFamily = BricolageGrotesqueFamily,
            color = ProLearnColors.Muted,
            modifier = Modifier.padding(horizontal = 14.dp)
        )
        Box(Modifier.weight(1f).height(1.dp).background(AuthSoftBorder))
    }
}

@Composable
internal fun AuthSecondaryButton(
    text: String,
    leading: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val lift by animateDpAsState(if (pressed) 1.dp else 0.dp, spring(), label = "secondaryLift")
    Row(
        modifier = Modifier
            .offset(y = lift)
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.76f))
            .border(1.dp, AuthSoftBorder, RoundedCornerShape(18.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(AuthSkyMist.copy(alpha = 0.82f)),
            contentAlignment = Alignment.Center
        ) {
            Text(leading, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AuthInk)
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = BricolageGrotesqueFamily,
            color = AuthInk
        )
    }
}

@Composable
internal fun AuthGoogleButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.982f else 1f,
        spring(dampingRatio = 0.72f),
        label = "googleButtonScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.82f))
            .border(1.dp, AuthSoftBorder, RoundedCornerShape(18.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.icons8_google),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(11.dp))
        Text(
            "Continue with Google",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = BricolageGrotesqueFamily,
            color = AuthInk
        )
    }
}

@Composable
internal fun AuthFeatureStrip(items: List<Pair<ImageVector, String>>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEachIndexed { index, item ->
            val fill = listOf(AuthLimeMist, AuthSkyMist, AuthBlushMist)[index % 3]
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(fill.copy(alpha = 0.62f))
                    .border(1.dp, Color.White.copy(alpha = 0.86f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(item.first, null, tint = AuthMoss, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(5.dp))
                Text(
                    item.second,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AuthInk,
                    fontFamily = BricolageGrotesqueFamily,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun PasswordStrengthMeter(
    password: String,
    fraction: Float,
    label: String
) {
    if (password.isEmpty()) return
    val animated by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(320, easing = FastOutSlowInEasing),
        label = "passwordStrength"
    )
    val color = when {
        fraction < 0.4f -> Color(0xFFE24B4A)
        fraction < 0.8f -> Color(0xFF99703F)
        else -> AuthMoss
    }
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(AuthSoftBorder.copy(alpha = 0.7f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animated)
                    .height(7.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(color)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            fontSize = 12.sp,
            color = ProLearnColors.MutedDark,
            fontWeight = FontWeight.SemiBold,
            fontFamily = BricolageGrotesqueFamily,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}

@Composable
internal fun AuthExamChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else if (selected) 1.03f else 1f, spring(dampingRatio = 0.72f), label = "examChipScale")
    val fill by animateColorAsState(if (selected) AuthMoss.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.72f), label = "examFill")
    val border by animateColorAsState(if (selected) AuthMoss else AuthSoftBorder, label = "examBorder")

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(100.dp))
            .background(fill)
            .border(if (selected) 1.7.dp else 1.dp, border, RoundedCornerShape(100.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text,
            fontSize = 13.sp,
            fontFamily = BricolageGrotesqueFamily,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            color = if (selected) AuthInk else ProLearnColors.MutedDark
        )
    }
}

internal fun defaultAuthFeatureItems(): List<Pair<ImageVector, String>> = listOf(
    Icons.Default.AutoAwesome to "AI tutor",
    Icons.Default.GraphicEq to "Progress",
    Icons.Default.Lock to "Private"
)

internal fun authPersonIcon(): ImageVector = Icons.Default.Person
internal fun authMailIcon(): ImageVector = Icons.Default.Mail
internal fun authLockIcon(): ImageVector = Icons.Default.Lock
