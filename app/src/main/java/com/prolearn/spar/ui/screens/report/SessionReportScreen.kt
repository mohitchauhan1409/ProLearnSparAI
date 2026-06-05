package com.prolearn.spar.ui.screens.report

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.prolearn.spar.domain.model.ConceptScore
import com.prolearn.spar.domain.model.Session
import com.prolearn.spar.domain.model.SessionFlashcard
import com.prolearn.spar.ui.components.ui.ProLearnButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

private val Page = Color(0xFFF7FAF7)
private val Ink = Color(0xFF13201A)
private val SoftInk = Color(0xFF5C6961)
private val Moss = Color(0xFF4E7D68)
private val Mint = Color(0xFFEAF6D8)
private val Sky = Color(0xFFEAF3FF)
private val Peach = Color(0xFFFFF0E4)
private val Rose = Color(0xFFFFEEF2)
private val Line = Color(0x1F13201A)
private val Card = Color(0xCCFFFFFF)

@Composable
fun SessionReportScreen(
    onNavigateToHome: () -> Unit,
    viewModel: SessionReportViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val session by viewModel.session.collectAsState()

    val readySession = session
    val reportDetails = readySession?.reportDetails
    if (readySession == null || reportDetails == null) {
        if (readySession?.reportGenerationFailed == true) {
            ReportGenerationFailed(onNavigateToHome = onNavigateToHome, session = readySession)
            return
        }
        PreparingReport(onNavigateToHome = onNavigateToHome, session = readySession)
        return
    }

    val report = remember(readySession) { readySession.toReport() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Page, Color(0xFFFFFFFF), Color(0xFFF1F7F3)),
                        start = Offset.Zero,
                        end = Offset(800f, 1400f)
                    )
                )
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            AnimatedSection(index = 0) {
                ReportHero(session = readySession, report = report)
            }

            Spacer(Modifier.height(16.dp))

            AnimatedSection(index = 1) {
                InsightCard(report = report)
            }

            if (report.flashcards.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                AnimatedSection(index = 2) {
                    FlashcardDeckSection(cards = report.flashcards)
                }
            }

            Spacer(Modifier.height(20.dp))

            AnimatedSection(index = 3) {
                MetricStrip(session = readySession, report = report)
            }

            if (readySession.conceptScores.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                AnimatedSection(index = 4) {
                    ConceptMasteryCard(concepts = readySession.conceptScores)
                }
            }

            if (report.nextSteps.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                AnimatedSection(index = 5) {
                    NextMovesSection(report = report)
                }
            }

            Spacer(Modifier.height(104.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Page.copy(alpha = 0.96f), Page)
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            ProLearnButton(
                text = "Back to home",
                onClick = onNavigateToHome,
                backgroundColor = Ink
            )
        }
    }
}

@Composable
private fun ReportGenerationFailed(onNavigateToHome: () -> Unit, session: Session) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Page)
            .statusBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AmbientWash()
        GlassPanel {
            Label("Report unavailable", Icons.Default.AutoAwesome)
            Spacer(Modifier.height(12.dp))
            Text("AI report could not be generated", color = Ink, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Your ${session.subject} session was saved, but the personalized AI breakdown did not complete. Start another session or try again later.",
                color = SoftInk,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
            Spacer(Modifier.height(18.dp))
            ProLearnButton(text = "Back to home", onClick = onNavigateToHome, backgroundColor = Ink)
        }
    }
}

@Composable
private fun PreparingReport(onNavigateToHome: () -> Unit, session: Session?) {
    val transition = rememberInfiniteTransition(label = "reportLoading")
    val pulse by transition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(920), RepeatMode.Reverse),
        label = "reportPulse"
    )
    val shimmer by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1300), RepeatMode.Restart),
        label = "reportShimmer"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Page, Color.White, Color(0xFFF1F7F3)),
                    start = Offset.Zero,
                    end = Offset(700f, 1300f)
                )
            )
            .statusBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AmbientWash()
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                }
            ) {
                PulsingOrb()
            }
            Spacer(Modifier.height(18.dp))
            Text("Generating your session report", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                session?.let {
                    "AI is reading your ${it.subject} conversation and building a personalized breakdown."
                } ?: "AI is preparing the final breakdown from your latest session.",
                color = SoftInk,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(22.dp))
            GeneratingSteps(progress = shimmer)
            Spacer(Modifier.height(26.dp))
            ProLearnButton(text = "Back to home", onClick = onNavigateToHome, backgroundColor = Ink)
        }
    }
}

@Composable
private fun GeneratingSteps(progress: Float) {
    GlassPanel {
        LoadingLine("Reading transcript", progress > 0.18f)
        Spacer(Modifier.height(10.dp))
        LoadingLine("Finding strengths and gaps", progress > 0.42f)
        Spacer(Modifier.height(10.dp))
        LoadingLine("Creating flashcards", progress > 0.66f)
    }
}

