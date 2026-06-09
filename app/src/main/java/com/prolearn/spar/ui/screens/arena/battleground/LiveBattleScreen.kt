package com.prolearn.spar.ui.screens.arena.battleground

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.ui.screens.arena.ArenaPlayer
import com.prolearn.spar.ui.screens.arena.ArenaQuestion
import com.prolearn.spar.ui.screens.arena.BattleArena
import com.prolearn.spar.ui.screens.arena.Coral
import com.prolearn.spar.ui.screens.arena.Gold
import com.prolearn.spar.ui.screens.arena.Ink
import com.prolearn.spar.ui.screens.arena.Mint
import com.prolearn.spar.ui.screens.arena.Moss
import com.prolearn.spar.ui.screens.arena.Paper
import com.prolearn.spar.ui.screens.arena.PremiumPanel
import com.prolearn.spar.ui.screens.arena.SoftBorder
import com.prolearn.spar.ui.theme.BricolageGrotesqueFamily
import com.prolearn.spar.ui.theme.ProLearnColors
import kotlinx.coroutines.delay
import kotlin.random.Random

internal const val ReadSecondsTotal = 5
internal const val AnswerSecondsTotal = 15
private const val HurrySecondsThreshold = 5

@Composable
internal fun LiveBattleScreen(
    kind: BattleKind,
    arena: BattleArena,
    questionIndex: Int,
    readSeconds: Int,
    answerSeconds: Int,
    selected: String?,
    score: Int,
    xp: Int,
    youHealth: Int,
    rivalHealth: Int,
    players: MutableList<ArenaPlayer>,
    onReadTick: (Int) -> Unit,
    onAnswerTick: (Int) -> Unit,
    onSelect: (String) -> Unit,
    onApplyResult: (score: Int, xp: Int, youHealth: Int, rivalHealth: Int) -> Unit,
    onNextQuestion: () -> Unit
) {
    val question = arena.questions[questionIndex]
    var resultApplied by remember(questionIndex) { mutableStateOf(false) }

    LaunchedEffect(questionIndex, readSeconds) {
        if (readSeconds > 0) {
            delay(1000)
            onReadTick(readSeconds - 1)
        }
    }

    LaunchedEffect(questionIndex, readSeconds, answerSeconds) {
        if (readSeconds == 0 && answerSeconds > 0) {
            delay(1000)
            onAnswerTick(answerSeconds - 1)
        }
    }

    LaunchedEffect(questionIndex, answerSeconds) {
        if (answerSeconds == 0) {
            if (selected == null) onSelect("")
            delay(800)
            onNextQuestion()
        }
    }

    LaunchedEffect(questionIndex, selected) {
        val chosen = selected ?: return@LaunchedEffect
        if (resultApplied) return@LaunchedEffect
        resultApplied = true

        val correct = chosen == question.answer
        val answeredIn = if (chosen.isBlank()) null else AnswerSecondsTotal - answerSeconds
        val earnedScore = if (correct) 100 + answerSeconds else 0
        val earnedXp = if (correct) 5 else -1
        val nextScore = score + earnedScore
        val nextXp = xp + earnedXp
        val nextYouHealth = if (kind == BattleKind.Duel && !correct) (youHealth - 18).coerceAtLeast(0) else youHealth
        val nextRivalHealth = if (kind == BattleKind.Duel && correct) (rivalHealth - 24).coerceAtLeast(0) else rivalHealth

        players.replaceAll { player ->
            if (player.name == "You") {
                player.copy(
                    score = nextScore,
                    xp = nextXp,
                    answeredIn = answeredIn,
                    streak = if (correct) player.streak + 1 else 0,
                    health = nextYouHealth
                )
            } else {
                val botCorrect = Random.nextInt(100) > if (kind == BattleKind.Erangel) 26 else 38
                val botTime = Random.nextInt(3, AnswerSecondsTotal.coerceAtLeast(4))
                val gain = if (botCorrect) 5 else -1
                player.copy(
                    score = player.score + if (botCorrect) 100 + (AnswerSecondsTotal - botTime) else Random.nextInt(0, 35),
                    xp = player.xp + gain,
                    answeredIn = botTime,
                    streak = if (botCorrect) player.streak + 1 else 0,
                    health = if (kind == BattleKind.Duel) nextRivalHealth else player.health
                )
            }
        }
        players.sortByDescending { it.score }
        onApplyResult(nextScore, nextXp, nextYouHealth, nextRivalHealth)
    }

    PremiumPanel(accent = arena.accent) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            BattleInfoChip("Q${questionIndex + 1}/${arena.questions.size}", arena.accent)
            Spacer(Modifier.width(8.dp))
            BattleInfoChip(question.tag, Gold)
            Spacer(Modifier.weight(1f))
            Text(
                if (readSeconds > 0) "Read ${readSeconds}s" else "${answerSeconds}s",
                color = if (readSeconds > 0 || answerSeconds > HurrySecondsThreshold) Moss else Coral,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        }
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { if (readSeconds > 0) readSeconds / ReadSecondsTotal.toFloat() else answerSeconds / AnswerSecondsTotal.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(100.dp)),
            color = if (readSeconds > 0 || answerSeconds > HurrySecondsThreshold) arena.accent else Coral,
            trackColor = SoftBorder
        )
        AnimatedVisibility(readSeconds == 0 && answerSeconds <= HurrySecondsThreshold && answerSeconds > 0) {
            HurryMessage(answerSeconds)
        }
        Spacer(Modifier.height(16.dp))
        Text(question.chapter.uppercase(), color = arena.accent, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(6.dp))
        Text(
            question.prompt,
            fontSize = 23.sp,
            lineHeight = 28.sp,
            color = Ink,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = BricolageGrotesqueFamily
        )
        Spacer(Modifier.height(14.dp))
        AnimatedVisibility(readSeconds > 0) {
            ReadingLockout(readSeconds, arena.accent)
        }
        AnimatedVisibility(readSeconds == 0) {
            Column {
                question.options.forEachIndexed { index, option ->
                    BattleAnswerOption("${'A' + index}", option, selected, question.answer) {
                        if (selected == null) onSelect(option)
                    }
                    Spacer(Modifier.height(9.dp))
                }
                AnimatedVisibility(selected != null) {
                    AnswerExplanation(question = question, selected = selected.orEmpty(), accent = arena.accent)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        LiveScoreSection(players = players, score = score, xp = xp, kind = kind)
    }
}

@Composable
private fun ReadingLockout(seconds: Int, accent: Color) {
    val pulse by rememberInfiniteTransition(label = "readPulse").animateFloat(
        initialValue = 0.96f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(820, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "readPulseValue"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = accent.copy(alpha = 0.12f), spotColor = accent.copy(alpha = 0.12f))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(accent.copy(alpha = 0.15f), Mint.copy(alpha = 0.88f), Color.White.copy(alpha = 0.92f))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.82f), RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .graphicsLayer { scaleX = pulse; scaleY = pulse }
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.78f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = accent, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "Read carefully",
                color = Ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = BricolageGrotesqueFamily
            )
            Text(
                "Options unlock in $seconds seconds. Spot the trap before speed matters.",
                color = ProLearnColors.MutedDark,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun BattleAnswerOption(label: String, option: String, selected: String?, answer: String, onClick: () -> Unit) {
    val isSelected = selected == option
    val isCorrect = selected != null && option == answer
    val color = when {
        isCorrect -> Moss
        isSelected -> Coral
        else -> Color.White
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected == null) Color.White.copy(alpha = 0.78f) else color.copy(alpha = 0.14f))
            .border(1.dp, if (isCorrect || isSelected) color else SoftBorder, RoundedCornerShape(18.dp))
            .clickable(enabled = selected == null, onClick = onClick)
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (selected == null) Paper else color.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(label, color = Ink, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.width(10.dp))
        Text(option, Modifier.weight(1f), color = Ink, fontWeight = FontWeight.SemiBold, lineHeight = 19.sp)
        AnimatedVisibility(selected != null && (isCorrect || isSelected)) {
            Icon(if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close, null, tint = color, modifier = Modifier.size(19.dp))
        }
    }
}

