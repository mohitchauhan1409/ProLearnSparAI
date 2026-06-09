package com.prolearn.spar.ui.screens.arena

import android.webkit.WebView
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.prolearn.spar.R
import com.prolearn.spar.ui.components.navigation.MainTab
import com.prolearn.spar.ui.components.navigation.ProLearnBottomNav
import com.prolearn.spar.ui.theme.BricolageGrotesqueFamily
import com.prolearn.spar.ui.theme.ProLearnColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private val PageBg = Color(0xFFF7F6F1)
private val Ink = Color(0xFF171A17)
private val Moss = Color(0xFF506B5F)
private val Gold = Color(0xFFB8924B)
private val Coral = Color(0xFFA85F4E)
private val Blue = Color(0xFF536B82)
private val Violet = Color(0xFF665E76)
private val SoftBorder = Color(0xFFE2DED4)
private val GlassStroke = Color(0x88FFFFFF)
private val Mint = Color(0xFFE9EEE3)
private val Sky = Color(0xFFE7ECEE)
private val Blush = Color(0xFFF1E8E3)
private val Paper = Color(0xFFFFFCF6)

private enum class ArenaPage { Home, Battleground, Challenges, Games }
private enum class BattleKind { Erangel, Room }

@Composable
fun ArenaScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var page by rememberSaveable { mutableStateOf(ArenaPage.Home) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = PageBg,
        bottomBar = {
            ProLearnBottomNav(
                selected = MainTab.Arena,
                onHome = onNavigateToHome,
                onArena = { page = ArenaPage.Home },
                onProfile = onNavigateToProfile
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(PageBg, Color(0xFFFBFAF5), Color(0xFFF1EFE8)),
                        start = Offset.Zero,
                        end = Offset(900f, 1600f)
                    )
                )
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            ArenaAmbient()
            when (page) {
                ArenaPage.Home -> ArenaHome(
                    onBattleground = { page = ArenaPage.Battleground },
                    onChallenges = { page = ArenaPage.Challenges },
                    onGames = { page = ArenaPage.Games }
                )

                ArenaPage.Battleground -> BattlegroundScreen(
                    onBack = { page = ArenaPage.Home },
                    snackbarHostState = snackbarHostState
                )

                ArenaPage.Challenges -> ChallengesScreen(
                    onBack = { page = ArenaPage.Home },
                    snackbarHostState = snackbarHostState
                )

                ArenaPage.Games -> GamesScreen(
                    onBack = { page = ArenaPage.Home },
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}

@Composable
private fun ArenaHome(
    onBattleground: () -> Unit,
    onChallenges: () -> Unit,
    onGames: () -> Unit
) {
    val xp by ArenaXpStore.totalXp.collectAsState()
    val rank = ArenaXpStore.rankFor(xp)
    val nextRankXp = when (rank) {
        "Bronze" -> 250
        "Silver" -> 500
        "Gold" -> 800
        "Diamond" -> 1200
        else -> xp
    }
    val rankProgress = if (rank == "Legend") 1f else (xp.toFloat() / nextRankXp).coerceIn(0f, 1f)

    ArenaList {
        item {
            ArenaHero(
                xp = xp,
                rank = rank,
                progress = rankProgress,
                nextRankXp = nextRankXp,
                onBattle = onBattleground
            )
        }
//        item {
//            ArenaBriefingSection()
//        }
//        item {
//            ArenaLiveStrip()
//        }
        item {
            SectionTitle(
                "Choose your format",
                "Every Arena mode turns revision into a visible skill loop."
            )
        }
        item {
            ArenaDestinationCard(
                title = "Battleground",
                kicker = "LIVE MATCHMAKING",
                body = "Jump into Erangel squads or a 1v1 Battle Room. Scores move after every answer.",
                artRes = R.drawable.arena_art_battleground,
                accent = Coral,
                reward = "Up to 100 XP",
                players = "6 online",
                primary = true,
                button = "Find battle",
                onClick = onBattleground
            )
        }
        item {
            ArenaDestinationCard(
                title = "Challenges",
                kicker = "FOCUS MISSIONS",
                body = "Read, watch, answer timestamp checks, and prove recall before XP unlocks.",
                artRes = R.drawable.arena_art_challenges,
                accent = Blue,
                reward = "50 XP each",
                players = "2 ready",
                primary = false,
                button = "Open missions",
                onClick = onChallenges
            )
        }
        item {
            ArenaDestinationCard(
                title = "Games",
                kicker = "PLAY TO MASTER",
                body = "Knowledge Ludo and Word Battlefield turn syllabus recall into real game moves.",
                artRes = R.drawable.arena_art_games,
                accent = Moss,
                reward = "Up to 70 XP",
                players = "4 modes",
                primary = false,
                button = "Enter arcade",
                onClick = onGames
            )
        }
//        item {
//            ArenaHowItWorks()
//        }
//        item {
//            ArenaTrainingPlan()
//        }
    }
}

@Composable
private fun BattlegroundScreen(
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var mode by rememberSaveable { mutableStateOf<String?>(null) }
    when (mode) {
        "erangel" -> BattleFlow(BattleKind.Erangel, onExit = { mode = null })
        "room" -> BattleFlow(BattleKind.Room, onExit = { mode = null })
        else -> ArenaList {
            item { ArenaHeader("Battleground", "Choose your combat style.", "Arena") { onBack() } }
            item {
                ModeCard(
                    "Erangel",
                    "Many vs many quiz battle with a live leaderboard.",
                    Icons.Default.Groups,
                    Coral,
                    "Find players"
                ) {
                    mode = "erangel"
                }
            }
            item {
                ModeCard(
                    "Battle Room",
                    "A sharp 1v1 duel with health bars and instant damage.",
                    Icons.Default.Shield,
                    Violet,
                    "Find opponent"
                ) {
                    mode = "room"
                }
            }
            item {
                ComingSoonCard(
                    "Custom Battle",
                    "Create your own room, invite friends or classmates, and battle on your terms. Your teacher can also host a class battle here.",
                    Icons.Default.GridOn,
                    snackbarHostState
                )
            }
            item {
                ComingSoonStatic(
                    "Scheduled Battle",
                    "Schedule a battle in advance, send invites, and fight at a set time. Like a planned match.",
                    Icons.Default.Timer
                )
            }
            item {
                ComingSoonStatic(
                    "Watch Live",
                    "Watch top students battle live and react with emojis. The best battles get featured here.",
                    Icons.Default.Videocam
                )
            }
        }
    }
}

@Composable
private fun BattleFlow(kind: BattleKind, onExit: () -> Unit) {
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
private fun ChallengesScreen(
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var challenge by rememberSaveable { mutableStateOf<String?>(null) }
    when (challenge) {
        "pdf" -> PdfChallenge { challenge = null }
        "youtube" -> YouTubeChallenge { challenge = null }
        else -> ArenaList {
            item { ArenaHeader("Challenges", "Study, test, recall, earn.", "Arena") { onBack() } }
            item {
                ChallengeCard(
                    "PDF Challenge",
                    "Biology",
                    "4 min",
                    "50 XP",
                    Icons.Default.PictureAsPdf,
                    Blue
                ) { challenge = "pdf" }
            }
            item {
                ChallengeCard(
                    "YouTube Challenge",
                    "Physics",
                    "5 min",
                    "50 XP",
                    Icons.Default.PlayCircle,
                    Coral
                ) { challenge = "youtube" }
            }
            item {
                ComingSoonStatic(
                    "Image Challenge",
                    "You will be shown a diagram, circuit, or biology figure and asked to label or answer questions about it. Perfect for NEET and board exams.",
                    Icons.Default.Science
                )
            }
            item {
                ComingSoonCard(
                    "Live Debate Challenge",
                    "Get a topic, type your argument, and let other students vote on who wins. Great for CUET and critical thinking practice.",
                    Icons.Default.Groups,
                    snackbarHostState
                )
            }
        }
    }
}

@Composable
private fun PdfChallenge(onExit: () -> Unit) {
    var stage by rememberSaveable { mutableStateOf("read") }
    var timer by rememberSaveable { mutableIntStateOf(60) }
    var answers by rememberSaveable { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var recall by rememberSaveable { mutableStateOf("") }
    var awarded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(stage, timer) {
        if (stage == "read" && timer > 0) {
            delay(1000)
            timer -= 1
        } else if (stage == "read") {
            stage = "test"
        }
    }

    ArenaList {
        item { ArenaHeader("PDF Challenge", "Photosynthesis sprint.", "Challenges") { onExit() } }
        item {
            when (stage) {
                "read" -> PremiumPanel {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatPill(
                            "Reading timer",
                            "${timer}s",
                            Icons.Default.Timer,
                            Coral,
                            Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    PdfText()
                    Spacer(Modifier.height(14.dp))
                    PremiumButton("Done reading", Icons.Default.CheckCircle) { stage = "test" }
                }

                "test" -> TestPanel(
                    questions = pdfQuestions,
                    answers = answers,
                    onAnswer = { index, value -> answers = answers + (index to value) },
                    onSubmit = { stage = "score" }
                )

                "score" -> ScorePanel(
                    title = "PDF Score",
                    questions = pdfQuestions,
                    answers = answers,
                    xp = 50,
                    onContinue = {
                        if (!awarded) {
                            ArenaXpStore.addXp(50)
                            awarded = true
                        }
                        stage = "recall"
                    }
                )

                "recall" -> PremiumPanel {
                    Text(
                        "Recall Round",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        fontFamily = BricolageGrotesqueFamily
                    )
                    Text(
                        "Write what you remember from the reading.",
                        fontSize = 13.sp,
                        color = ProLearnColors.MutedDark
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = recall,
                        onValueChange = { recall = it },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Type your recall notes...") })
                    Spacer(Modifier.height(12.dp))
                    PremiumButton("Submit recall", Icons.Default.AutoAwesome) { stage = "summary" }
                }

                else -> SummaryPanel(
                    "Challenge complete",
                    "You captured ${
                        recall.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
                    } recall words and earned your PDF Challenge XP.",
                    onExit
                )
            }
        }
    }
}

@Composable
private fun YouTubeChallenge(onExit: () -> Unit) {
    var stage by rememberSaveable { mutableStateOf("watch") }
    var progress by rememberSaveable { mutableIntStateOf(0) }
    var popupAt by rememberSaveable { mutableStateOf<Int?>(null) }
    var answers by rememberSaveable { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var awarded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(stage, progress, popupAt) {
        if (stage == "watch" && popupAt == null && progress < 100) {
            delay(450)
            progress += 2
            if (progress == 30 || progress == 60) popupAt = progress
        } else if (stage == "watch" && progress >= 100) {
            stage = "test"
        }
    }

    ArenaList {
        item {
            ArenaHeader(
                "YouTube Challenge",
                "Timestamp questions unlock the lesson.",
                "Challenges"
            ) { onExit() }
        }
        item {
            when (stage) {
                "watch" -> PremiumPanel {
                    YouTubeEmbed()
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(100.dp)),
                        color = Coral,
                        trackColor = Blush
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Lesson progress $progress%",
                        color = ProLearnColors.MutedDark,
                        fontSize = 13.sp
                    )
                    if (popupAt != null) {
                        Spacer(Modifier.height(14.dp))
                        TimestampQuestion(popupAt!!) { popupAt = null }
                    }
                }

                "test" -> TestPanel(
                    youtubeQuestions,
                    answers,
                    { index, value -> answers = answers + (index to value) }) { stage = "score" }

                "score" -> ScorePanel("YouTube Score", youtubeQuestions, answers, 50) {
                    if (!awarded) {
                        ArenaXpStore.addXp(50)
                        awarded = true
                    }
                    stage = "summary"
                }

                else -> SummaryPanel(
                    "Video mastered",
                    "Timestamp checkpoints and final test complete. XP added to your Arena rank.",
                    onExit
                )
            }
        }
    }
}

@Composable
private fun GamesScreen(
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var game by rememberSaveable { mutableStateOf<String?>(null) }
    when (game) {
        "ludo" -> KnowledgeLudo { game = null }
        "word" -> WordBattlefield { game = null }
        else -> ArenaList {
            item {
                ArenaHeader(
                    "Games",
                    "Board-game energy, syllabus rewards.",
                    "Arena"
                ) { onBack() }
            }
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(620.dp),
                    userScrollEnabled = false,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        listOf(
                            GameTile(
                                "Knowledge Ludo",
                                "4 players",
                                "All subjects",
                                "70 XP",
                                Icons.Default.GridOn,
                                Moss
                            ) { game = "ludo" },
                            GameTile(
                                "Word Battlefield",
                                "2 players",
                                "Science terms",
                                "60 XP",
                                Icons.Default.AutoAwesome,
                                Violet
                            ) { game = "word" },
                            GameTile(
                                "Tower Defense",
                                "Solo",
                                "JEE/NEET",
                                "Soon",
                                Icons.Default.Shield,
                                Coral
                            ) { },
                            GameTile(
                                "Treasure Hunt",
                                "Groups",
                                "Classroom",
                                "Soon",
                                Icons.Default.EmojiEvents,
                                Gold
                            ) { }
                        )
                    ) { tile -> tile.Content(snackbarHostState) }
                }
            }
        }
    }
}

@Composable
private fun KnowledgeLudo(onExit: () -> Unit) {
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
private fun WordBattlefield(onExit: () -> Unit) {
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
private fun ArenaList(content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 118.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

@Composable
private fun ArenaHeader(
    title: String,
    subtitle: String,
    action: String?,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 34.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Ink,
                fontFamily = BricolageGrotesqueFamily
            )
            Text(
                subtitle,
                fontSize = 14.sp,
                color = ProLearnColors.MutedDark,
                fontFamily = BricolageGrotesqueFamily
            )
        }
        if (action != null && onAction != null) {
            MiniButton(action, onAction)
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Text(
            title,
            fontSize = 21.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Ink,
            fontFamily = BricolageGrotesqueFamily
        )
        Text(
            subtitle,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = ProLearnColors.MutedDark,
            fontFamily = BricolageGrotesqueFamily
        )
    }
}

@Composable
private fun ArenaBriefingSection() {
    PremiumPanel(accent = Moss) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Designed for serious revision",
                    fontSize = 22.sp,
                    lineHeight = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Ink
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Arena keeps the competitive layer useful: timed recall, topic-based scoring, and a rank path that rewards consistency instead of random taps.",
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = ProLearnColors.MutedDark
                )
            }
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White.copy(alpha = 0.72f))
                    .border(1.dp, Ink.copy(alpha = 0.07f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.arena_art_challenges),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ArenaQuickStat(
                "Win rate",
                "78%",
                "Last 7 battles",
                Icons.Default.EmojiEvents,
                Gold,
                Modifier.weight(1f)
            )
            ArenaQuickStat(
                "Hot streak",
                "4",
                "XP boosted",
                Icons.Default.Bolt,
                Coral,
                Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ArenaHowItWorks() {
    PremiumPanel(accent = Blue) {
        SectionTitle(
            "How Arena builds mastery",
            "Each session follows a simple loop that keeps effort visible."
        )
        Spacer(Modifier.height(14.dp))
        ArenaStepRow(
            "01",
            "Choose a pressure level",
            "Quick duel, mission, or game board based on your energy."
        )
        ArenaStepRow(
            "02",
            "Answer with stakes",
            "Timers, health bars, and board moves make recall feel consequential."
        )
        ArenaStepRow(
            "03",
            "Convert effort to rank",
            "XP, streaks, and replay-ready summaries show what improved."
        )
    }
}

@Composable
private fun ArenaStepRow(number: String, title: String, body: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Ink.copy(alpha = 0.06f))
                .border(1.dp, Ink.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(number, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Moss)
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
            Text(body, fontSize = 12.sp, lineHeight = 17.sp, color = ProLearnColors.MutedDark)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ArenaTrainingPlan() {
    PremiumPanel(accent = Gold) {
        SectionTitle(
            "Suggested weekly rhythm",
            "A balanced route for students who want progress without burnout."
        )
        Spacer(Modifier.height(14.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            PlanChip("Mon", "PDF challenge")
            PlanChip("Wed", "Battle Room")
            PlanChip("Fri", "Knowledge Ludo")
            PlanChip("Sun", "Leaderboard push")
        }
        Spacer(Modifier.height(14.dp))
        Text(
            "The mix keeps one deep study task, one high-pressure recall task, and one lighter game loop in rotation.",
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = ProLearnColors.MutedDark
        )
    }
}

@Composable
private fun PlanChip(day: String, label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.76f))
            .border(1.dp, Ink.copy(alpha = 0.07f), RoundedCornerShape(16.dp))
            .padding(horizontal = 11.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(day, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Gold)
        Spacer(Modifier.width(7.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink)
    }
}

@Composable
private fun ArenaHero(
    xp: Int,
    rank: String,
    progress: Float,
    nextRankXp: Int,
    onBattle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                22.dp,
                RoundedCornerShape(30.dp),
                ambientColor = Ink.copy(alpha = 0.10f),
                spotColor = Ink.copy(alpha = 0.10f)
            )
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.linearGradient(
                    listOf(Paper, Color(0xFFF4F0E7), Color(0xFFEDE9DF)),
                    start = Offset.Zero,
                    end = Offset(780f, 520f)
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.88f), RoundedCornerShape(30.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(Ink.copy(alpha = 0.06f))
                            .border(1.dp, Ink.copy(alpha = 0.08f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Arena · Skill league",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Ink,
                            fontFamily = BricolageGrotesqueFamily
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Learn. Compete. Win.",
                        fontSize = 34.sp,
                        lineHeight = 35.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic,
                        color = Ink,
                        fontFamily = BricolageGrotesqueFamily
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Pick a mode, answer under pressure, and turn every correct move into Arena XP.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = ProLearnColors.MutedDark,
                        fontFamily = BricolageGrotesqueFamily
                    )
                }
            }

            Column {
                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GlassMetric("XP", "$xp", Icons.Default.Bolt, Gold, Modifier.weight(1f))
                    GlassMetric(
                        "Rank",
                        rank,
                        Icons.Default.MilitaryTech,
                        Violet,
                        Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(24.dp))
                PremiumButton("Start live battle", Icons.Default.PlayArrow, onBattle)
            }
        }
    }
}

