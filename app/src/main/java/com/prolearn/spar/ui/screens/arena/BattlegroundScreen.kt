package com.prolearn.spar.ui.screens.arena

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.R
import com.prolearn.spar.ui.theme.ProLearnColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class BattleKind { Erangel, Room }

@Composable
fun BattlegroundScreen(
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var mode by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    when (mode) {
        "erangel" -> BattleFlow(BattleKind.Erangel, onExit = { mode = null })
        "room" -> BattleFlow(BattleKind.Room, onExit = { mode = null })
        else -> ArenaList {
            item { ArenaHeader("Battleground", "Choose your combat style.", "Arena") { onBack() } }
            item {
                ArenaFeatureCard(
                    title = "Erangel",
                    eyebrow = "Squad Battle",
                    body = "Drop into a many-vs-many quiz battle with live rankings, fast streaks, and clutch comeback moments.",
                    meta = "Many players · Live leaderboard · 100 XP",
                    icon = Icons.Default.Groups,
                    accent = Coral,
                    imageRes = R.drawable.arena_preview_erangel,
                    button = "Find players"
                ) {
                    mode = "erangel"
                }
            }
            item {
                ArenaFeatureCard(
                    title = "Battle Room",
                    eyebrow = "1v1 Duel",
                    body = "Face one rival in a focused answer duel where every correct response hits harder.",
                    meta = "Two players · Health duel · 80 XP",
                    icon = Icons.Default.Shield,
                    accent = Violet,
                    imageRes = R.drawable.arena_preview_battle_room,
                    button = "Find opponent"
                ) {
                    mode = "room"
                }
            }
            item {
                ArenaFeatureCard(
                    title = "Custom Battle",
                    eyebrow = "Private Room",
                    body = "Create a room, invite friends or classmates, and let teachers host class battles on their terms.",
                    meta = "Invite link · Class rooms · Host controls",
                    icon = Icons.Default.GridOn,
                    accent = Moss,
                    imageRes = R.drawable.arena_preview_custom_battle,
                    button = "Notify me",
                    comingSoon = true
                ) {
                    scope.launch { snackbarHostState.showSnackbar("Custom Battle notifications enabled") }
                }
            }
            item {
                ArenaFeatureCard(
                    title = "Scheduled Battle",
                    eyebrow = "Tournament Slot",
                    body = "Plan a battle in advance, send invites, and meet the room exactly when the timer opens.",
                    meta = "Calendar match · Reminders · Invites",
                    icon = Icons.Default.Timer,
                    accent = Blue,
                    imageRes = R.drawable.arena_preview_scheduled_battle,
                    button = "Coming soon",
                    comingSoon = true
                ) {}
            }
            item {
                ArenaFeatureCard(
                    title = "Watch Live",
                    eyebrow = "Spectator Mode",
                    body = "Watch top students battle live, react to tight rounds, and catch featured learning matches.",
                    meta = "Live matches · Reactions · Replays",
                    icon = Icons.Default.Videocam,
                    accent = Gold,
                    imageRes = R.drawable.arena_preview_watch_live,
                    button = "Coming soon",
                    comingSoon = true
                ) {}
            }
        }
    }
}

