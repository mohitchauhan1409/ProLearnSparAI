package com.prolearn.spar.ui.screens.arena

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
fun GamesScreen(
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var game by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
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
                ArenaFeatureCard(
                    title = "Knowledge Ludo",
                    eyebrow = "Board Battle",
                    body = "Answer correctly to roll, race tokens around the board, and outpace the table.",
                    meta = "4 players · All subjects · 70 XP",
                    icon = Icons.Default.GridOn,
                    accent = Moss,
                    imageRes = R.drawable.arena_preview_games_collection,
                    button = "Play"
                ) {
                    game = "ludo"
                }
            }
            item {
                ArenaFeatureCard(
                    title = "Word Battlefield",
                    eyebrow = "Term Control",
                    body = "Build syllabus words from tiles, claim the board, and beat the rival score.",
                    meta = "2 players · Science terms · 60 XP",
                    icon = Icons.Default.AutoAwesome,
                    accent = Violet,
                    imageRes = R.drawable.arena_preview_games_collection,
                    button = "Play"
                ) {
                    game = "word"
                }
            }
            item {
                ArenaFeatureCard(
                    title = "Tower Defense",
                    eyebrow = "Solo Strategy",
                    body = "Defend lanes with correct answers and upgrade knowledge towers across JEE and NEET topics.",
                    meta = "Solo · JEE/NEET · Strategy",
                    icon = Icons.Default.Shield,
                    accent = Coral,
                    imageRes = R.drawable.arena_preview_games_collection,
                    button = "Coming soon",
                    comingSoon = true
                ) {
                    scope.launch { snackbarHostState.showSnackbar("Tower Defense is coming soon") }
                }
            }
            item {
                ArenaFeatureCard(
                    title = "Treasure Hunt",
                    eyebrow = "Group Quest",
                    body = "Solve clues with classmates, unlock checkpoints, and chase a classroom reward trail.",
                    meta = "Groups · Classroom · Puzzle trail",
                    icon = Icons.Default.EmojiEvents,
                    accent = Gold,
                    imageRes = R.drawable.arena_preview_games_collection,
                    button = "Coming soon",
                    comingSoon = true
                ) {
                    scope.launch { snackbarHostState.showSnackbar("Treasure Hunt is coming soon") }
                }
            }
        }
    }
}

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