@Composable
private fun ArenaMapArt(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        drawCircle(
            Gold.copy(alpha = 0.20f),
            size.minDimension * 0.34f,
            Offset(size.width * 0.84f, size.height * 0.16f)
        )
        drawCircle(
            Coral.copy(alpha = 0.14f),
            size.minDimension * 0.26f,
            Offset(size.width * 0.16f, size.height * 0.84f)
        )
        drawCircle(
            Blue.copy(alpha = 0.10f),
            size.minDimension * 0.22f,
            Offset(size.width * 0.78f, size.height * 0.78f)
        )

        val route = Path().apply {
            moveTo(size.width * 0.08f, size.height * 0.66f)
            cubicTo(
                size.width * 0.28f,
                size.height * 0.36f,
                size.width * 0.56f,
                size.height * 0.82f,
                size.width * 0.92f,
                size.height * 0.42f
            )
            lineTo(size.width * 0.96f, size.height * 0.48f)
            cubicTo(
                size.width * 0.62f,
                size.height * 0.92f,
                size.width * 0.28f,
                size.height * 0.46f,
                size.width * 0.12f,
                size.height * 0.74f
            )
            close()
        }
        drawPath(route, Moss.copy(alpha = 0.08f))

        repeat(9) { index ->
            val x = size.width * (0.12f + (index % 3) * 0.31f)
            val y = size.height * (0.22f + (index / 3) * 0.24f)
            drawCircle(Color.White.copy(alpha = 0.72f), 16f, Offset(x, y))
            drawCircle(
                listOf(Moss, Coral, Blue, Gold)[index % 4].copy(alpha = 0.46f),
                7f,
                Offset(x, y)
            )
        }
    }
}

