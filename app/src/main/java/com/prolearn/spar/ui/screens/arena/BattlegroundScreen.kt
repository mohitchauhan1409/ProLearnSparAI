package com.prolearn.spar.ui.screens.arena

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WorkspacePremium
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.R
import com.prolearn.spar.ui.theme.BricolageGrotesqueFamily
import com.prolearn.spar.ui.theme.ProLearnColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private enum class BattleKind { Erangel, Duel }
private enum class BattlegroundView { Modes, ArenaPicker, Playbook, Matchmaking, Lobby, Quiz, Results }

@Composable
fun BattlegroundScreen(
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var view by rememberSaveable { mutableStateOf(BattlegroundView.Modes) }
    var kind by rememberSaveable { mutableStateOf(BattleKind.Erangel) }
    var arenaId by rememberSaveable { mutableStateOf(battleArenas.first().id) }
    val arena = battleArenas.first { it.id == arenaId }
    val scope = rememberCoroutineScope()

    BackHandler {
        when (view) {
            BattlegroundView.Modes -> onBack()
            BattlegroundView.ArenaPicker -> view = BattlegroundView.Modes
            BattlegroundView.Playbook -> view = BattlegroundView.ArenaPicker
            BattlegroundView.Matchmaking,
            BattlegroundView.Lobby,
            BattlegroundView.Quiz,
            BattlegroundView.Results -> view = BattlegroundView.ArenaPicker
        }
    }

    when (view) {
        BattlegroundView.Modes -> ArenaList {
            item { ArenaHeader("Battleground", "Choose your combat style.", "Arena") { onBack() } }
            item {
                ArenaFeatureCard(
                    title = "Learn Erangel",
                    eyebrow = "Squad Battle",
                    body = "Drop into a many-vs-many quiz battle with live rankings, fast streaks, and clutch comeback moments.",
                    meta = "Many players · Live leaderboard · 100 XP",
                    icon = Icons.Default.Groups,
                    accent = Coral,
                    imageRes = R.drawable.arena_preview_erangel,
                    button = "Start Battle"
                ) {
                    kind = BattleKind.Erangel
                    view = BattlegroundView.ArenaPicker
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
                    button = "Start Battle"
                ) {
                    kind = BattleKind.Duel
                    view = BattlegroundView.ArenaPicker
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
                    button = "Notify me",
                    comingSoon = true
                ) {
                    scope.launch { snackbarHostState.showSnackbar("Custom Battle notifications enabled") }
                }
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
                    button = "Notify me",
                    comingSoon = true
                ) {
                    scope.launch { snackbarHostState.showSnackbar("Custom Battle notifications enabled") }
                }
            }
        }

        BattlegroundView.ArenaPicker -> ArenaPicker(
            kind = kind,
            selectedArena = arena,
            onPlaybook = {
                arenaId = it.id
                view = BattlegroundView.Playbook
            },
            onStart = {
                arenaId = it.id
                view = BattlegroundView.Matchmaking
            }
        )

        BattlegroundView.Playbook -> BattlePlaybook(
            kind = kind,
            arena = arena,
            onBack = { view = BattlegroundView.ArenaPicker },
            onStart = { view = BattlegroundView.Matchmaking }
        )

        BattlegroundView.Matchmaking,
        BattlegroundView.Lobby,
        BattlegroundView.Quiz,
        BattlegroundView.Results -> BattleFlow(
            initialView = view,
            kind = kind,
            arena = arena,
            onStageChange = { view = it },
            onExit = { view = BattlegroundView.ArenaPicker }
        )
    }
}

