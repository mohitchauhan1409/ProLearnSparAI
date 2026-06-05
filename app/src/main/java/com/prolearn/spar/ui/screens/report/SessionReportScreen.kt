package com.prolearn.spar.ui.screens.report

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.domain.model.Session
import com.prolearn.spar.ui.components.spar.ConceptBar
import com.prolearn.spar.ui.components.spar.ScoreRing
import com.prolearn.spar.ui.components.ui.ProLearnButton
import com.prolearn.spar.ui.theme.ProLearnColors
import com.prolearn.spar.ui.theme.ProLearnShapes
import kotlinx.coroutines.delay

@Composable
fun SessionReportScreen(
    session: Session,
    onNavigateToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProLearnColors.White)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        // Title
        Text(
            "Session complete",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = ProLearnColors.Black,
            letterSpacing = (-0.5).sp
        )
        Text(
            "${session.subject} \u00B7 ${session.chapter}",
            fontSize = 14.sp,
            color = ProLearnColors.Muted
        )

        Spacer(Modifier.height(32.dp))

        // Score ring
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            ScoreRing(score = session.score)
        }

        Spacer(Modifier.height(24.dp))

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("${session.questionCount}", "Questions")
            StatItem(
                "${session.durationSeconds / 60}:${(session.durationSeconds % 60).toString().padStart(2, '0')}",
                "Time"
            )
            StatItem("${session.independentAnswers}", "Without hints")
        }

        Spacer(Modifier.height(32.dp))

        // Concept breakdown
        if (session.conceptScores.isNotEmpty()) {
            Text(
                "Concept breakdown",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = ProLearnColors.Black
            )
            Spacer(Modifier.height(12.dp))
            session.conceptScores.forEachIndexed { index, score ->
                ConceptBar(concept = score, index = index)
            }
            Spacer(Modifier.height(24.dp))
        }

        // AI Insight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ProLearnColors.Surface, ProLearnShapes.md)
                .border(1.dp, ProLearnColors.Border, ProLearnShapes.md)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    "AI INSIGHT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ProLearnColors.Muted,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))

                var displayText by remember { mutableStateOf("") }
                LaunchedEffect(session.aiInsight) {
                    displayText = ""
                    session.aiInsight.forEachIndexed { i, _ ->
                        delay(20L)
                        displayText = session.aiInsight.substring(0, i + 1)
                    }
                }
                Text(
                    displayText,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = ProLearnColors.Black
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        ProLearnButton(text = "Back to home", onClick = onNavigateToHome)

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = ProLearnColors.Black)
        Text(label, fontSize = 12.sp, color = ProLearnColors.Muted)
    }
}
