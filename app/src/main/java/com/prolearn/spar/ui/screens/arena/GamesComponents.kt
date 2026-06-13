package com.prolearn.spar.ui.screens.arena

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.R
import com.prolearn.spar.ui.theme.ProLearnColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ClueFlipChallenge(subject: String, difficulty: String, onExit: () -> Unit) {
    var roundIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedCsv by rememberSaveable { mutableStateOf("") }
    var attempts by rememberSaveable { mutableIntStateOf(0) }
    var roundDone by rememberSaveable { mutableStateOf(false) }
    var totalXp by rememberSaveable { mutableIntStateOf(0) }
    var correctRounds by rememberSaveable { mutableIntStateOf(0) }
    var message by rememberSaveable { mutableStateOf("Pick the card whose hint points to the answer.") }
    var awarded by rememberSaveable { mutableStateOf(false) }
    var showSummary by rememberSaveable { mutableStateOf(false) }

    val round = clueFlipRounds[roundIndex]
    val maxXp = clueFlipRounds.sumOf { flipRoundXp(it.baseXp, difficulty, 1) }
    val selected = remember(selectedCsv) {
        selectedCsv.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun resetRound(nextIndex: Int) {
        roundIndex = nextIndex
        selectedCsv = ""
        attempts = 0
        roundDone = false
        message = "Pick the card whose hint points to the answer."
    }

    Box(Modifier.fillMaxSize()) {
        ArenaList {
            item { ArenaHeader("Clue Flip", "$subject deck · $difficulty", "Games") { onExit() } }
            item {
                PremiumPanel(accent = Moss) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatPill(
                            "Round",
                            "${roundIndex + 1}/${clueFlipRounds.size}",
                            Icons.Default.GridOn,
                            Moss,
                            Modifier.weight(1f)
                        )
                        StatPill(
                            "Chances",
                            "${(3 - attempts).coerceAtLeast(0)}/3",
                            Icons.Default.Shield,
                            Gold,
                            Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    ClueQuestionPanel(round, difficulty, maxXp, totalXp)
                    Spacer(Modifier.height(14.dp))
                    PremiumCardGrid(
                        cards = round.cards,
                        selected = selected,
                        locked = roundDone || attempts >= 3
                    ) { index, card ->
                        if (index !in selected && !roundDone && attempts < 3) {
                            val newAttempts = attempts + 1
                            selectedCsv = (selected + index).joinToString(",")
                            attempts = newAttempts
                            if (card.correct) {
                                val earned = flipRoundXp(round.baseXp, difficulty, newAttempts)
                                totalXp += earned
                                correctRounds += 1
                                roundDone = true
                                message =
                                    "Correct. You found ${round.answer} and earned $earned XP."
                            } else if (newAttempts >= 3) {
                                val correctIndex = round.cards.indexOfFirst { it.correct }
                                selectedCsv = (selected + index + correctIndex).joinToString(",")
                                roundDone = true
                                message = "No chances left. The answer was ${round.answer}."
                            } else {
                                message = "That card was a trap hint. Try another card."
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    GameMessageStrip(message, if (roundDone) Moss else Blue)
                    if (roundDone) {
                        Spacer(Modifier.height(12.dp))
                        ClueAnswerRevealPanel(round)
                        Spacer(Modifier.height(12.dp))
                        if (roundIndex < clueFlipRounds.lastIndex) {
                            PremiumButton("Next clue", Icons.Default.PlayArrow) {
                                resetRound(roundIndex + 1)
                            }
                        } else {
                            PremiumButton("Show summary", Icons.Default.EmojiEvents) {
                                if (!awarded) {
                                    ArenaXpStore.addXp(totalXp)
                                    awarded = true
                                }
                                showSummary = true
                            }
                        }
                    }
                }
            }
        }
        if (showSummary) {
            GameSummarySheet(
                title = "Clue Flip complete",
                subtitle = "$subject · $difficulty",
                xp = totalXp,
                details = listOf(
                    "Correct clues" to "$correctRounds/${clueFlipRounds.size}",
                    "Best possible" to "30 XP/round",
                    "Accuracy" to "${correctRounds * 100 / clueFlipRounds.size}%"
                ),
                accent = Moss,
                onDone = onExit,
                onAgain = {
                    showSummary = false
                    roundIndex = 0
                    selectedCsv = ""
                    attempts = 0
                    roundDone = false
                    totalXp = 0
                    correctRounds = 0
                    awarded = false
                    message = "Pick the card whose hint points to the answer."
                }
            )
        }
    }
}

@Composable
fun XpSpinWheel(subject: String, onExit: () -> Unit) {
    val walletXp by ArenaXpStore.totalXp.collectAsState()
    var stage by rememberSaveable { mutableStateOf("wheel") }
    var rotationTarget by rememberSaveable { mutableStateOf(0f) }
    var isSpinning by rememberSaveable { mutableStateOf(false) }
    var showContractPopup by rememberSaveable { mutableStateOf(false) }
    var showSummary by rememberSaveable { mutableStateOf(false) }
    var spinSeed by rememberSaveable { mutableIntStateOf(0) }
    var challengeIndex by rememberSaveable { mutableIntStateOf(-1) }
    var questionStep by rememberSaveable { mutableIntStateOf(0) }
    var solvedCount by rememberSaveable { mutableIntStateOf(0) }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var sessionXp by rememberSaveable { mutableIntStateOf(0) }
    var secondsLeft by rememberSaveable { mutableIntStateOf(0) }
    var summaryTitle by rememberSaveable { mutableStateOf("Spin complete") }
    var message by rememberSaveable { mutableStateOf("Spin costs 5 XP. Contracts reward clean solving.") }

    val animatedRotation by animateFloatAsState(
        targetValue = rotationTarget,
        animationSpec = tween(1850, easing = FastOutSlowInEasing),
        label = "spinWheelRotation"
    )
    val challenge = spinContracts.getOrNull(challengeIndex)
    val question =
        challenge?.let { arenaQuestions[(questionStep + challengeIndex * 2) % arenaQuestions.size] }

    LaunchedEffect(spinSeed) {
        if (spinSeed > 0) {
            delay(1950)
            isSpinning = false
            showContractPopup = true
            delay(5000)
            showContractPopup = false
            stage = "questions"
            secondsLeft = spinContracts[challengeIndex].timeLimitSeconds ?: 0
        }
    }

    LaunchedEffect(stage, questionStep, challengeIndex, secondsLeft) {
        val timed = challenge?.timeLimitSeconds ?: 0
        if (stage == "questions" && timed > 0 && secondsLeft > 0 && selected == null) {
            delay(1000)
            secondsLeft -= 1
        } else if (stage == "questions" && timed > 0 && secondsLeft == 0 && selected == null) {
            summaryTitle = "Time ran out"
            showSummary = true
        }
    }

    fun spinNow() {
        if (!isSpinning && ArenaXpStore.spendXp(5)) {
            val landed = Random.nextInt(spinContracts.size)
            val segment = 360f / spinContracts.size
            challengeIndex = landed
            questionStep = 0
            solvedCount = 0
            selected = null
            summaryTitle = "Spin complete"
            message = "Spinning..."
            isSpinning = true
            rotationTarget += 1440f + (spinContracts.size - landed) * segment + segment / 2f
            spinSeed += 1
        } else if (!isSpinning) {
            message = "You need at least 5 XP to spin."
        }
    }

    Box(Modifier.fillMaxSize()) {
        ArenaList {
            item {
                ArenaHeader(
                    "XP Spinwheel",
                    "$subject contracts · 5 XP entry",
                    "Games"
                ) { onExit() }
            }
            item {
                when (stage) {
                    "wheel" -> SpinWheelStage(
                        walletXp = walletXp,
                        sessionXp = sessionXp,
                        rotation = animatedRotation,
                        isSpinning = isSpinning,
                        message = message
                    )

                    "questions" -> if (challenge != null && question != null) {
                        SpinQuestionStage(
                            subject = subject,
                            contract = challenge,
                            question = question,
                            questionIndex = questionStep,
                            solvedCount = solvedCount,
                            selected = selected,
                            secondsLeft = secondsLeft
                        ) { answer ->
                            if (selected == null) {
                                selected = answer
                                if (answer == question.answer) {
                                    val nextSolved = solvedCount + 1
                                    solvedCount = nextSolved
                                    if (nextSolved >= challenge.questionsRequired) {
                                        sessionXp += challenge.rewardXp
                                        ArenaXpStore.addXp(challenge.rewardXp)
                                        summaryTitle = "Contract cleared"
                                        showSummary = true
                                    } else {
                                        message = "Correct. Next question unlocked."
                                    }
                                } else {
                                    summaryTitle = "Contract missed"
                                    showSummary = true
                                }
                            }
                        }
                        AnimatedVisibility(selected != null && selected == question.answer && solvedCount < challenge.questionsRequired) {
                            PremiumButton("Next question", Icons.Default.PlayArrow) {
                                selected = null
                                questionStep += 1
                                secondsLeft = challenge.timeLimitSeconds ?: 0
                            }
                        }
                    }
                }
            }
        }
        if (showContractPopup && challenge != null) {
            SpinContractPopup(challenge)
        }
        if (stage == "wheel") {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.92f))
                    .padding(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 18.dp)
            ) {
                PremiumButton(
                    if (isSpinning) "Spinning..." else "Spin for 5 XP",
                    Icons.Default.Bolt,
                    { spinNow() }
                )
            }
        }
        if (showSummary && stage == "questions" && challenge != null) {
            GameSummarySheet(
                title = summaryTitle,
                subtitle = "${challenge.title} · $subject",
                xp = if (solvedCount >= challenge.questionsRequired) challenge.rewardXp else 0,
                details = listOf(
                    "Solved" to "$solvedCount/${challenge.questionsRequired}",
                    "Spin cost" to "5 XP",
                    "Session won" to "$sessionXp XP"
                ),
                accent = challenge.accent,
                onDone = onExit,
                onAgain = {
                    showSummary = false
                    stage = "wheel"
                    challengeIndex = -1
                    selected = null
                    message = "Spin costs 5 XP. Contracts reward clean solving."
                }
            )
        }
    }
}

@Composable
private fun RewardChip(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.76f))
            .border(1.dp, accent.copy(alpha = 0.20f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            color = ProLearnColors.MutedDark,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            value,
            color = accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun GameHeroPanel(title: String, subtitle: String, imageRes: Int, accent: Color) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(210.dp)
            .shadow(
                18.dp,
                RoundedCornerShape(28.dp),
                ambientColor = accent.copy(alpha = 0.16f),
                spotColor = accent.copy(alpha = 0.16f)
            )
            .clip(RoundedCornerShape(28.dp))
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.52f to Color(0x22000000),
                        1f to Color(0xD6000000)
                    )
                )
        )
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                title,
                color = Color.White,
                fontSize = 24.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                subtitle,
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun ClueQuestionPanel(round: FlipRound, difficulty: String, maxXp: Int, totalXp: Int) {
    Column(
        Modifier
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Moss.copy(alpha = 0.14f),
                        Color.White.copy(alpha = 0.94f),
                        Gold.copy(alpha = 0.12f)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.82f), RoundedCornerShape(26.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Default.AutoAwesome, Moss, 38)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Find the answer",
                    color = Moss,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    round.prompt,
                    color = Ink,
                    fontSize = 21.sp,
                    lineHeight = 25.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            RewardChip("1st flip", "${flipRoundXp(round.baseXp, difficulty, 1)} XP", Moss, Modifier.weight(1f))
            RewardChip("2nd flip", "${flipRoundXp(round.baseXp, difficulty, 2)} XP", Blue, Modifier.weight(1f))
            RewardChip("3rd flip", "${flipRoundXp(round.baseXp, difficulty, 3)} XP", Gold, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { if (maxXp == 0) 0f else totalXp / maxXp.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(100.dp)),
            color = Moss,
            trackColor = SoftBorder
        )
    }
}

