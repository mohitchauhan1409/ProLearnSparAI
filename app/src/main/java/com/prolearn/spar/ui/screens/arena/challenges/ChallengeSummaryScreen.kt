package com.prolearn.spar.ui.screens.arena.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.ui.screens.arena.ArenaHeader
import com.prolearn.spar.ui.screens.arena.ArenaList
import com.prolearn.spar.ui.screens.arena.Blue
import com.prolearn.spar.ui.screens.arena.Coral
import com.prolearn.spar.ui.screens.arena.Gold
import com.prolearn.spar.ui.screens.arena.IconBadge
import com.prolearn.spar.ui.screens.arena.Ink
import com.prolearn.spar.ui.screens.arena.Moss
import com.prolearn.spar.ui.screens.arena.Paper
import com.prolearn.spar.ui.screens.arena.PremiumButton
import com.prolearn.spar.ui.screens.arena.PremiumPanel
import com.prolearn.spar.ui.screens.arena.SoftBorder
import com.prolearn.spar.ui.theme.ProLearnColors

@Composable
internal fun ChallengeSummaryScreen(
    result: ChallengeResult,
    onStartNewChallenge: () -> Unit,
    onBack: () -> Unit
) {
    val accent = if (result.type == ChallengeType.Pdf) Blue else Coral
    ArenaList {
        item {
            ArenaHeader(
                "Challenge Summary",
                "${result.setup.subject} · ${result.setup.topic}",
                "Challenges"
            ) { onBack() }
        }
        item {
            PremiumPanel(accent = accent) {
                ResultHero(result, accent)
                Spacer(Modifier.height(14.dp))
                Row {
                    SummaryMetric("Score", "${result.score}/${result.total}", Icons.Default.CheckCircle, Moss, Modifier.weight(1f))
                    Spacer(Modifier.width(10.dp))
                    SummaryMetric("XP earned", "${result.xpEarned}/${result.setup.maxXp}", Icons.Default.EmojiEvents, Gold, Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Row {
                    SummaryMetric("Read/watch", formatSeconds(result.readingSeconds), Icons.Default.Timer, accent, Modifier.weight(1f))
                    Spacer(Modifier.width(10.dp))
                    SummaryMetric("Answers", formatSeconds(result.answeringSeconds), Icons.Default.AutoAwesome, accent, Modifier.weight(1f))
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "XP is based on correct answers first, then adjusted by how quickly you finished the content and answered the quiz.",
                    color = ProLearnColors.MutedDark,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(16.dp))
                result.questions.forEachIndexed { index, question ->
                    val selected = result.answers[index]
                    AnswerReviewRow(index, question.prompt, selected, question.answer)
                    Spacer(Modifier.height(8.dp))
                }
                Spacer(Modifier.height(10.dp))
                PremiumButton("Start new challenge", Icons.Default.AutoAwesome, onStartNewChallenge)
            }
        }
    }
}

@Composable
private fun ResultHero(result: ChallengeResult, accent: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(accent.copy(alpha = 0.18f), Gold.copy(alpha = 0.10f), Color.White.copy(alpha = 0.78f))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.72f), RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(Icons.Default.EmojiEvents, Gold, 64)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(result.title, color = Ink, fontSize = 18.sp, lineHeight = 27.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                performanceLabel(result.score, result.total, result.xpEarned),
                color = ProLearnColors.MutedDark,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accent: Color, modifier: Modifier) {
    Row(
        modifier = modifier
            .height(68.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Paper)
            .border(1.dp, SoftBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(icon, accent, 34)
        Spacer(Modifier.width(9.dp))
        Column {
            Text(label, color = ProLearnColors.MutedDark, fontSize = 11.sp)
            Text(value, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun AnswerReviewRow(index: Int, prompt: String, selected: String?, answer: String) {
    val correct = selected == answer
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (correct) Moss.copy(alpha = 0.10f) else Coral.copy(alpha = 0.10f))
            .border(1.dp, if (correct) Moss.copy(alpha = 0.24f) else Coral.copy(alpha = 0.24f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            if (correct) Icons.Default.CheckCircle else Icons.Default.Close,
            null,
            tint = if (correct) Moss else Coral,
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
        )
        Spacer(Modifier.width(9.dp))
        Column(Modifier.weight(1f)) {
            Text("Q${index + 1}. $prompt", color = Ink, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold)
            Text("Correct: $answer", color = Moss, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text("Your answer: ${selected ?: "Skipped"}", color = ProLearnColors.MutedDark, fontSize = 12.sp)
        }
    }
}

private fun performanceLabel(score: Int, total: Int, xp: Int): String = when {
    score == total -> "Excellent accuracy. Speed decided how close you got to full XP."
    score == 0 -> "No XP this round because none of the answers were correct."
    xp >= 40 -> "Good run. A little more accuracy or speed can push this higher."
    else -> "You earned partial XP. Review the misses and try again."
}