@Composable
private fun LoadingLine(text: String, active: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (active) Moss else Line)
        )
        Spacer(Modifier.width(9.dp))
        Text(text, color = if (active) Ink else SoftInk, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AmbientWash() {
    Box(
        modifier = Modifier
            .size(240.dp)
            .graphicsLayer { alpha = 0.55f }
            .background(Mint, CircleShape)
    )
    Box(
        modifier = Modifier
            .padding(start = 190.dp, top = 80.dp)
            .size(210.dp)
            .graphicsLayer { alpha = 0.55f }
            .background(Sky, CircleShape)
    )
}

@Composable
private fun AnimatedSection(
    index: Int,
    content: @Composable () -> Unit
) {
    val alpha = remember { Animatable(0f) }
    val offset = remember { Animatable(18f) }
    LaunchedEffect(Unit) {
        delay(index * 70L)
        alpha.animateTo(1f, tween(420))
        offset.animateTo(0f, tween(420))
    }
    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha.value
            translationY = offset.value
        }
    ) {
        content()
    }
}

@Composable
private fun ReportHero(session: Session, report: ReportUi) {
    GlassPanel(
        brush = Brush.linearGradient(listOf(Color.White, Color(0xFFF3F9F5)))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ScoreDial(score = session.score)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = Moss, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Session complete", color = Moss, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    report.title,
                    color = Ink,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 29.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "${session.subject} / ${session.chapter.displayChapter()} / ${session.difficulty}",
                    color = SoftInk,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MetricStrip(session: Session, report: ReportUi) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MiniMetric(Icons.Default.Timer, report.timeText, "Study time", Mint, Modifier.weight(1f))
        MiniMetric(Icons.Default.Psychology, "${session.questionCount}", "Turns", Sky, Modifier.weight(1f))
        MiniMetric(Icons.Default.Lightbulb, "${session.hintsUsed}", "Hints", Peach, Modifier.weight(1f))
    }
}