@Composable
fun BattleFlow(kind: BattleKind, onExit: () -> Unit) {
    var stage by rememberSaveable { mutableStateOf("matchmaking") }
    var countdown by rememberSaveable { mutableIntStateOf(if (kind == BattleKind.Erangel) 10 else 5) }
    var questionIndex by rememberSaveable { mutableIntStateOf(0) }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var seconds by rememberSaveable { mutableIntStateOf(15) }
    var youScore by rememberSaveable { mutableIntStateOf(0) }
    var youHealth by rememberSaveable { mutableIntStateOf(100) }
    var rivalHealth by rememberSaveable { mutableIntStateOf(100) }
    val leaderboard = remember { mutableStateListOf<ArenaPlayer>().apply { addAll(arenaPlayers) } }

    LaunchedEffect(stage) {
        if (stage == "matchmaking") {
            delay(if (kind == BattleKind.Erangel) 3000 else 2000)
            stage = "lobby"
        }
    }
    LaunchedEffect(stage, countdown) {
        if (stage == "lobby" && countdown > 0) {
            delay(1000)
            countdown -= 1
        } else if (stage == "lobby") {
            stage = "quiz"
        }
    }
    LaunchedEffect(stage, questionIndex, selected) {
        if (stage == "quiz" && selected == null && seconds > 0) {
            delay(1000)
            seconds -= 1
        } else if (stage == "quiz" && seconds == 0) {
            selected = ""
        }
    }
    LaunchedEffect(selected) {
        val chosen = selected ?: return@LaunchedEffect
        if (stage != "quiz") return@LaunchedEffect
        val correct = chosen == arenaQuestions[questionIndex].answer
        if (correct) {
            youScore += if (kind == BattleKind.Erangel) 120 else 100
            rivalHealth = (rivalHealth - 20).coerceAtLeast(0)
        } else {
            youHealth = (youHealth - 18).coerceAtLeast(0)
        }
        leaderboard.replaceAll { player ->
            if (player.name == "You") player.copy(score = youScore + if (correct) 120 else 0)
            else player.copy(score = player.score + Random.nextInt(20, 90))
        }
        leaderboard.sortByDescending { it.score }
        delay(900)
        if (questionIndex == arenaQuestions.lastIndex) {
            stage = "results"
        } else {
            questionIndex += 1
            seconds = 15
            selected = null
        }
    }

    ArenaList {
        item {
            ArenaHeader(
                if (kind == BattleKind.Erangel) "Erangel" else "Battle Room",
                "Fast questions. Fast rewards.",
                "Modes"
            ) { onExit() }
        }
        item {
            when (stage) {
                "matchmaking" -> MatchmakingCard(if (kind == BattleKind.Erangel) "Finding players..." else "Finding opponent...")
                "lobby" -> LobbyCard(kind, countdown)
                "quiz" -> BattleQuizCard(
                    kind = kind,
                    question = arenaQuestions[questionIndex],
                    number = questionIndex + 1,
                    seconds = seconds,
                    selected = selected,
                    onSelect = { if (selected == null) selected = it },
                    leaderboard = leaderboard,
                    youHealth = youHealth,
                    rivalHealth = rivalHealth,
                    youScore = youScore
                )

                "results" -> BattleResultCard(
                    kind = kind,
                    won = if (kind == BattleKind.Room) rivalHealth <= youHealth else leaderboard.firstOrNull()?.name == "You",
                    score = youScore,
                    leaderboard = leaderboard,
                    onPlayAgain = {
                        stage = "matchmaking"
                        questionIndex = 0
                        selected = null
                        countdown = if (kind == BattleKind.Erangel) 10 else 5
                        seconds = 15
                        youScore = 0
                        youHealth = 100
                        rivalHealth = 100
                    }
                )
            }
        }
    }
}

@Composable
fun MatchmakingCard(text: String) {
    val pulse by rememberInfiniteTransition(label = "matchPulse").animateFloat(
        initialValue = 0.86f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            tween(760, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulse"
    )
    PremiumPanel(accent = Coral) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(260.dp), contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .size(122.dp)
                    .graphicsLayer { scaleX = pulse; scaleY = pulse }
                    .clip(CircleShape)
                    .background(Coral.copy(alpha = 0.12f)))
            Icon(Icons.Default.Shield, null, tint = Coral, modifier = Modifier.size(54.dp))
            Text(
                text,
                modifier = Modifier.align(Alignment.BottomCenter),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )
        }
    }
}

@Composable
fun LobbyCard(kind: BattleKind, countdown: Int) {
    PremiumPanel(accent = if (kind == BattleKind.Erangel) Coral else Violet) {
        Text(
            "Battle starts in $countdown",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Ink
        )
        Spacer(Modifier.height(12.dp))
        if (kind == BattleKind.Erangel) {
            arenaPlayers.forEach { PlayerRow(it) }
        } else {
            PlayerRow(ArenaPlayer("You", "Grade 10", "YO"))
            PlayerRow(ArenaPlayer("Naina", "Grade 10", "NA"))
        }
    }
}

