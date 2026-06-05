package com.prolearn.spar.ui.components.spar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.prolearn.spar.ui.theme.ProLearnColors
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun VoiceWaveform(
    audioLevel: Float,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 11,
    maxBarHeight: Dp = 32.dp,
    barWidth: Dp = 3.dp
) {
    val idleTransition = rememberInfiniteTransition(label = "idleTransition")
    val idleScale by idleTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "idleScale"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(barCount) { i ->
            val heightAnim = remember { Animatable(3f) }

            LaunchedEffect(audioLevel, isActive) {
                val targetHeight = if (isActive) {
                    val phaseOffset = abs(sin(i.toFloat())) * 0.5f
                    ((audioLevel * maxBarHeight.value) * (0.5f + phaseOffset)).coerceAtLeast(3f)
                } else {
                    3f * idleScale
                }
                heightAnim.animateTo(targetHeight, spring(dampingRatio = 0.6f, stiffness = 200f))
            }

            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(heightAnim.value.dp)
                    .background(
                        if (isActive) ProLearnColors.WaveActive else ProLearnColors.WaveIdle,
                        RoundedCornerShape(100)
                    )
            )
            if (i < barCount - 1) {
                Box(modifier = Modifier.width(2.dp))
            }
        }
    }
}