@Composable
private fun AnswerExplanation(question: ArenaQuestion, selected: String, accent: Color) {
    val correct = selected == question.answer
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(if (correct) Mint.copy(alpha = 0.84f) else Coral.copy(alpha = 0.10f))
            .border(1.dp, if (correct) Moss.copy(alpha = 0.24f) else Coral.copy(alpha = 0.22f), RoundedCornerShape(22.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lightbulb, null, tint = if (correct) accent else Coral, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                if (correct) "Correct. Here is why:" else "Good review. Correct answer: ${question.answer}",
                color = Ink,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(question.explanation, color = ProLearnColors.MutedDark, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

@Composable
private fun HurryMessage(seconds: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Coral.copy(alpha = 0.12f))
            .border(1.dp, Coral.copy(alpha = 0.24f), RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.LocalFireDepartment, null, tint = Coral, modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(8.dp))
        Text("Hurry, only $seconds seconds left", color = Coral, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
    }
}

@Composable
private fun LiveScoreSection(players: List<ArenaPlayer>, score: Int, xp: Int, kind: BattleKind) {
    val you = players.firstOrNull { it.name == "You" }?.copy(score = score, xp = xp)
        ?: ArenaPlayer("You", "Current player", "YO", 0, score = score, xp = xp)

    if (kind == BattleKind.Duel) {
        val livePlayers = players.map { player ->
            if (player.name == "You") you else player
        }.sortedByDescending { it.score }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Live score", color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
            Spacer(Modifier.weight(1f))
            Text("Duel board", color = ProLearnColors.MutedDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.height(8.dp))
        livePlayers.take(2).forEach { player ->
            ScoreCard(player = player, score = player.score, xp = player.xp, isYou = player.name == "You")
            Spacer(Modifier.height(8.dp))
        }
    } else {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("You", color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Spacer(Modifier.weight(1f))
            Text("Live score", color = ProLearnColors.MutedDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.height(8.dp))
        ScoreCard(player = you, score = score, xp = xp, isYou = true)
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Top 3", color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Spacer(Modifier.weight(1f))
            Text("Live score", color = ProLearnColors.MutedDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.height(8.dp))
        players.take(3).forEach { player ->
            ScoreCard(player = player, score = player.score, xp = player.xp, isYou = player.name == "You")
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ScoreCard(player: ArenaPlayer, score: Int, xp: Int, isYou: Boolean) {
    val scoreColor = if (score >= 0) Moss else MaterialTheme.colorScheme.error
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (isYou) Mint.copy(alpha = 0.82f) else Color.White.copy(alpha = 0.66f))
            .border(1.dp, if (isYou) Moss.copy(alpha = 0.22f) else SoftBorder, RoundedCornerShape(18.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LiveStudentAvatar(player, size = 38)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(player.name, color = Ink, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(
                "${xp} XP",
                color = if (xp >= 0) ProLearnColors.MutedDark else MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = scoreColor, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(score.toString(), color = scoreColor, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun LiveStudentAvatar(player: ArenaPlayer, modifier: Modifier = Modifier, size: Int = 44) {
    Box(
        modifier
            .size(size.dp)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(Mint)
            .border(2.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(player.avatarRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
        Box(
            Modifier
                .matchParentSize()
                .background(Brush.verticalGradient(0f to Color.Transparent, 1f to Color(0x22000000)))
        )
    }
}

@Composable
private fun BattleInfoChip(text: String, accent: Color) {
    Text(
        text,
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(accent.copy(alpha = 0.13f))
            .border(1.dp, accent.copy(alpha = 0.20f), RoundedCornerShape(100.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        color = Ink,
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