@Composable
private fun ArenaPicker(
    kind: BattleKind,
    selectedArena: BattleArena,
    onPlaybook: (BattleArena) -> Unit,
    onStart: (BattleArena) -> Unit
) {
    val arenaRows = battleArenas.chunked(2)

    ArenaList {
        item {
            ArenaHeader(
                title = if (kind == BattleKind.Erangel) "Select Subject" else "1v1 Battle Room",
                subtitle = "Choose a subject and begin your battle.",                action = "Read playbook",
                onAction = { onPlaybook(selectedArena) }
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
        items(arenaRows.size) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                arenaRows[rowIndex].forEach { arena ->
                    SubjectArenaCard(
                        arena = arena,
                        modifier = Modifier.weight(1f),
                        onStart = { onStart(arena) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BattleFlow(
    initialView: BattlegroundView,
    kind: BattleKind,
    arena: BattleArena,
    onStageChange: (BattlegroundView) -> Unit,
    onExit: () -> Unit
) {
    var stage by rememberSaveable(arena.id, kind) { mutableStateOf(initialView) }
    var lobbyCountdown by rememberSaveable(arena.id, kind) { mutableIntStateOf(5) }
    var questionIndex by rememberSaveable(arena.id, kind) { mutableIntStateOf(0) }
    var readSeconds by rememberSaveable(arena.id, kind) { mutableIntStateOf(10) }
    var answerSeconds by rememberSaveable(arena.id, kind) { mutableIntStateOf(60) }
    var selected by rememberSaveable(arena.id, kind) { mutableStateOf<String?>(null) }
    var score by rememberSaveable(arena.id, kind) { mutableIntStateOf(0) }
    var xp by rememberSaveable(arena.id, kind) { mutableIntStateOf(0) }
    var youHealth by rememberSaveable(arena.id, kind) { mutableIntStateOf(100) }
    var rivalHealth by rememberSaveable(arena.id, kind) { mutableIntStateOf(100) }
    val players = remember(arena.id, kind) {
        mutableStateListOf<ArenaPlayer>().apply {
            addAll(if (kind == BattleKind.Erangel) battlePlayers else duelPlayers)
        }
    }

    fun switchStage(next: BattlegroundView) {
        stage = next
        onStageChange(next)
    }

    LaunchedEffect(stage) {
        when (stage) {
            BattlegroundView.Matchmaking -> {
                delay(if (kind == BattleKind.Erangel) 4300 else 2700)
                switchStage(BattlegroundView.Lobby)
            }

            BattlegroundView.Lobby -> {
                while (lobbyCountdown > 0) {
                    delay(1000)
                    lobbyCountdown -= 1
                }
                switchStage(BattlegroundView.Quiz)
            }

            else -> Unit
        }
    }

    LaunchedEffect(stage, questionIndex, readSeconds) {
        if (stage == BattlegroundView.Quiz && selected == null && readSeconds > 0) {
            delay(1000)
            readSeconds -= 1
        }
    }

    LaunchedEffect(stage, questionIndex, readSeconds, answerSeconds, selected) {
        if (stage == BattlegroundView.Quiz && readSeconds == 0 && selected == null && answerSeconds > 0) {
            delay(1000)
            answerSeconds -= 1
        } else if (stage == BattlegroundView.Quiz && answerSeconds == 0 && selected == null) {
            selected = ""
        }
    }

    LaunchedEffect(selected) {
        val chosen = selected ?: return@LaunchedEffect
        if (stage != BattlegroundView.Quiz) return@LaunchedEffect
        val question = arena.questions[questionIndex]
        val correct = chosen == question.answer
        val answeredIn = if (chosen.isBlank()) null else 60 - answerSeconds
        xp += if (correct) 5 else -1
        score += if (correct) 100 + answerSeconds else 0
        if (kind == BattleKind.Duel) {
            if (correct) rivalHealth = (rivalHealth - 24).coerceAtLeast(0) else youHealth = (youHealth - 18).coerceAtLeast(0)
        }
        players.replaceAll { player ->
            if (player.name == "You") {
                player.copy(
                    score = score + if (correct) 100 + answerSeconds else 0,
                    xp = xp + if (correct) 5 else -1,
                    answeredIn = answeredIn,
                    streak = if (correct) player.streak + 1 else 0,
                    health = youHealth
                )
            } else {
                val botCorrect = Random.nextInt(100) > if (kind == BattleKind.Erangel) 26 else 38
                val botTime = Random.nextInt(8, 51)
                val gain = if (botCorrect) 5 else -1
                player.copy(
                    score = player.score + if (botCorrect) 100 + (60 - botTime) else Random.nextInt(0, 35),
                    xp = player.xp + gain,
                    answeredIn = botTime,
                    streak = if (botCorrect) player.streak + 1 else 0,
                    health = if (kind == BattleKind.Duel) rivalHealth else player.health
                )
            }
        }
        players.sortByDescending { it.score }
        delay(1200)
        if (questionIndex == arena.questions.lastIndex || youHealth <= 0 || rivalHealth <= 0) {
            switchStage(BattlegroundView.Results)
        } else {
            questionIndex += 1
            readSeconds = 10
            answerSeconds = 60
            selected = null
        }
    }

    ArenaList {
        item {
            ArenaHeader(
                title = arena.subject,
                subtitle = if (kind == BattleKind.Erangel) "10-player ranked battle" else "Rank-matched 1v1 duel",
                action = "Exit",
                onAction = onExit
            )
        }
        item {
            when (stage) {
                BattlegroundView.Matchmaking -> MatchmakingCard(kind, arena, players)
                BattlegroundView.Lobby -> LobbyCard(kind, arena, lobbyCountdown, players)
                BattlegroundView.Quiz -> BattleQuizCard(
                    kind = kind,
                    arena = arena,
                    question = arena.questions[questionIndex],
                    number = questionIndex + 1,
                    total = arena.questions.size,
                    readSeconds = readSeconds,
                    answerSeconds = answerSeconds,
                    selected = selected,
                    onSelect = { if (selected == null && readSeconds == 0) selected = it },
                    players = players,
                    youHealth = youHealth,
                    rivalHealth = rivalHealth,
                    currentXp = xp
                )

                BattlegroundView.Results -> BattleResultCard(
                    kind = kind,
                    arena = arena,
                    players = players,
                    score = score,
                    baseXp = xp,
                    onPlayAgain = {
                        players.clear()
                        players.addAll(if (kind == BattleKind.Erangel) battlePlayers else duelPlayers)
                        lobbyCountdown = 5
                        questionIndex = 0
                        readSeconds = 10
                        answerSeconds = 60
                        selected = null
                        score = 0
                        xp = 0
                        youHealth = 100
                        rivalHealth = 100
                        switchStage(BattlegroundView.Matchmaking)
                    },
                    onChangeArena = onExit
                )

                else -> Unit
            }
        }
    }
}

@Composable
private fun BattleModeHero(
    title: String,
    eyebrow: String,
    body: String,
    meta: String,
    icon: ImageVector,
    accent: Color,
    imageRes: Int,
    onClick: () -> Unit
) {
    ArenaFeatureCard(
        title = title,
        eyebrow = eyebrow,
        body = body,
        meta = meta,
        icon = icon,
        accent = accent,
        imageRes = imageRes,
        button = "Open"
    ) { onClick() }
}

@Composable
private fun BattleSummaryStrip(kind: BattleKind, arena: BattleArena) {
    PremiumPanel(accent = arena.accent) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatPill("Players", if (kind == BattleKind.Erangel) "10" else "2", Icons.Default.Groups, arena.accent, Modifier.weight(1f))
            StatPill("Max XP", "${if (kind == BattleKind.Erangel) arena.maxXp else 80}", Icons.Default.WorkspacePremium, Gold, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatPill("Timer", "10s + 60s", Icons.Default.Timer, Coral, Modifier.weight(1f))
            StatPill("Match", arena.difficulty, Icons.Default.Radar, Moss, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SubjectArenaCard(
    arena: BattleArena,
    modifier: Modifier = Modifier,
    onStart: () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)

    Image(
        painter = painterResource(arena.imageRes),
        contentDescription = arena.subject,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .aspectRatio(1f)
            .shadow(7.dp, shape)
            .clip(shape)
            .border(1.dp, Color.White.copy(alpha = 0.82f), shape)
            .clickable(onClick = onStart)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BattleInfoChips(arena: BattleArena, kind: BattleKind) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InfoChip("${if (kind == BattleKind.Erangel) arena.players else 2} players", arena.accent)
        InfoChip("${if (kind == BattleKind.Erangel) arena.maxXp else 80} max XP", Gold)
        InfoChip(arena.exam, Blue)
        InfoChip(arena.questionPool, Moss)
    }
}

@Composable
private fun BattlePlaybook(
    kind: BattleKind,
    arena: BattleArena,
    onBack: () -> Unit,
    onStart: () -> Unit
) {
    ArenaList {
        item { ArenaHeader("Playbook", arena.subject, "Arenas") { onBack() } }
        item {
            PremiumPanel(accent = arena.accent) {
                Text(
                    "How this battle works",
                    fontSize = 28.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Ink,
                    fontFamily = BricolageGrotesqueFamily
                )
                Spacer(Modifier.height(10.dp))
                PlaybookRow(Icons.Default.Groups, "Matchmaking", "You are matched with ${if (kind == BattleKind.Erangel) "9 other students" else "1 student"} near your internal ranking level, based on accuracy, speed, streaks, and recent battle form.")
                PlaybookRow(Icons.AutoMirrored.Filled.MenuBook, "Question source", "${arena.questionPool}. Every question shows whether it is PYQ-tagged or specially curated.")
                PlaybookRow(Icons.Default.Timer, "Question rhythm", "First 10 seconds are reading-only. Options unlock after that. You then get 60 seconds to answer.")
                PlaybookRow(Icons.Default.LocalFireDepartment, "XP scoring", "Correct answer adds 5 XP. Wrong answer deducts 1 XP. Speed and streaks push leaderboard score.")
                PlaybookRow(Icons.Default.EmojiEvents, "Winner rewards", if (kind == BattleKind.Erangel) "Top 3 receive podium bonuses: #1 +50 XP, #2 +40 XP, #3 +30 XP." else "The duel winner receives a +50 XP bonus. The runner-up keeps earned question XP.")
                PlaybookRow(Icons.Default.Shield, "Fairness", "Everyone sees the same question at the same time, with answer timing visible below the question.")
                Spacer(Modifier.height(14.dp))
                SolidAction("Start ${if (kind == BattleKind.Erangel) "Erangel" else "1v1 Battle"}", Icons.AutoMirrored.Filled.ArrowForward, Modifier.fillMaxWidth(), onStart)
            }
        }
    }
}

@Composable
private fun PlaybookRow(icon: ImageVector, title: String, body: String) {
    Row(Modifier.padding(vertical = 9.dp), verticalAlignment = Alignment.Top) {
        IconBadge(icon, Coral, 34)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            Text(body, color = ProLearnColors.MutedDark, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun MatchmakingCard(kind: BattleKind, arena: BattleArena, players: List<ArenaPlayer>) {
    val pulse by rememberInfiniteTransition(label = "matchPulse").animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(760, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val rotate by rememberInfiniteTransition(label = "matchOrbit").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4200, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "orbit"
    )

    PremiumPanel(accent = arena.accent, bgColor = Color.Transparent) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp, top = 200.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Animation area ──────────────────────────────────────────
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .size(124.dp)
                        .graphicsLayer { scaleX = pulse; scaleY = pulse }
                        .clip(CircleShape)
                        .background(arena.accent.copy(alpha = 0.14f))
                        .border(1.dp, arena.accent.copy(alpha = 0.22f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Radar, null, tint = arena.accent, modifier = Modifier.size(54.dp))
                }

                val visiblePlayers = if (kind == BattleKind.Erangel) players else players.take(2)
                visiblePlayers.forEachIndexed { index, player ->
                    val angle = Math.toRadians((index * (360.0 / visiblePlayers.size)) + rotate)
                    StudentAvatar(
                        player = player,
                        modifier = Modifier.offset(
                            x = (cos(angle) * 124).dp,
                            y = (sin(angle) * 124).dp
                        ),
                        size = 48
                    )
                }
            }

            Spacer(Modifier.height(38.dp))
            Text(
                if (kind == BattleKind.Erangel) "Finding 10 rank-matched students" else "Finding your duel opponent",
                color = Ink,
                fontSize = 23.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${arena.subject} · ${arena.exam} · ${arena.difficulty}",
                color = ProLearnColors.MutedDark,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
private fun LobbyCard(kind: BattleKind, arena: BattleArena, countdown: Int, players: List<ArenaPlayer>) {
    PremiumPanel(accent = arena.accent) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Battle starts in $countdown",
                modifier = Modifier.weight(1f),
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Ink,
                fontFamily = BricolageGrotesqueFamily
            )
            InfoChip(if (kind == BattleKind.Erangel) "10 locked" else "2 locked", arena.accent)
        }
        Spacer(Modifier.height(12.dp))
        players.forEach { PlayerRow(it, showTiming = false) }
    }
}

@Composable
private fun BattleQuizCard(
    kind: BattleKind,
    arena: BattleArena,
    question: ArenaQuestion,
    number: Int,
    total: Int,
    readSeconds: Int,
    answerSeconds: Int,
    selected: String?,
    onSelect: (String) -> Unit,
    players: List<ArenaPlayer>,
    youHealth: Int,
    rivalHealth: Int,
    currentXp: Int
) {
    PremiumPanel(accent = arena.accent) {
        if (kind == BattleKind.Duel) {
            HealthDuel(youHealth, rivalHealth)
            Spacer(Modifier.height(12.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            InfoChip("Q$number/$total", arena.accent)
            Spacer(Modifier.width(8.dp))
            InfoChip(question.tag, Gold)
            Spacer(Modifier.weight(1f))
            Text(
                if (readSeconds > 0) "Read ${readSeconds}s" else "${answerSeconds}s",
                color = if (readSeconds > 0 || answerSeconds > 12) Moss else Coral,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        }
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { if (readSeconds > 0) readSeconds / 10f else answerSeconds / 60f },
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(100.dp)),
            color = if (readSeconds > 0 || answerSeconds > 12) arena.accent else Coral,
            trackColor = SoftBorder
        )
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
            ReadingLockout(readSeconds)
        }
        AnimatedVisibility(readSeconds == 0) {
            Column {
                question.options.forEachIndexed { index, option ->
                    BattleAnswerOption("${'A' + index}", option, selected, question.answer) { onSelect(option) }
                    Spacer(Modifier.height(9.dp))
                }
                if (selected != null) {
                    Text(question.explanation, color = ProLearnColors.MutedDark, fontSize = 13.sp, lineHeight = 18.sp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Your XP: $currentXp", color = Moss, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.weight(1f))
            Text("Answered live", color = Ink, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        players.take(if (kind == BattleKind.Erangel) 6 else 2).forEach { PlayerRow(it, showTiming = true) }
    }
}

@Composable
private fun ReadingLockout(seconds: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Mint.copy(alpha = 0.8f))
            .border(1.dp, SoftBorder, RoundedCornerShape(20.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = Moss, modifier = Modifier.size(28.dp))
        Text(
            "Read carefully. Options unlock in $seconds seconds.",
            color = Ink,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Text(
            "Every student sees the same question before answering begins.",
            color = ProLearnColors.MutedDark,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
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
fun AnswerOption(option: String, selected: String?, answer: String, onClick: () -> Unit) {
    BattleAnswerOption("", option, selected, answer, onClick)
}

@Composable
private fun PlayerRow(player: ArenaPlayer, showTiming: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StudentAvatar(player, Modifier, 42)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(player.name, color = Ink, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(player.grade, color = ProLearnColors.MutedDark, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (showTiming) {
            Text(
                player.answeredIn?.let { "${it}s · ${player.xp} XP" } ?: "reading",
                color = if (player.name == "You") Moss else ProLearnColors.MutedDark,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        } else {
            Text("${player.score}", color = Moss, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StudentAvatar(player: ArenaPlayer, modifier: Modifier = Modifier, size: Int = 44) {
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
private fun HealthDuel(you: Int, rival: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        HealthBar("You", you, Moss, Modifier.weight(1f))
        HealthBar("Rival", rival, Coral, Modifier.weight(1f))
    }
}

@Composable
private fun HealthBar(name: String, value: Int, color: Color, modifier: Modifier) {
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
private fun BattleResultCard(
    kind: BattleKind,
    arena: BattleArena,
    players: List<ArenaPlayer>,
    score: Int,
    baseXp: Int,
    onPlayAgain: () -> Unit,
    onChangeArena: () -> Unit
) {
    val yourRank = players.indexOfFirst { it.name == "You" }.let { if (it == -1) players.size else it + 1 }
    val podiumBonus = if (kind == BattleKind.Erangel) when (yourRank) {
        1 -> 50
        2 -> 40
        3 -> 30
        else -> 0
    } else if (yourRank == 1) 50 else 0
    val earned = (baseXp + podiumBonus).coerceAtLeast(0)
    var awarded by rememberSaveable(arena.id, kind) { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!awarded) {
            ArenaXpStore.addXp(earned)
            awarded = true
        }
    }
    PremiumPanel(accent = if (yourRank == 1) Gold else arena.accent) {
        Confetti()
        Text(
            if (yourRank == 1) "Victory" else "Battle complete",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            fontFamily = BricolageGrotesqueFamily
        )
        Text(
            "Rank #$yourRank · Score $score · XP earned $earned",
            color = ProLearnColors.MutedDark,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(14.dp))
        players.take(if (kind == BattleKind.Erangel) 5 else 2).forEachIndexed { index, player ->
            PodiumRow(index + 1, player, player.name == "You")
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlineAction("Change Arena", Icons.AutoMirrored.Filled.MenuBook, arena.accent, Modifier.weight(1f), onChangeArena)
            SolidAction("Play Again", Icons.Default.PlayArrow, Modifier.weight(1f), onPlayAgain)
        }
    }
}

@Composable
private fun PodiumRow(rank: Int, player: ArenaPlayer, isYou: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isYou) Mint.copy(alpha = 0.82f) else Color.White.copy(alpha = 0.62f))
            .border(1.dp, if (isYou) Moss.copy(alpha = 0.24f) else SoftBorder, RoundedCornerShape(18.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("#$rank", color = if (rank <= 3) Gold else Ink, fontWeight = FontWeight.ExtraBold, modifier = Modifier.width(34.dp))
        StudentAvatar(player, size = 36)
        Spacer(Modifier.width(10.dp))
        Text(player.name, Modifier.weight(1f), color = Ink, fontWeight = FontWeight.Bold)
        Text("${player.score}", color = Moss, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun InfoChip(text: String, accent: Color) {
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

@Composable
private fun OutlineAction(text: String, icon: ImageVector, accent: Color, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.72f))
            .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(7.dp))
        Text(text, color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, maxLines = 1)
    }
}

@Composable
private fun SolidAction(text: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Ink)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, maxLines = 1)
        Spacer(Modifier.width(7.dp))
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(17.dp))
    }
}
