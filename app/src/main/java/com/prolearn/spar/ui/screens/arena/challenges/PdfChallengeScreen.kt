package com.prolearn.spar.ui.screens.arena.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.prolearn.spar.ui.screens.arena.ArenaHeader
import com.prolearn.spar.ui.screens.arena.ArenaList
import com.prolearn.spar.ui.screens.arena.Blue
import com.prolearn.spar.ui.screens.arena.PageBg
import com.prolearn.spar.ui.screens.arena.PremiumButton
import kotlinx.coroutines.delay

@Composable
internal fun PdfChallengeScreen(
    setup: ChallengeSetup,
    onBack: () -> Unit,
    onComplete: (ChallengeResult) -> Unit
) {
    var stage by rememberSaveable { mutableStateOf("read") }
    var timer by rememberSaveable { mutableIntStateOf(setup.durationSeconds) }
    var answerSeconds by rememberSaveable { mutableIntStateOf(0) }
    var answers by rememberSaveable { mutableStateOf<Map<Int, String>>(emptyMap()) }

    LaunchedEffect(stage, timer) {
        if (stage == "read" && timer > 0) {
            delay(1000)
            timer -= 1
        } else if (stage == "read") {
            stage = "test"
        }
    }

    LaunchedEffect(stage, answerSeconds) {
        if (stage == "test") {
            delay(1000)
            answerSeconds += 1
        }
    }

    val readingSeconds = setup.durationSeconds - timer
    if (stage == "read") {
        PdfReadingScreen(
            setup = setup,
            timer = timer,
            onBack = onBack,
            onDone = { stage = "test" }
        )
    } else {
        ArenaList {
            item {
                ArenaHeader(
                    "PDF Challenge",
                    "${setup.subject} · ${setup.topic} · ${setup.difficulty}",
                    "Challenges"
                ) { onBack() }
            }
            item {
                ChallengeMcqStage(
                    title = "Question Summary",
                    subtitle = "Answer from the PDF. XP depends on accuracy, reading speed, and answer speed.",
                    questions = pdfQuestions,
                    answers = answers,
                    accent = Blue,
                    onAnswer = { index, value -> answers = answers + (index to value) },
                    onSubmit = {
                        val score = pdfQuestions.indices.count { answers[it] == pdfQuestions[it].answer }
                        val xp = calculateChallengeXp(
                            questions = pdfQuestions,
                            answers = answers,
                            readingSeconds = readingSeconds,
                            answeringSeconds = answerSeconds,
                            readingLimitSeconds = setup.durationSeconds,
                            maxXp = setup.maxXp
                        )
                        onComplete(
                            ChallengeResult(
                                type = ChallengeType.Pdf,
                                setup = setup,
                                title = "PDF Challenge Summary",
                                score = score,
                                total = pdfQuestions.size,
                                readingSeconds = readingSeconds,
                                answeringSeconds = answerSeconds,
                                xpEarned = xp,
                                questions = pdfQuestions,
                                answers = answers
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun PdfReadingScreen(
    setup: ChallengeSetup,
    timer: Int,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(start = 18.dp, end = 18.dp, top = 14.dp)
    ) {
        Box(Modifier.fillMaxWidth()) {
            ArenaHeader(
                "PDF Challenge",
                "${setup.subject} · ${setup.topic} · ${setup.difficulty}",
                "Challenges"
            ) { onBack() }
        }
        Spacer(Modifier.height(16.dp))
        ChallengeProgressHeader(
            icon = Icons.Default.PictureAsPdf,
            accent = Blue,
            title = "Read the demo PDF",
            subtitle = "${setup.goal} · up to ${setup.maxXp} XP",
            timer = timer,
            progress = 1f - (timer.toFloat() / setup.durationSeconds)
        )
        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            PdfDocumentPreview(pdfDemoContent)
            Spacer(Modifier.height(12.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, PageBg.copy(alpha = 0.96f), PageBg)
                    )
                )
                .navigationBarsPadding()
                .padding(top = 12.dp, bottom = 16.dp)
        ) {
            PremiumButton("Done reading", Icons.Default.CheckCircle, onDone)
        }
    }
}
