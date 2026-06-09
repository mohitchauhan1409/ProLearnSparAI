package com.prolearn.spar.ui.screens.arena.challenges

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.prolearn.spar.ui.screens.arena.ArenaHeader
import com.prolearn.spar.ui.screens.arena.ArenaList
import com.prolearn.spar.ui.screens.arena.Coral
import kotlinx.coroutines.delay

@Composable
internal fun VideoChallengeScreen(
    setup: ChallengeSetup,
    onBack: () -> Unit,
    onComplete: (ChallengeResult) -> Unit
) {
    var stage by rememberSaveable { mutableStateOf("watch") }
    var timer by rememberSaveable { mutableIntStateOf(setup.durationSeconds) }
    var answerSeconds by rememberSaveable { mutableIntStateOf(0) }
    var answers by rememberSaveable { mutableStateOf<Map<Int, String>>(emptyMap()) }

    LaunchedEffect(stage, timer) {
        if (stage == "watch" && timer > 0) {
            delay(1000)
            timer -= 1
        } else if (stage == "watch") {
            stage = "test"
        }
    }

    LaunchedEffect(stage, answerSeconds) {
        if (stage == "test") {
            delay(1000)
            answerSeconds += 1
        }
    }

    val watchSeconds = setup.durationSeconds - timer
    if (stage == "watch") {
        VideoWatchScreen(
            setup = setup,
            timer = timer,
            onBack = onBack,
            onDone = { stage = "test" }
        )
    } else {
        ArenaList {
            item {
                ArenaHeader(
                    "YouTube Challenge",
                    "${setup.subject} · ${setup.topic} · ${setup.difficulty}",
                    "Challenges"
                ) { onBack() }
            }
            item {
                ChallengeMcqStage(
                    title = "Question Summary",
                    subtitle = "Clear the video questions. XP depends on accuracy, watch time, and answer speed.",
                    questions = youtubeQuestions,
                    answers = answers,
                    accent = Coral,
                    onAnswer = { index, value -> answers = answers + (index to value) },
                    onSubmit = {
                        val score = youtubeQuestions.indices.count { answers[it] == youtubeQuestions[it].answer }
                        val xp = calculateChallengeXp(
                            questions = youtubeQuestions,
                            answers = answers,
                            readingSeconds = watchSeconds,
                            answeringSeconds = answerSeconds,
                            readingLimitSeconds = setup.durationSeconds,
                            maxXp = setup.maxXp
                        )
                        onComplete(
                            ChallengeResult(
                                type = ChallengeType.YouTube,
                                setup = setup,
                                title = "YouTube Challenge Summary",
                                score = score,
                                total = youtubeQuestions.size,
                                readingSeconds = watchSeconds,
                                answeringSeconds = answerSeconds,
                                xpEarned = xp,
                                questions = youtubeQuestions,
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
private fun VideoWatchScreen(
    setup: ChallengeSetup,
    timer: Int,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    StickyChallengeScaffold(
        buttonText = "Done watching",
        buttonIcon = Icons.Default.CheckCircle,
        onButton = onDone
    ) {
        item {
            ArenaHeader(
                "YouTube Challenge",
                "${setup.subject} · ${setup.topic} · ${setup.difficulty}",
                "Challenges"
            ) { onBack() }
        }
        item {
            ChallengeProgressHeader(
                icon = Icons.Default.PlayCircle,
                accent = Coral,
                title = "Watch the demo lesson",
                subtitle = "${setup.goal} · up to ${setup.maxXp} XP",
                timer = timer,
                progress = 1f - (timer.toFloat() / setup.durationSeconds)
            )
        }
        item { YouTubeEmbed() }
        item { VideoLessonCard() }
        item { Spacer(Modifier.height(72.dp)) }
    }
}