@Composable
fun PlayerRow(player: ArenaPlayer) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Mint),
            contentAlignment = Alignment.Center
        ) {
            Text(player.avatar, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(player.name, color = Ink, fontWeight = FontWeight.Bold)
            Text(player.grade, color = ProLearnColors.MutedDark, fontSize = 12.sp)
        }
        Text("${player.score}", color = Moss, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BattleQuizCard(
    kind: BattleKind,
    question: ArenaQuestion,
    number: Int,
    seconds: Int,
    selected: String?,
    onSelect: (String) -> Unit,
    leaderboard: List<ArenaPlayer>,
    youHealth: Int,
    rivalHealth: Int,
    youScore: Int
) {
    PremiumPanel(accent = if (kind == BattleKind.Erangel) Coral else Violet) {
        if (kind == BattleKind.Room) {
            HealthDuel(youHealth, rivalHealth)
            Spacer(Modifier.height(12.dp))
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(
                "Question $number/5",
                color = ProLearnColors.MutedDark,
                fontWeight = FontWeight.Bold
            )
            Text("${seconds}s", color = Coral, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { seconds / 15f },
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(100.dp)),
            color = if (seconds > 5) Moss else Coral,
            trackColor = SoftBorder
        )
        Spacer(Modifier.height(14.dp))
        Text(
            question.prompt,
            fontSize = 22.sp,
            lineHeight = 27.sp,
            color = Ink,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(12.dp))
        question.options.forEach { option ->
            AnswerOption(option, selected, question.answer) { onSelect(option) }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(8.dp))
        if (kind == BattleKind.Erangel) {
            Text("Live leaderboard", color = Ink, fontWeight = FontWeight.Bold)
            leaderboard.take(6).forEachIndexed { index, player ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${index + 1}. ${player.name}",
                        color = if (player.name == "You") Moss else ProLearnColors.MutedDark,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("${player.score}", color = Ink, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Text(
                "Score $youScore",
                color = Moss,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun AnswerOption(option: String, selected: String?, answer: String, onClick: () -> Unit) {
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
            .border(
                1.dp,
                if (isCorrect || isSelected) color else SoftBorder,
                RoundedCornerShape(18.dp)
            )
            .clickable(enabled = selected == null, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(option, Modifier.weight(1f), color = Ink, fontWeight = FontWeight.SemiBold)
        AnimatedVisibility(selected != null && (isCorrect || isSelected)) {
            Icon(
                if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun HealthDuel(you: Int, rival: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        HealthBar("You", you, Moss, Modifier.weight(1f))
        HealthBar("Naina", rival, Coral, Modifier.weight(1f))
    }
}

@Composable
fun HealthBar(name: String, value: Int, color: Color, modifier: Modifier) {
    Column(modifier) {
        Text("$name $value", fontSize = 12.sp, color = Ink, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(SoftBorder)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(value / 100f)
                    .fillMaxHeight()
                    .background(color)
            )
        }
    }
}

@Composable
fun BattleResultCard(
    kind: BattleKind,
    won: Boolean,
    score: Int,
    leaderboard: List<ArenaPlayer>,
    onPlayAgain: () -> Unit
) {
    val xp = if (kind == BattleKind.Erangel) if (won) 100 else 30 else if (won) 80 else 25
    var awarded by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!awarded) {
            ArenaXpStore.addXp(xp)
            awarded = true
        }
    }
    PremiumPanel(accent = if (won) Gold else Coral) {
        Confetti()
        Text(
            if (won) "Victory" else "Battle complete",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Score $score · XP earned $xp",
            color = ProLearnColors.MutedDark,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        if (kind == BattleKind.Erangel) {
            Spacer(Modifier.height(12.dp))
            leaderboard.take(3).forEachIndexed { index, player ->
                Text(
                    "#${index + 1} ${player.name} · ${player.score}",
                    color = Ink,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        MiniButton("Watch Replay — Coming Soon") { }
        Spacer(Modifier.height(10.dp))
        PremiumButton(
            if (kind == BattleKind.Room) "Rematch" else "Play Again",
            Icons.Default.PlayArrow,
            onPlayAgain
        )
    }
}
