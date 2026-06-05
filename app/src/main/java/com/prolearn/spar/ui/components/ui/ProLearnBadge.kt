package com.prolearn.spar.ui.components.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.ui.theme.ProLearnColors

@Composable
fun ProLearnBadge(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "badgeScale"
    )

    val bg = if (selected) ProLearnColors.Black else ProLearnColors.White
    val txtColor = if (selected) ProLearnColors.White else ProLearnColors.Black
    val bdr = if (selected) ProLearnColors.Black else ProLearnColors.Border

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(100.dp))
            .background(bg)
            .border(1.dp, bdr, RoundedCornerShape(100.dp))
            .then(if (onClick != null) Modifier.clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() } else Modifier)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = txtColor
        )
    }
}