@Composable
private fun MiniMetric(icon: ImageVector, value: String, label: String, tint: Color, modifier: Modifier = Modifier) {
    GlassPanel(modifier = modifier, padding = 12.dp) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(tint),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Ink, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(value, color = Ink, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Text(label, color = SoftInk, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
private fun InsightCard(report: ReportUi) {
    GlassPanel {
        Label("AI-generated report", Icons.Default.AutoAwesome)
        Spacer(Modifier.height(10.dp))
        Text(
            report.summary,
            color = Ink,
            fontSize = 18.sp,
            lineHeight = 25.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))
        Text(
            report.insight,
            color = SoftInk,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun HighlightsGrid(report: ReportUi) {
    Column {
        SectionTitle("Highlights")
        Spacer(Modifier.height(10.dp))
        report.highlights.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEachIndexed { index, text ->
                    HighlightCard(text, if (index == 0) Mint else Sky, Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
        }
        SectionTitle("Next moves")
        Spacer(Modifier.height(10.dp))
        report.nextSteps.forEachIndexed { index, step ->
            NextStepRow(index + 1, step)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HighlightCard(text: String, tint: Color, modifier: Modifier) {
    GlassPanel(modifier = modifier, padding = 13.dp) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(tint),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.TrendingUp, null, tint = Ink, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(text, color = Ink, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ConceptMasteryCard(concepts: List<ConceptScore>) {
    GlassPanel {
        SectionTitle("Concept mastery")
        Spacer(Modifier.height(12.dp))
        concepts.sortedByDescending { it.score }.forEach { concept ->
            ConceptRow(concept)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ConceptRow(concept: ConceptScore) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(concept.name, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("${concept.score}%", color = Moss, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(7.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE8EEE9))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((concept.score / 100f).coerceIn(0f, 1f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.horizontalGradient(listOf(Moss, Color(0xFF8DBA76))))
            )
        }
    }
}

@Composable
private fun NextStepRow(index: Int, text: String) {
    GlassPanel(padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Peach),
                contentAlignment = Alignment.Center
            ) {
                Text("$index", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Text(text, color = Ink, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun FlashcardDeckSection(cards: List<SessionFlashcard>) {
    Column {
        SectionTitle("Flashcards from this session")
        Spacer(Modifier.height(10.dp))
        Text(
            "Swipe the top card to cycle through your AI-made revision deck.",
            color = SoftInk,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
        Spacer(Modifier.height(14.dp))
        FlashcardDeck(cards = cards)
    }
}

@Composable
private fun FlashcardDeck(cards: List<SessionFlashcard>) {
    val deck = remember(cards) { mutableStateListOf<SessionFlashcard>().apply { addAll(cards) } }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(236.dp),
        contentAlignment = Alignment.Center
    ) {
        deck.take(4).asReversed().forEachIndexed { reverseIndex, card ->
            val stackIndex = deck.take(4).lastIndex - reverseIndex
            val isTop = stackIndex == 0
            FlashcardStackCard(
                card = card,
                stackIndex = stackIndex,
                offsetX = if (isTop) offsetX.value else 0f,
                modifier = Modifier
                    .zIndex((10 - stackIndex).toFloat())
                    .pointerInput(isTop, deck.size) {
                        if (!isTop) return@pointerInput
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                scope.launch {
                                    offsetX.snapTo(offsetX.value + dragAmount.x)
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    if (abs(offsetX.value) > 110f && deck.size > 1) {
                                        val direction = if (offsetX.value > 0) 1 else -1
                                        offsetX.animateTo(direction * 520f, spring(dampingRatio = 0.82f))
                                        val top = deck.removeAt(0)
                                        deck.add(top)
                                        offsetX.snapTo(0f)
                                    } else {
                                        offsetX.animateTo(0f, spring(dampingRatio = 0.78f))
                                    }
                                }
                            }
                        )
                    }
            )
        }
    }
}

@Composable
private fun FlashcardStackCard(
    card: SessionFlashcard,
    stackIndex: Int,
    offsetX: Float,
    modifier: Modifier = Modifier
) {
    val colors = listOf(Mint, Sky, Rose, Peach)
    val baseTint = colors[stackIndex % colors.size]
    Column(
        modifier = modifier
            .width(292.dp)
            .height(204.dp)
            .graphicsLayer {
                translationX = offsetX + stackIndex * 11f
                translationY = stackIndex * 10f
                scaleX = 1f - stackIndex * 0.045f
                scaleY = 1f - stackIndex * 0.045f
                rotationZ = offsetX / 34f
                alpha = 1f - stackIndex * 0.08f
            }
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.linearGradient(listOf(Color.White, baseTint)))
            .border(1.dp, Color.White.copy(alpha = 0.82f), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.School, null, tint = Moss, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(card.tag.uppercase(), color = Moss, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(14.dp))
        Text(card.front, color = Ink, fontSize = 17.sp, lineHeight = 23.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        Text(card.back, color = SoftInk, fontSize = 13.sp, lineHeight = 18.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ScoreDial(score: Int) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(104.dp)) {
        Canvas(Modifier.size(104.dp)) {
            val stroke = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = Color(0xFFE3EAE4),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke,
                size = Size(size.width, size.height)
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(Moss, Color(0xFFB9DA82), Moss)),
                startAngle = -90f,
                sweepAngle = (score.coerceIn(0, 100) / 100f) * 360f,
                useCenter = false,
                style = stroke,
                size = Size(size.width, size.height)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$score", color = Ink, fontSize = 27.sp, fontWeight = FontWeight.Bold)
            Text("score", color = SoftInk, fontSize = 11.sp)
        }
    }
}

@Composable
private fun PulsingOrb() {
    val scale = remember { Animatable(0.9f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, tween(600))
    }
    Box(
        modifier = Modifier
            .size(86.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Mint, Sky))),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.AutoAwesome, null, tint = Ink, modifier = Modifier.size(34.dp))
    }
}

@Composable
private fun GlassPanel(
    modifier: Modifier = Modifier,
    padding: androidx.compose.ui.unit.Dp = 16.dp,
    brush: Brush = Brush.linearGradient(listOf(Card, Color(0xF2FFFFFF))),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(brush)
            .border(1.dp, Color.White.copy(alpha = 0.78f), RoundedCornerShape(8.dp))
            .padding(padding),
        content = content
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun Label(text: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Moss, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(6.dp))
        Text(text.uppercase(), color = Moss, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

private data class ReportUi(
    val title: String,
    val timeText: String,
    val summary: String,
    val insight: String,
    val highlights: List<String>,
    val nextSteps: List<String>,
    val flashcards: List<SessionFlashcard>
)

private fun Session.toReport(): ReportUi {
    val details = requireNotNull(reportDetails)
    val title = when {
        details.title.isNotBlank() -> details.title
        score >= 85 -> "Excellent momentum"
        score >= 70 -> "Strong session"
        score >= 45 -> "Useful practice"
        questionCount > 0 -> "Good start"
        else -> "Session saved"
    }

    return ReportUi(
        title = title,
        timeText = durationSeconds.toClock(),
        summary = details.summary,
        insight = aiInsight,
        highlights = details.highlights.filter { it.isNotBlank() },
        nextSteps = details.nextSteps.filter { it.isNotBlank() },
        flashcards = details.flashcards.filter { it.front.isNotBlank() && it.back.isNotBlank() }
    )
}

private fun Int.toClock(): String {
    val minutes = this / 60
    val seconds = this % 60
    return if (minutes == 0) "${seconds}s" else "$minutes:${seconds.toString().padStart(2, '0')}"
}

private fun String.displayChapter(): String =
    if (this == "Generic") "Open chapter" else this
