package com.prolearn.spar.ui.screens.arena

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.prolearn.spar.R
import com.prolearn.spar.ui.theme.BricolageGrotesqueFamily
import com.prolearn.spar.ui.theme.ProLearnColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChallengesScreen(
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var challenge by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    when (challenge) {
        "pdf" -> PdfChallenge { challenge = null }
        "youtube" -> YouTubeChallenge { challenge = null }
        else -> ArenaList {
            item { ArenaHeader("Challenges", "Study, test, recall, earn.", "Arena") { onBack() } }
            item {
                ArenaFeatureCard(
                    title = "PDF Challenge",
                    eyebrow = "Read Sprint",
                    body = "Read a sharp study passage, beat the timer, then prove recall with a short test.",
                    meta = "Biology · 4 min · 50 XP",
                    icon = Icons.Default.PictureAsPdf,
                    accent = Blue,
                    imageRes = R.drawable.arena_preview_pdf_challenge,
                    button = "Start"
                ) { challenge = "pdf" }
            }
            item {
                ArenaFeatureCard(
                    title = "YouTube Challenge",
                    eyebrow = "Video Checkpoints",
                    body = "Watch a lesson with timed questions that keep attention active from start to finish.",
                    meta = "Physics · 5 min · 50 XP",
                    icon = Icons.Default.PlayCircle,
                    accent = Coral,
                    imageRes = R.drawable.arena_preview_youtube_challenge,
                    button = "Start"
                ) { challenge = "youtube" }
            }
            item {
                ArenaFeatureCard(
                    title = "Image Challenge",
                    eyebrow = "Visual Recall",
                    body = "Label diagrams, circuits, microscope slides, and figures with exam-style prompts.",
                    meta = "NEET · Boards · Diagram mastery",
                    icon = Icons.Default.Science,
                    accent = Moss,
                    imageRes = R.drawable.arena_preview_image_challenge,
                    button = "Coming soon",
                    comingSoon = true
                ) {}
            }
            item {
                ArenaFeatureCard(
                    title = "Live Debate Challenge",
                    eyebrow = "Argument Arena",
                    body = "Get a topic, build your argument, and let students vote on the stronger case.",
                    meta = "CUET · Critical thinking · Peer votes",
                    icon = Icons.Default.Groups,
                    accent = Violet,
                    imageRes = R.drawable.arena_preview_watch_live,
                    button = "Notify me",
                    comingSoon = true
                ) {
                    scope.launch { snackbarHostState.showSnackbar("Live Debate Challenge notifications enabled") }
                }
            }
        }
    }
}

@Composable
fun PdfChallenge(onExit: () -> Unit) {
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
fun YouTubeChallenge(onExit: () -> Unit) {
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
fun ChallengeCard(
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
fun PdfText() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HighlightText("Photosynthesis is the process by which green plants make food using sunlight, carbon dioxide, and water.")
        BodyText("The leaves contain chlorophyll, a green pigment that captures light energy. Tiny openings called stomata allow carbon dioxide to enter the leaf.")
        HighlightText("During the process, plants produce glucose for energy and release oxygen as a by-product.")
        BodyText("This process supports almost all life because it forms the base of food chains and keeps oxygen available in the atmosphere.")
    }
}

@Composable
fun HighlightText(text: String) {
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
fun BodyText(text: String) {
    Text(text, color = ProLearnColors.MutedDark, lineHeight = 20.sp, fontSize = 14.sp)
}

@Composable
fun TestPanel(
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
fun ScorePanel(
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
fun SummaryPanel(title: String, body: String, onDone: () -> Unit) {
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
fun YouTubeEmbed() {
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
fun TimestampQuestion(at: Int, onAnswered: () -> Unit) {
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
