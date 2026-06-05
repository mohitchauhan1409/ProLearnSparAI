package com.prolearn.spar.ui.components.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.ui.theme.ProLearnColors
import com.prolearn.spar.ui.theme.ProLearnShapes

@Composable
fun ProLearnButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = ProLearnColors.Black,
    textColor: Color = ProLearnColors.White,
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 1.5.dp,
    height: Int = 52
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "buttonScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .scale(scale)
            .clip(ProLearnShapes.md)
            .background(if (enabled) backgroundColor else ProLearnColors.Disabled)
            .then(
                if (borderColor != Color.Transparent)
                    Modifier.border(borderWidth, borderColor, ProLearnShapes.md)
                else Modifier
            )
            .then(
                if (enabled) Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ) else Modifier
            )
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) textColor else ProLearnColors.Muted
        )
    }
}