@Composable
private fun ArenaRankMedal(rank: String) {
    val pulse by rememberInfiniteTransition(label = "rankPulse").animateFloat(
        initialValue = 0.92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            tween(1300, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "rankPulseScale"
    )
    Box(
        modifier = Modifier
            .size(82.dp)
            .graphicsLayer {
                scaleX = pulse
                scaleY = pulse
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(Gold.copy(alpha = 0.18f))
        )
        Box(
            Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.82f))
                .border(1.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.MilitaryTech, null, tint = Gold, modifier = Modifier.size(24.dp))
                Text(
                    rank.take(3).uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Ink
                )
            }
        }
    }
}

@Composable
private fun GlassMetric(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.68f))
            .border(1.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(22.dp))
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(icon, accent, 44)
        Spacer(Modifier.width(9.dp))
        Column {
            Text(label, fontSize = 11.sp, color = ProLearnColors.MutedDark, maxLines = 1)
            Text(
                value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Ink,
                fontStyle = FontStyle.Italic,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ArenaQuickStat(
    label: String,
    value: String,
    helper: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(92.dp)
            .shadow(
                16.dp,
                RoundedCornerShape(24.dp),
                ambientColor = accent.copy(alpha = 0.10f),
                spotColor = accent.copy(alpha = 0.10f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.84f))
            .border(1.dp, GlassStroke, RoundedCornerShape(24.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(icon, accent, 38)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, color = ProLearnColors.MutedDark, maxLines = 1)
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Ink,
                maxLines = 1
            )
            Text(
                helper,
                fontSize = 11.sp,
                color = accent,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ArenaLiveStrip() {
    val offset by rememberInfiniteTransition(label = "liveStrip").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "liveStripOffset"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Ink)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Coral.copy(alpha = 0.18f + offset * 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Notifications, null, tint = Coral, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "Live now",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Mira just won Battle Room and earned 80 XP",
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text("Watch soon", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ArenaDestinationCard(
    title: String,
    kicker: String,
    body: String,
    artRes: Int,
    accent: Color,
    reward: String,
    players: String,
    primary: Boolean,
    button: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.985f else 1f,
        spring(dampingRatio = 0.72f),
        label = "destinationScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (primary) 232.dp else 202.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                if (primary) 20.dp else 12.dp,
                RoundedCornerShape(26.dp),
                ambientColor = Ink.copy(alpha = if (primary) 0.10f else 0.07f),
                spotColor = Ink.copy(alpha = if (primary) 0.10f else 0.07f)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    listOf(Paper, Color(0xFFF7F3EB), Color.White.copy(alpha = 0.92f)),
                    start = Offset.Zero,
                    end = Offset(760f, 360f)
                )
            )
            .border(1.dp, Ink.copy(alpha = 0.07f), RoundedCornerShape(26.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(
                accent.copy(alpha = 0.07f),
                size.minDimension * 0.38f,
                Offset(size.width * 0.90f, size.height * 0.24f)
            )
            drawLine(
                Ink.copy(alpha = 0.06f),
                Offset(0f, size.height * 0.72f),
                Offset(size.width, size.height * 0.72f),
                1.5f
            )
        }
        Image(
            painter = painterResource(id = artRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 10.dp)
                .width(if (primary) 138.dp else 122.dp)
                .height(if (primary) 104.dp else 92.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.fillMaxWidth(if (primary) 0.66f else 0.70f)) {
                    Text(
                        kicker,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accent,
                        maxLines = 1
                    )
                    Text(
                        title,
                        fontSize = if (primary) 28.sp else 24.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Ink
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        body,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = ProLearnColors.MutedDark,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                DestinationChip(reward, Icons.Default.Star, accent)
                Spacer(Modifier.width(8.dp))
                DestinationChip(players, Icons.Default.Groups, Ink)
                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Ink)
                        .padding(horizontal = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        button,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DestinationChip(text: String, icon: ImageVector, tint: Color) {
    Row(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(Color.White.copy(alpha = 0.74f))
            .border(1.dp, Color.White, RoundedCornerShape(100.dp))
            .padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(5.dp))
        Text(text, color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun FeatureEntryCard(
    title: String,
    body: String,
    icon: ImageVector,
    accent: Color,
    button: String,
    onClick: () -> Unit
) {
    PremiumPanel(accent = accent) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon, accent)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Ink,
                    fontFamily = BricolageGrotesqueFamily
                )
                Text(body, fontSize = 13.sp, lineHeight = 19.sp, color = ProLearnColors.MutedDark)
            }
        }
        Spacer(Modifier.height(16.dp))
        PremiumButton(button, Icons.Default.PlayArrow, onClick)
    }
}

@Composable
private fun ModeCard(
    title: String,
    body: String,
    icon: ImageVector,
    accent: Color,
    button: String,
    onClick: () -> Unit
) {
    PremiumPanel(accent = accent) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon, accent)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text(body, fontSize = 13.sp, color = ProLearnColors.MutedDark, lineHeight = 18.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        PremiumButton(button, Icons.Default.Bolt, onClick)
    }
}

@Composable
private fun ChallengeCard(
    title: String,
    subject: String,
    time: String,
    xp: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    PremiumPanel(accent = accent) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon, accent)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text("$subject · $time · $xp", fontSize = 13.sp, color = ProLearnColors.MutedDark)
            }
        }
        Spacer(Modifier.height(12.dp))
        PremiumButton("Start", Icons.Default.PlayArrow, onClick)
    }
}

@Composable
private fun SmallGameCard(
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
private fun PremiumPanel(accent: Color = Moss, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                18.dp,
                RoundedCornerShape(28.dp),
                ambientColor = accent.copy(alpha = 0.10f),
                spotColor = accent.copy(alpha = 0.10f)
            )
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.82f))
            .border(1.dp, GlassStroke, RoundedCornerShape(28.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun PremiumButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Ink)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            fontFamily = BricolageGrotesqueFamily
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun MiniButton(text: String, onClick: () -> Unit) {
    Text(
        text,
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Color.White.copy(alpha = 0.82f))
            .border(1.dp, SoftBorder, RoundedCornerShape(100.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 9.dp),
        color = Ink,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(62.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.82f))
            .border(1.dp, GlassStroke, RoundedCornerShape(22.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(icon, accent, 34)
        Spacer(Modifier.width(9.dp))
        Column {
            Text(label, fontSize = 11.sp, color = ProLearnColors.MutedDark)
            Text(
                value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun IconBadge(icon: ImageVector, accent: Color, size: Int = 44) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = accent, modifier = Modifier.size((size * 0.48f).dp))
    }
}

@Composable
private fun MatchmakingCard(text: String) {
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
        Box(Modifier
            .fillMaxWidth()
            .height(260.dp), contentAlignment = Alignment.Center) {
            Box(Modifier
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
private fun LobbyCard(kind: BattleKind, countdown: Int) {
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
private fun PlayerRow(player: ArenaPlayer) {
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
private fun BattleQuizCard(
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
private fun AnswerOption(option: String, selected: String?, answer: String, onClick: () -> Unit) {
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
private fun HealthDuel(you: Int, rival: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        HealthBar("You", you, Moss, Modifier.weight(1f))
        HealthBar("Naina", rival, Coral, Modifier.weight(1f))
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
            Box(Modifier
                .fillMaxWidth(value / 100f)
                .fillMaxHeight()
                .background(color))
        }
    }
}

@Composable
private fun BattleResultCard(
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

@Composable
private fun ComingSoonCard(
    title: String,
    body: String,
    icon: ImageVector,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    ComingSoonStatic(title, body, icon) {
        scope.launch { snackbarHostState.showSnackbar("$title notifications enabled") }
    }
}

@Composable
private fun ComingSoonStatic(
    title: String,
    body: String,
    icon: ImageVector,
    onNotify: (() -> Unit)? = null
) {
    PremiumPanel(accent = Gold) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon, Gold)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
                Text(body, fontSize = 13.sp, lineHeight = 18.sp, color = ProLearnColors.MutedDark)
            }
        }
        Spacer(Modifier.height(12.dp))
        MiniButton(if (onNotify == null) "Coming Soon" else "Notify Me", onNotify ?: {})
    }
}

@Composable
private fun PdfText() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HighlightText("Photosynthesis is the process by which green plants make food using sunlight, carbon dioxide, and water.")
        BodyText("The leaves contain chlorophyll, a green pigment that captures light energy. Tiny openings called stomata allow carbon dioxide to enter the leaf.")
        HighlightText("During the process, plants produce glucose for energy and release oxygen as a by-product.")
        BodyText("This process supports almost all life because it forms the base of food chains and keeps oxygen available in the atmosphere.")
    }
}

@Composable
private fun HighlightText(text: String) {
    Text(
        text,
        Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Gold.copy(alpha = 0.24f))
            .padding(10.dp),
        color = Ink,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun BodyText(text: String) {
    Text(text, color = ProLearnColors.MutedDark, lineHeight = 20.sp, fontSize = 14.sp)
}

@Composable
private fun TestPanel(
    questions: List<ArenaQuestion>,
    answers: Map<Int, String>,
    onAnswer: (Int, String) -> Unit,
    onSubmit: () -> Unit
) {
    PremiumPanel(accent = Blue) {
        questions.forEachIndexed { index, question ->
            Text("${index + 1}. ${question.prompt}", color = Ink, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            question.options.forEach { option ->
                AnswerOption(option, answers[index], question.answer) { onAnswer(index, option) }
                Spacer(Modifier.height(6.dp))
            }
            Spacer(Modifier.height(8.dp))
        }
        PremiumButton("Submit test", Icons.Default.CheckCircle, onSubmit)
    }
}

@Composable
private fun ScorePanel(
    title: String,
    questions: List<ArenaQuestion>,
    answers: Map<Int, String>,
    xp: Int,
    onContinue: () -> Unit
) {
    val score = questions.indices.count { answers[it] == questions[it].answer }
    PremiumPanel(accent = Moss) {
        Text(title, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
        Text(
            "$score/${questions.size} correct · $xp XP",
            color = Moss,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        questions.forEachIndexed { index, question ->
            Text(question.prompt, color = Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("Correct: ${question.answer}", color = Moss, fontSize = 13.sp)
            Text(
                "Your answer: ${answers[index] ?: "Skipped"}",
                color = ProLearnColors.MutedDark,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(8.dp))
        PremiumButton("Continue", Icons.Default.AutoAwesome, onContinue)
    }
}

@Composable
private fun SummaryPanel(title: String, body: String, onDone: () -> Unit) {
    PremiumPanel(accent = Gold) {
        IconBadge(Icons.Default.EmojiEvents, Gold, 58)
        Spacer(Modifier.height(10.dp))
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
        Text(body, fontSize = 14.sp, lineHeight = 20.sp, color = ProLearnColors.MutedDark)
        Spacer(Modifier.height(14.dp))
        PremiumButton("Back to list", Icons.Default.CheckCircle, onDone)
    }
}

@Composable
private fun YouTubeEmbed() {
    val html = """
        <html><body style="margin:0;background:#111;">
        <iframe width="100%" height="100%" src="https://www.youtube.com/embed/kKKM8Y-u7ds" title="Educational video" frameborder="0" allowfullscreen></iframe>
        </body></html>
    """.trimIndent()
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16 / 9f)
            .clip(RoundedCornerShape(22.dp))
            .background(Ink)
    )
}

@Composable
private fun TimestampQuestion(at: Int, onAnswered: () -> Unit) {
    Column(
        Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Sky)
            .border(1.dp, Color.White, RoundedCornerShape(22.dp))
            .padding(14.dp)
    ) {
        Text("Checkpoint at $at%", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
        Text("What does force change?", color = ProLearnColors.MutedDark)
        Spacer(Modifier.height(10.dp))
        PremiumButton("Motion", Icons.Default.CheckCircle, onAnswered)
    }
}

@Composable
private fun MiniQuestion(question: ArenaQuestion, onAnswered: (Boolean) -> Unit) {
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
private fun LudoBoard(position: Int, aiPosition: Int) {
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
private fun BoxScope.Token(position: Int, color: Color, label: String) {
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
private fun WordBoard(word: String) {
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
private fun Tile(letter: String) {
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
private fun WinnerPanel(won: Boolean, xp: Int, onDone: () -> Unit) {
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

@Composable
private fun Confetti() {
    val phase by rememberInfiniteTransition(label = "confetti").animateFloat(
        0f,
        1f,
        infiniteRepeatable(tween(1300), RepeatMode.Restart),
        label = "phase"
    )
    Canvas(Modifier
        .fillMaxWidth()
        .height(42.dp)) {
        repeat(18) { i ->
            val x = size.width * ((i * 37 % 100) / 100f)
            val y = size.height * (((phase + i * 0.13f) % 1f))
            drawCircle(listOf(Gold, Coral, Blue, Moss)[i % 4], 4f, Offset(x, y))
        }
    }
}

@Composable
private fun BoxScope.ArenaAmbient() {
    val drift by rememberInfiniteTransition(label = "arenaAmbient").animateFloat(
        -9f,
        9f,
        infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "drift"
    )
    Box(
        Modifier
            .size(240.dp)
            .offset(x = (-82).dp, y = (78 + drift).dp)
            .clip(CircleShape)
            .background(Mint.copy(alpha = 0.58f))
    )
    Box(
        Modifier
            .size(220.dp)
            .align(Alignment.TopEnd)
            .offset(x = 74.dp, y = (18 - drift).dp)
            .clip(CircleShape)
            .background(Sky.copy(alpha = 0.62f))
    )
    Box(
        Modifier
            .size(190.dp)
            .align(Alignment.BottomEnd)
            .offset(x = 54.dp, y = (-120).dp)
            .clip(CircleShape)
            .background(Blush.copy(alpha = 0.58f))
    )
}
