package com.prolearn.spar.ui.components.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.ui.theme.BricolageGrotesqueFamily
import com.prolearn.spar.ui.theme.ProLearnColors

@Composable
fun FloatingLabelTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    isError: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }
    val isActive = isFocused || value.isNotEmpty()

    // 0f = resting placeholder, 1f = floated label
    val floatProgress by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "labelFloat"
    )

    val borderColor = when {
        isError -> ProLearnColors.Error
        isFocused -> ProLearnColors.Black
        else -> ProLearnColors.Border
    }
    val borderWidth = if (isFocused || isError) 1.5.dp else 1.dp

    val visualTransformation = if (isPassword && !showPassword)
        PasswordVisualTransformation() else VisualTransformation.None

    // Label font size: 15sp → 11sp
    val labelFontSize = (15f - 4f * floatProgress).sp

    // Label top padding: 19dp → 8dp
    val labelTopPadding = lerp(19.dp, 8.dp, floatProgress)

    // Label color: Muted → Black (or Error)
    val labelColor = lerp(
        ProLearnColors.Muted,
        if (isError) ProLearnColors.Error else ProLearnColors.Black,
        floatProgress
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ProLearnColors.Surface)
            .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
    ) {
        // Floating label
        Text(
            text = label,
            fontSize = labelFontSize,
            fontFamily = BricolageGrotesqueFamily,
            fontWeight = if (floatProgress > 0.5f) FontWeight.Medium else FontWeight.Normal,
            color = labelColor,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = labelTopPadding)
        )

        // Text input — anchored to the bottom half
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = 16.dp,
                    end = if (isPassword) 52.dp else 16.dp,
                    bottom = 11.dp
                )
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
            textStyle = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = BricolageGrotesqueFamily,
                color = ProLearnColors.Black,
                letterSpacing = (-0.1).sp
            ),
            singleLine = true,
            cursorBrush = SolidColor(ProLearnColors.Black),
            visualTransformation = visualTransformation,
            keyboardOptions = if (isPassword)
                KeyboardOptions(keyboardType = KeyboardType.Password)
            else KeyboardOptions.Default
        )

        // Password visibility toggle
        if (isPassword && onTogglePassword != null) {
            IconButton(
                onClick = onTogglePassword,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (showPassword) "Hide" else "Show",
                    tint = ProLearnColors.Muted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
