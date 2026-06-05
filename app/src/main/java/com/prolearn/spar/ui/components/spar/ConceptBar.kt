package com.prolearn.spar.ui.components.spar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.prolearn.spar.domain.model.ConceptScore
import com.prolearn.spar.ui.theme.ProLearnColors
import kotlinx.coroutines.delay

@Composable
fun ConceptBar(
    concept: ConceptScore,
    index: Int = 0,
    modifier: Modifier = Modifier
) {
    val barWidth = remember { Animatable(0f) }

    LaunchedEffect(concept.score) {
        delay(index * 200L)
        barWidth.animateTo(concept.score / 100f, tween(600))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = concept.name,
            style = MaterialTheme.typography.bodyLarge,
            color = ProLearnColors.Black,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(ProLearnColors.Border)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(barWidth.value)
                    .background(ProLearnColors.Black, RoundedCornerShape(2.dp))
            )
        }
        Text(
            text = "${concept.score}%",
            style = MaterialTheme.typography.bodySmall,
            color = ProLearnColors.Muted,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
