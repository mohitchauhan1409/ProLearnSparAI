package com.prolearn.spar.ui.components.spar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.prolearn.spar.ui.theme.ProLearnColors
import kotlinx.coroutines.delay

@Composable
fun ScoreRing(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 8.dp,
    animateNumbers: Boolean = true
) {
    val animatedScore = remember { Animatable(0f) }
    var displayValue by remember { mutableIntStateOf(0) }

    LaunchedEffect(score) {
        animatedScore.animateTo(
            score.toFloat(),
            animationSpec = tween(1200, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        val duration = 600
        val steps = 30
        repeat(steps) { i ->
            displayValue = (score * (i + 1) / steps)
            delay((duration / steps).toLong())
        }
        displayValue = score
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val radius = (size.toPx() / 2) - strokePx
            val topLeft = Offset(strokePx, strokePx)
            val arcSize = Size(radius * 2, radius * 2)

            drawArc(
                color = ProLearnColors.Border,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(strokePx, cap = StrokeCap.Round)
            )

            drawArc(
                color = ProLearnColors.Black,
                startAngle = -90f,
                sweepAngle = 360f * (animatedScore.value / 100f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(strokePx, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${displayValue}%",
                style = MaterialTheme.typography.displayLarge,
                color = ProLearnColors.Black
            )
            Text(
                text = "score",
                style = MaterialTheme.typography.labelSmall,
                color = ProLearnColors.Muted
            )
        }
    }
}
