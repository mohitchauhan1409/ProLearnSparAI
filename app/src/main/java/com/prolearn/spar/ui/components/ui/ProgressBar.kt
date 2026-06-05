package com.prolearn.spar.ui.components.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.prolearn.spar.ui.theme.ProLearnColors

@Composable
fun ProLearnProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(ProLearnColors.Border, RoundedCornerShape(2.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(4.dp)
                .background(ProLearnColors.Black, RoundedCornerShape(2.dp))
        )
    }
}

@Composable
fun PulsePlaceholder(modifier: Modifier = Modifier, height: Int = 16) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsePlaceholder")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulseAlpha"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .alpha(alpha)
            .background(ProLearnColors.Surface, RoundedCornerShape(4.dp))
    )
}