@Composable
private fun PremiumCardGrid(
    cards: List<FlipCard>,
    selected: Set<Int>,
    locked: Boolean,
    onCard: (Int, FlipCard) -> Unit
) {
    Column(
        Modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Ink.copy(alpha = 0.05f))
            .border(1.dp, GlassStroke, RoundedCornerShape(28.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        cards.chunked(3).forEachIndexed { rowIndex, row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEachIndexed { colIndex, card ->
                    val index = rowIndex * 3 + colIndex
                    FlipHintCard(
                        card = card,
                        revealed = index in selected,
                        locked = locked,
                        modifier = Modifier.weight(1f)
                    ) { onCard(index, card) }
                }
            }
        }
    }
}

@Composable
private fun GameMessageStrip(message: String, accent: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(accent.copy(alpha = 0.11f))
            .border(1.dp, accent.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(Icons.Default.Bolt, accent, 30)
        Spacer(Modifier.width(9.dp))
        Text(
            message,
            color = Ink,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ClueAnswerRevealPanel(round: FlipRound) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Mint.copy(alpha = 0.58f))
            .border(1.dp, Moss.copy(alpha = 0.22f), RoundedCornerShape(22.dp))
            .padding(14.dp)
    ) {
        Text("Answer revealed", color = Moss, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
        Text(round.answer, color = Ink, fontSize = 21.sp, lineHeight = 24.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(6.dp))
        Text(
            round.explanation,
            color = ProLearnColors.MutedDark,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun FlipHintCard(
    card: FlipCard,
    revealed: Boolean,
    locked: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        if (revealed) 180f else 0f,
        spring(dampingRatio = 0.72f),
        label = "flipCardRotation"
    )
    Box(
        modifier
            .aspectRatio(1f)
            .shadow(
                10.dp,
                RoundedCornerShape(18.dp),
                ambientColor = Ink.copy(alpha = 0.12f),
                spotColor = Ink.copy(alpha = 0.12f)
            )
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
                alpha = if (locked && !revealed) 0.62f else 1f
            }
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = !locked && !revealed, onClick = onClick)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        if (revealed) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(
                        1.5.dp,
                        if (card.correct) Moss.copy(alpha = 0.42f) else Coral.copy(alpha = 0.32f),
                        RoundedCornerShape(18.dp)
                    )
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF12363A))
                    .border(1.5.dp, Gold.copy(alpha = 0.48f), RoundedCornerShape(18.dp))
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 13.dp, vertical = 18.dp)
                .graphicsLayer { if (revealed) rotationY = 180f }
        ) {
            if (!revealed) {
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.10f))
                        .border(1.dp, Gold.copy(alpha = 0.58f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("?", color = Gold, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(8.dp))
            }
            Text(
                if (revealed) card.answer else card.hint.take(42),
                color = if (revealed) Ink else Color.White,
                fontSize = if (revealed) 13.sp else 11.sp,
                lineHeight = if (revealed) 16.sp else 14.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                minLines = if (revealed) 2 else 3,
                maxLines = if (revealed) 3 else 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SpinWheel(rotation: Float) {
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1.12f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = rotation }
        ) {
            Image(
                painter = painterResource(R.drawable.arena_spinwheel_asset),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = 2.dp)
                .size(width = 34.dp, height = 46.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Ink)
                .border(1.dp, Color.White.copy(alpha = 0.32f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("▼", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SPIN", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                "5 XP",
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SpinWheelTaskLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier
            .width(74.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Ink.copy(alpha = 0.42f))
            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
            .padding(horizontal = 6.dp, vertical = 5.dp),
        color = Color.White,
        fontSize = 10.sp,
        lineHeight = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun SpinTaskLegend() {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.78f))
            .border(1.dp, GlassStroke, RoundedCornerShape(22.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        spinContracts.forEach { contract ->
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    Modifier
                        .padding(top = 4.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(contract.accent)
                )
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "${contract.title} · ${contract.rewardXp} XP",
                        color = Ink,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        contract.description,
                        color = ProLearnColors.MutedDark,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SpinContractPanel(contract: SpinContract, solved: Int, step: Int) {
    Column(
        Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(contract.accent.copy(alpha = 0.13f))
            .border(1.dp, contract.accent.copy(alpha = 0.24f), RoundedCornerShape(22.dp))
            .padding(14.dp)
    ) {
        Text(
            contract.title,
            color = contract.accent,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            "Solve ${contract.questionsRequired} ${contract.difficulty} question${if (contract.questionsRequired > 1) "s" else ""} to earn ${contract.rewardXp} XP",
            color = Ink,
            fontSize = 18.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            RewardChip("Solved", "$solved/${contract.questionsRequired}", Moss, Modifier.weight(1f))
            RewardChip(
                "Question",
                "${(step + 1).coerceAtMost(contract.questionsRequired)}/${contract.questionsRequired}",
                Blue,
                Modifier.weight(1f)
            )
            RewardChip("Reward", "${contract.rewardXp} XP", Gold, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SpinWheelStage(
    walletXp: Int,
    sessionXp: Int,
    rotation: Float,
    isSpinning: Boolean,
    message: String
) {
    PremiumPanel(accent = Coral, bgColor = Color.White.copy(alpha = 0.88f)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatPill("Wallet", "$walletXp XP", Icons.Default.Star, Gold, Modifier.weight(1f))
            StatPill(
                "Won now",
                "$sessionXp XP",
                Icons.Default.EmojiEvents,
                Moss,
                Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))
        SpinWheel(rotation)
        SpinTaskLegend()
        Spacer(Modifier.height(8.dp))
        GameMessageStrip(message, if (isSpinning) Coral else Gold)
    }
}

@Composable
private fun SpinRevealStage(contract: SpinContract) {
    val pulse by rememberInfiniteTransition(label = "contractPulse").animateFloat(
        0.96f,
        1.04f,
        infiniteRepeatable(tween(720, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "contractPulseValue"
    )
    PremiumPanel(accent = contract.accent) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.radialGradient(
                        listOf(contract.accent.copy(alpha = 0.42f), Ink.copy(alpha = 0.95f))
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(30.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer { scaleX = pulse; scaleY = pulse }) {
                IconBadge(Icons.Default.EmojiEvents, Gold, 74)
                Spacer(Modifier.height(14.dp))
                Text(
                    "You got",
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    contract.title,
                    color = Color.White,
                    fontSize = 30.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    contract.description,
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(14.dp))
                RewardChip("Reward", "${contract.rewardXp} XP", Gold)
            }
        }
    }
}

@Composable
private fun SpinContractPopup(contract: SpinContract) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.42f))
            .padding(22.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .shadow(
                    30.dp,
                    RoundedCornerShape(30.dp),
                    ambientColor = contract.accent.copy(alpha = 0.26f),
                    spotColor = contract.accent.copy(alpha = 0.26f)
                )
                .clip(RoundedCornerShape(30.dp))
                .background(Color.White.copy(alpha = 0.97f))
                .border(1.dp, contract.accent.copy(alpha = 0.24f), RoundedCornerShape(30.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconBadge(Icons.Default.EmojiEvents, Gold, 64)
            Spacer(Modifier.height(12.dp))
            Text(
                "Wheel landed on",
                color = ProLearnColors.MutedDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                contract.title,
                color = Ink,
                fontSize = 28.sp,
                lineHeight = 31.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                contract.description,
                color = ProLearnColors.MutedDark,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RewardChip("Reward", "${contract.rewardXp} XP", Gold)
                RewardChip("Task", contract.shortLabel.replace("\n", " · "), contract.accent)
            }
        }
    }
}

@Composable
private fun SpinQuestionStage(
    subject: String,
    contract: SpinContract,
    question: ArenaQuestion,
    questionIndex: Int,
    solvedCount: Int,
    selected: String?,
    secondsLeft: Int,
    onSelect: (String) -> Unit
) {
    PremiumPanel(accent = contract.accent) {
        SpinContractPanel(contract, solvedCount, questionIndex)
        Spacer(Modifier.height(12.dp))
        GameQuestionCard(
            label = subject,
            question = question,
            accent = contract.accent,
            selected = selected,
            secondsLeft = if (contract.timeLimitSeconds == null) null else secondsLeft,
            onSelect = onSelect
        )
    }
}

@Composable
private fun GameQuestionCard(
    label: String,
    question: ArenaQuestion,
    accent: Color,
    selected: String?,
    secondsLeft: Int?,
    onSelect: (String) -> Unit
) {
    Column(
        Modifier
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White.copy(alpha = 0.94f))
            .border(1.dp, GlassStroke, RoundedCornerShape(26.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RewardChip(label, question.chapter, accent)
            Spacer(Modifier.weight(1f))
            if (secondsLeft != null) {
                Text(
                    "${secondsLeft}s",
                    color = if (secondsLeft <= 8) Coral else Moss,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        if (secondsLeft != null) {
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { (secondsLeft / 40f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(100.dp)),
                color = if (secondsLeft <= 8) Coral else accent,
                trackColor = SoftBorder
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            question.prompt,
            color = Ink,
            fontSize = 22.sp,
            lineHeight = 27.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(14.dp))
        question.options.forEachIndexed { index, option ->
            GameAnswerOption("${'A' + index}", option, selected, question.answer, accent) {
                if (selected == null) onSelect(option)
            }
            Spacer(Modifier.height(9.dp))
        }
        AnimatedVisibility(selected != null) {
            Text(
                if (selected == question.answer) "Correct. ${question.explanation}" else "Answer: ${question.answer}. ${question.explanation}",
                color = if (selected == question.answer) Moss else Coral,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun GameAnswerOption(
    label: String,
    option: String,
    selected: String?,
    answer: String,
    accent: Color,
    onClick: () -> Unit
) {
    val isSelected = selected == option
    val isCorrect = selected != null && option == answer
    val bg = when {
        isCorrect -> Mint
        isSelected -> Blush
        else -> Color.White
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .border(
                1.dp,
                if (isCorrect) Moss.copy(alpha = 0.42f) else if (isSelected) Coral.copy(alpha = 0.42f) else SoftBorder,
                RoundedCornerShape(18.dp)
            )
            .clickable(enabled = selected == null, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center
        ) {
            Text(label, color = accent, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
        }
        Spacer(Modifier.width(10.dp))
        Text(
            option,
            color = Ink,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SpinInlineSummary(
    title: String,
    contract: SpinContract,
    solved: Int,
    sessionXp: Int,
    onSpinAgain: () -> Unit
) {
    PremiumPanel(accent = contract.accent) {
        Confetti()
        Text(
            title,
            color = Ink,
            fontSize = 28.sp,
            lineHeight = 31.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatPill(
                "Solved",
                "$solved/${contract.questionsRequired}",
                Icons.Default.Shield,
                Moss,
                Modifier.weight(1f)
            )
            StatPill("Session", "$sessionXp XP", Icons.Default.Star, Gold, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        PremiumButton("Spin again", Icons.Default.Bolt, onSpinAgain)
    }
}

@Composable
fun GameSetupSheet(
    title: String,
    subtitle: String,
    accent: Color,
    subject: String,
    onSubject: (String) -> Unit,
    difficulty: String,
    onDifficulty: (String) -> Unit,
    showDifficulty: Boolean,
    primary: String,
    onStart: () -> Unit,
    onDismiss: () -> Unit
) {
    BottomSheetScrim(onDismiss = onDismiss) {
        Text(
            title,
            color = Ink,
            fontSize = 24.sp,
            lineHeight = 27.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(subtitle, color = ProLearnColors.MutedDark, fontSize = 13.sp, lineHeight = 18.sp)
        Spacer(Modifier.height(14.dp))
        Text("Subject", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(8.dp))
        OptionChips(listOf("Physics", "Chemistry", "Maths", "Aptitude"), subject, accent, onSubject)
        if (showDifficulty) {
            Spacer(Modifier.height(14.dp))
            Text("Difficulty", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(8.dp))
            OptionChips(listOf("Easy", "Medium", "Hard"), difficulty, accent, onDifficulty)
        }
        Spacer(Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(Modifier.weight(0.30f)) {
                SheetSecondaryButton("Cancel", onDismiss)
            }
            Box(Modifier.weight(0.70f)) {
                PremiumButton(primary, Icons.Default.PlayArrow, onStart)
            }
        }
    }
}

@Composable
private fun GameSummarySheet(
    title: String,
    subtitle: String,
    xp: Int,
    details: List<Pair<String, String>>,
    accent: Color,
    onDone: () -> Unit,
    onAgain: () -> Unit
) {
    BottomSheetScrim(onDismiss = onDone) {
        Confetti()
        Text(
            title,
            color = Ink,
            fontSize = 26.sp,
            lineHeight = 29.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            subtitle,
            color = ProLearnColors.MutedDark,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(accent.copy(alpha = 0.12f))
                .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(24.dp))
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("$xp XP", color = accent, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
            Text("earned this run", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        details.forEach { (label, value) ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, color = ProLearnColors.MutedDark, fontSize = 13.sp)
                Text(value, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.height(7.dp))
        }
        Spacer(Modifier.height(10.dp))
        PremiumButton("Play again", Icons.Default.PlayArrow, onAgain)
        Spacer(Modifier.height(8.dp))
        SheetSecondaryButton("Done", onDone)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetScrim(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        contentColor = Ink,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .size(width = 42.dp, height = 5.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(SoftBorder)
            )
        }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, bottom = 26.dp),
            content = content
        )
    }
}

@Composable
private fun SheetSecondaryButton(text: String, onClick: () -> Unit) {
    Text(
        text,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, SoftBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(top = 14.dp),
        color = Ink,
        textAlign = TextAlign.Center,
        fontSize = 14.sp,
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
private fun OptionChips(
    options: List<String>,
    selected: String,
    accent: Color,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.chunked(2).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { option ->
                    val active = selected == option
                    Text(
                        option,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (active) accent.copy(alpha = 0.16f) else Color.White)
                            .border(
                                1.dp,
                                if (active) accent.copy(alpha = 0.34f) else SoftBorder,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { onSelect(option) }
                            .padding(vertical = 12.dp),
                        color = if (active) accent else Ink,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}


private data class FlipCard(
    val hint: String,
    val answer: String,
    val correct: Boolean = false
)

private data class FlipRound(
    val prompt: String,
    val answer: String,
    val baseXp: Int,
    val explanation: String,
    val cards: List<FlipCard>
)

private data class SpinContract(
    val title: String,
    val shortLabel: String,
    val questionsRequired: Int,
    val difficulty: String,
    val rewardXp: Int,
    val accent: Color,
    val description: String,
    val timeLimitSeconds: Int? = null
)

private fun flipRoundXp(baseXp: Int, difficulty: String, attempt: Int): Int {
    val attemptBoost = when (attempt) {
        1 -> 1f
        2 -> 0.70f
        else -> 0.40f
    }
    return (baseXp * attemptBoost).toInt().coerceAtLeast(1)
}

private val clueFlipRounds = listOf(
    FlipRound(
        prompt = "Which gas is released during photosynthesis?",
        answer = "Oxygen",
        baseXp = 30,
        explanation = "During photosynthesis, water molecules split in the light reaction and oxygen is released as a by-product.",
        cards = listOf(
            FlipCard("Taken in through stomata", "Carbon dioxide"),
            FlipCard("Released when water splits", "Oxygen", true),
            FlipCard("Main gas in air", "Nitrogen"),
            FlipCard("Uses oxygen to release energy", "Respiration"),
            FlipCard("Green pigment clue", "Chlorophyll"),
            FlipCard("Stored food product", "Glucose"),
            FlipCard("Gas from combustion", "Carbon dioxide"),
            FlipCard("Light energy helper", "Sunlight"),
            FlipCard("Root absorption", "Water")
        )
    ),
    FlipRound(
        prompt = "Which law connects force, mass, and acceleration?",
        answer = "Newton's second law",
        baseXp = 30,
        explanation = "Newton's second law is written as F = ma, so force is directly connected to mass and acceleration.",
        cards = listOf(
            FlipCard("Equal and opposite reaction", "Newton's third law"),
            FlipCard("Inertia at rest", "Newton's first law"),
            FlipCard("F = ma", "Newton's second law", true),
            FlipCard("Energy is conserved", "Conservation of energy"),
            FlipCard("Momentum before and after", "Conservation of momentum"),
            FlipCard("Universal attraction", "Gravitation law"),
            FlipCard("Current follows voltage", "Ohm's law"),
            FlipCard("Pressure changes volume", "Boyle's law"),
            FlipCard("Heat at constant pressure", "Charles' law")
        )
    ),
    FlipRound(
        prompt = "What is the slope of a distance-time graph?",
        answer = "Speed",
        baseXp = 30,
        explanation = "The slope of a distance-time graph is distance divided by time, which gives speed.",
        cards = listOf(
            FlipCard("Change in velocity per time", "Acceleration"),
            FlipCard("Distance covered per time", "Speed", true),
            FlipCard("Area under velocity-time graph", "Displacement"),
            FlipCard("Mass times velocity", "Momentum"),
            FlipCard("Force times displacement", "Work"),
            FlipCard("Rise over run for velocity and time", "Acceleration"),
            FlipCard("Potential plus kinetic", "Mechanical energy"),
            FlipCard("Rate of doing work", "Power"),
            FlipCard("Opposes motion", "Friction")
        )
    )
)

private val spinContracts = listOf(
    SpinContract(
        "Quick Win",
        "1 Easy\n10 XP",
        1,
        "easy",
        10,
        Coral,
        "Solve one easy question and bank a quick reward."
    ),
    SpinContract(
        "Timed Medium",
        "40s Medium\n14 XP",
        1,
        "medium",
        14,
        Blue,
        "Solve one medium question before the timer expires.",
        40
    ),
    SpinContract(
        "Double Focus",
        "2 Medium\n18 XP",
        2,
        "medium",
        18,
        Gold,
        "Answer two back-to-back questions without missing."
    ),
    SpinContract(
        "Boss Chain",
        "3 Mixed\n28 XP",
        3,
        "mixed",
        28,
        Violet,
        "Clear three questions in a row for the biggest payout."
    ),
    SpinContract(
        "Speed Tap",
        "25s Easy\n12 XP",
        1,
        "easy",
        12,
        Color(0xFF596463),
        "A short timer, one question, clean answer.",
        25
    ),
    SpinContract(
        "Elite Pull",
        "2 Hard\n24 XP",
        2,
        "hard",
        24,
        Mint,
        "Two tougher pulls with a stronger XP reward."
    )
)

@Composable
fun KnowledgeLudo(onExit: () -> Unit) {
    var questionOpen by rememberSaveable { mutableStateOf(true) }
    var turn by rememberSaveable { mutableIntStateOf(0) }
    var position by rememberSaveable { mutableIntStateOf(0) }
    var aiPosition by rememberSaveable { mutableIntStateOf(0) }
    var dice by rememberSaveable { mutableIntStateOf(1) }
    var winner by rememberSaveable { mutableStateOf<String?>(null) }
    var awarded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(turn, questionOpen, winner) {
        if (!questionOpen && winner == null) {
            delay(1000)
            aiPosition = (aiPosition + Random.nextInt(1, 7)).coerceAtMost(28)
            if (aiPosition >= 28) winner = "AI Captain"
            questionOpen = true
            turn += 1
        }
    }

    ArenaList {
        item { ArenaHeader("Knowledge Ludo", "Answer correctly to roll.", "Games") { onExit() } }
        item {
            PremiumPanel {
                LudoBoard(position, aiPosition)
                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatPill(
                        "Your token",
                        "$position/28",
                        Icons.Default.PlayArrow,
                        Moss,
                        Modifier.weight(1f)
                    )
                    StatPill("Dice", "$dice", Icons.Default.GridOn, Gold, Modifier.weight(1f))
                }
                if (questionOpen && winner == null) {
                    Spacer(Modifier.height(14.dp))
                    MiniQuestion(arenaQuestions[turn % arenaQuestions.size]) { correct ->
                        if (correct) {
                            dice = Random.nextInt(1, 7)
                            position = (position + dice).coerceAtMost(28)
                            if (position >= 28) winner = "You"
                        }
                        questionOpen = false
                    }
                }
                if (winner != null) {
                    Spacer(Modifier.height(14.dp))
                    WinnerPanel(winner == "You", if (winner == "You") 70 else 20) {
                        if (!awarded) {
                            ArenaXpStore.addXp(if (winner == "You") 70 else 20)
                            awarded = true
                        }
                        onExit()
                    }
                }
            }
        }
    }
}

@Composable
fun WordBattlefield(onExit: () -> Unit) {
    val words =
        remember { listOf("osmosis", "nucleus", "photon", "valence", "enzyme", "vector", "ion") }
    var input by rememberSaveable { mutableStateOf("") }
    var round by rememberSaveable { mutableIntStateOf(1) }
    var score by rememberSaveable { mutableIntStateOf(0) }
    var rival by rememberSaveable { mutableIntStateOf(80) }
    var message by rememberSaveable { mutableStateOf("Form a syllabus word from your tiles.") }
    var awarded by rememberSaveable { mutableStateOf(false) }
    val tiles = remember { "OSMIPHN".toList() }

    ArenaList {
        item {
            ArenaHeader(
                "Word Battlefield",
                "Valid terms claim the board.",
                "Games"
            ) { onExit() }
        }
        item {
            PremiumPanel {
                WordBoard(input.uppercase())
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tiles.forEach { Tile("$it") }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Type science word") })
                Spacer(Modifier.height(8.dp))
                Text(message, fontSize = 13.sp, color = ProLearnColors.MutedDark)
                Spacer(Modifier.height(12.dp))
                PremiumButton(
                    if (round <= 5) "Play round $round" else "Finish battle",
                    Icons.Default.Bolt
                ) {
                    val clean = input.trim().lowercase()
                    if (round <= 5 && clean in words) {
                        score += clean.length * 20
                        rival += Random.nextInt(20, 70)
                        round += 1
                        input = ""
                        message = "Accepted. Opponent played a counter-word."
                    } else if (round <= 5) {
                        rival += 35
                        round += 1
                        message = "Rejected. Try a syllabus term next round."
                    }
                }
                if (round > 5) {
                    Spacer(Modifier.height(14.dp))
                    WinnerPanel(score >= rival, if (score >= rival) 60 else 15) {
                        if (!awarded) {
                            ArenaXpStore.addXp(if (score >= rival) 60 else 15)
                            awarded = true
                        }
                        onExit()
                    }
                    Text(
                        "Final score: You $score · Opponent $rival",
                        color = Ink,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private data class GameTile(
    val name: String,
    val players: String,
    val subject: String,
    val xp: String,
    val icon: ImageVector,
    val accent: Color,
    val onClick: () -> Unit
) {
    @Composable
    fun Content(snackbarHostState: SnackbarHostState) {
        val scope = rememberCoroutineScope()
        SmallGameCard(name, players, subject, xp, icon, accent) {
            if (xp == "Soon") {
                scope.launch { snackbarHostState.showSnackbar("$name is coming soon") }
            } else {
                onClick()
            }
        }
    }
}

private val pdfQuestions = listOf(
    ArenaQuestion(
        "What pigment captures sunlight in leaves?",
        listOf("Chlorophyll", "Keratin", "Insulin", "Hemoglobin"),
        "Chlorophyll"
    ),
    ArenaQuestion(
        "Which gas enters leaves through stomata?",
        listOf("Oxygen", "Carbon dioxide", "Nitrogen", "Helium"),
        "Carbon dioxide"
    ),
    ArenaQuestion(
        "What sugar is made during photosynthesis?",
        listOf("Glucose", "Fructose only", "Lactose", "Maltose only"),
        "Glucose"
    )
)

private val youtubeQuestions = listOf(
    ArenaQuestion(
        "A force can change an object's...",
        listOf("Color only", "Motion", "Mass always", "Name"),
        "Motion"
    ),
    ArenaQuestion(
        "Acceleration means change in...",
        listOf("Velocity", "Temperature", "Shape only", "Charge"),
        "Velocity"
    ),
    ArenaQuestion(
        "Balanced forces produce...",
        listOf("No net force", "Infinite speed", "New mass", "Light"),
        "No net force"
    )
)

@Composable
fun SmallGameCard(
    name: String,
    players: String,
    subject: String,
    xp: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.97f else 1f,
        spring(dampingRatio = 0.7f),
        label = "gameCardScale"
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White.copy(alpha = 0.86f))
            .border(1.dp, GlassStroke, RoundedCornerShape(26.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        IconBadge(icon, accent)
        Column {
            Text(
                name,
                fontSize = 17.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )
            Text(players, fontSize = 11.sp, color = ProLearnColors.MutedDark)
            Text(
                subject,
                fontSize = 11.sp,
                color = ProLearnColors.MutedDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(xp, fontSize = 12.sp, color = accent, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MiniQuestion(question: ArenaQuestion, onAnswered: (Boolean) -> Unit) {
    Column(
        Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Mint.copy(alpha = 0.72f))
            .padding(14.dp)
    ) {
        Text(question.prompt, color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        question.options.forEach { option ->
            MiniButton(option) { onAnswered(option == question.answer) }
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
fun LudoBoard(position: Int, aiPosition: Int) {
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, SoftBorder, RoundedCornerShape(24.dp))
            .padding(14.dp)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val cell = size.width / 7f
            for (i in 0..7) {
                drawLine(SoftBorder, Offset(i * cell, 0f), Offset(i * cell, size.height), 1.5f)
                drawLine(SoftBorder, Offset(0f, i * cell), Offset(size.width, i * cell), 1.5f)
            }
            drawCircle(
                Moss.copy(alpha = 0.18f),
                size.minDimension * 0.18f,
                Offset(cell * 1.2f, cell * 1.2f)
            )
            drawCircle(
                Coral.copy(alpha = 0.18f),
                size.minDimension * 0.18f,
                Offset(cell * 5.8f, cell * 1.2f)
            )
            drawCircle(
                Blue.copy(alpha = 0.18f),
                size.minDimension * 0.18f,
                Offset(cell * 1.2f, cell * 5.8f)
            )
            drawCircle(
                Gold.copy(alpha = 0.18f),
                size.minDimension * 0.18f,
                Offset(cell * 5.8f, cell * 5.8f)
            )
        }
        Token(position, Moss, "Y")
        Token(aiPosition, Coral, "A")
    }
}

@Composable
fun BoxScope.Token(position: Int, color: Color, label: String) {
    val step = min(position, 28)
    val angle = (step / 28f) * 6.28318f
    Box(
        Modifier
            .align(Alignment.Center)
            .offset(x = (95 * cos(angle)).dp, y = (95 * sin(angle)).dp)
            .size(32.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun WordBoard(word: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, SoftBorder, RoundedCornerShape(24.dp))
            .padding(10.dp)
    ) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(8) { row ->
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(8) { col ->
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if ((row + col) % 3 == 0) Sky else Color(0xFFF6F8F5)),
                            contentAlignment = Alignment.Center
                        ) {
                            val index = col - 1
                            if (row == 3 && index in word.indices) Text(
                                "${word[index]}",
                                color = Ink,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Tile(letter: String) {
    Box(
        Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Gold.copy(alpha = 0.34f))
            .border(1.dp, Gold, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center
    ) {
        Text(letter, color = Ink, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun WinnerPanel(won: Boolean, xp: Int, onDone: () -> Unit) {
    Column(
        Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(if (won) Mint else Blush)
            .padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Confetti()
        Text(
            if (won) "You win!" else "Good fight",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Ink
        )
        Text("$xp XP earned", color = Moss, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        PremiumButton("Claim XP", Icons.Default.Star, onDone)
    }
}
