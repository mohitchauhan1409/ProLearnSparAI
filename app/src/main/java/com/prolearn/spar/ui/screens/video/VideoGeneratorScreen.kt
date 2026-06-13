package com.prolearn.spar.ui.screens.video

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prolearn.spar.ui.components.ui.ProLearnButton
import com.prolearn.spar.ui.components.ui.ProLearnTextField
import com.prolearn.spar.ui.theme.BricolageGrotesqueFamily
import com.prolearn.spar.ui.theme.ChalkFontFamily
import com.prolearn.spar.ui.theme.ProLearnColors

private val PageBg = Color(0xFFF8FAF7)
private val Ink = Color(0xFF151616)

private val SUGGESTIONS = listOf(
    "Photosynthesis explained simply",
    "How neural networks learn",
    "Newton's three laws of motion",
    "The French Revolution in 5 minutes",
    "Supply and demand basics",
    "What is the Pythagorean theorem"
)

@Composable
fun VideoGeneratorScreen(
    onBack: () -> Unit,
    viewModel: VideoGeneratorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(PageBg, Color(0xFFF2F7F4), Color(0xFFFFFBF5))
                )
            )
    ) {
        AnimatedContent(
            targetState = state.phase,
            transitionSpec = { fadeIn(tween(280)) togetherWith fadeOut(tween(180)) },
            label = "phase"
        ) { phase ->
            when (phase) {
                GenPhase.IDLE -> TopicStage(state, viewModel, onBack)
                GenPhase.SCRIPTING, GenPhase.VOICING -> GeneratingStage(state, onBack)
                GenPhase.ERROR -> ErrorStage(state, viewModel, onBack)
                GenPhase.READY -> PlayerStage(state, viewModel, onBack)
            }
        }
    }
}

// ─── Topic input ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TopicStage(
    state: VideoUiState,
    viewModel: VideoGeneratorViewModel,
    onBack: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        TopBar(title = "AI Video Lessons", onBack = onBack)
        Spacer(Modifier.height(8.dp))

        Row(
            Modifier
                .clip(RoundedCornerShape(100))
                .background(Color.White.copy(alpha = 0.8f))
                .border(1.dp, ProLearnColors.Border, RoundedCornerShape(100))
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFF4E7D68), modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                "Powered by Claude + natural voice",
                fontSize = 12.sp,
                color = ProLearnColors.MutedDark,
                fontFamily = BricolageGrotesqueFamily
            )
        }

        Spacer(Modifier.height(20.dp))
        Text(
            "Turn any topic into a\nvideo lesson",
            fontSize = 30.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Ink,
            fontFamily = BricolageGrotesqueFamily
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Type what you want to learn. Your AI teacher writes the script, narrates it, and explains it on screen.",
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = ProLearnColors.MutedDark,
            fontFamily = BricolageGrotesqueFamily
        )

        Spacer(Modifier.height(24.dp))
        ProLearnTextField(
            value = state.topic,
            onValueChange = viewModel::onTopicChange,
            placeholder = "e.g. How do black holes form?"
        )

        Spacer(Modifier.height(16.dp))
        Text(
            "Try one of these",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = ProLearnColors.Muted,
            fontFamily = BricolageGrotesqueFamily
        )
        Spacer(Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SUGGESTIONS.forEach { s ->
                SuggestionChip(s) { viewModel.onTopicChange(s) }
            }
        }

        Spacer(Modifier.height(28.dp))
        ProLearnButton(
            text = "Generate lesson",
            onClick = { viewModel.generate() },
            enabled = state.topic.trim().length >= 3
        )
    }
}

@Composable
private fun SuggestionChip(text: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        Modifier
            .clip(RoundedCornerShape(100))
            .background(Color.White.copy(alpha = 0.85f))
            .border(1.dp, ProLearnColors.Border, RoundedCornerShape(100))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(
            text,
            fontSize = 13.sp,
            color = ProLearnColors.Black,
            fontFamily = BricolageGrotesqueFamily
        )
    }
}

// ─── Generating ─────────────────────────────────────────────────────────────

@Composable
private fun GeneratingStage(state: VideoUiState, onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        TopBar(title = "Creating your lesson", onBack = onBack)
        Spacer(Modifier.weight(1f))
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val spin = rememberInfiniteTransition(label = "spin")
            val angle by spin.animateFloat(
                0f, 360f,
                infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
                label = "angle"
            )
            Box(contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(96.dp)
                        .rotate(angle)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(Color(0xFFEAF6D8), Color(0xFFEAF3FF), Color(0xFFFFEFF3), Color(0xFFEAF6D8))
                            )
                        )
                )
                Box(
                    Modifier.size(72.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, null, tint = Ink, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.height(28.dp))

            val scripting = state.phase == GenPhase.SCRIPTING
            Text(
                if (scripting) "Writing the lesson script…" else "Recording the voiceover…",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
                fontFamily = BricolageGrotesqueFamily
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (scripting)
                    "Your AI teacher is planning the scenes for \"${state.topic}\"."
                else
                    "Narrating scene ${state.voiceDone} of ${state.voiceTotal} in a natural voice.",
                fontSize = 13.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center,
                color = ProLearnColors.MutedDark,
                fontFamily = BricolageGrotesqueFamily
            )

            Spacer(Modifier.height(22.dp))
            val progress = when {
                scripting -> 0.18f
                state.voiceTotal > 0 -> 0.25f + 0.75f * (state.voiceDone.toFloat() / state.voiceTotal)
                else -> 0.25f
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(100))
                    .background(ProLearnColors.Border)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(6.dp)
                        .clip(RoundedCornerShape(100))
                        .background(Ink)
                )
            }
        }
        Spacer(Modifier.weight(1.4f))
    }
}

// ─── Error ──────────────────────────────────────────────────────────────────

@Composable
private fun ErrorStage(
    state: VideoUiState,
    viewModel: VideoGeneratorViewModel,
    onBack: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        TopBar(title = "AI Video Lessons", onBack = onBack)
        Spacer(Modifier.weight(1f))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Couldn't create the lesson", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink, fontFamily = BricolageGrotesqueFamily)
            Spacer(Modifier.height(8.dp))
            Text(
                state.error ?: "Please try again.",
                fontSize = 13.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center,
                color = ProLearnColors.MutedDark,
                fontFamily = BricolageGrotesqueFamily
            )
            Spacer(Modifier.height(24.dp))
            ProLearnButton(text = "Try again", onClick = { viewModel.generate() })
            Spacer(Modifier.height(12.dp))
            ProLearnButton(
                text = "Back",
                onClick = { viewModel.reset() },
                backgroundColor = Color.White,
                textColor = Ink,
                borderColor = ProLearnColors.Border
            )
        }
        Spacer(Modifier.weight(1.4f))
    }
}

// ─── Player ─────────────────────────────────────────────────────────────────

@Composable
private fun PlayerStage(
    state: VideoUiState,
    viewModel: VideoGeneratorViewModel,
    onBack: () -> Unit
) {
    // The doubt sheet can open over either the poster or the player.
    if (state.doubtOpen) {
        DoubtSheet(state = state, viewModel = viewModel)
    }

    // Don't auto-play — show a poster and let the student press Start.
    if (!state.started) {
        StartPoster(
            state = state,
            onStart = { viewModel.start() },
            onClose = { viewModel.reset(); onBack() }
        )
        return
    }

    val scene = state.scenes.getOrNull(state.currentScene)?.scene
    val accent = remember(state.currentScene) { accentFor(scene?.visual ?: "concept") }

    // Smooth per-scene progress bar driven by the known clip duration.
    val progress = remember { Animatable(0f) }
    var lastScene by remember { mutableIntStateOf(-1) }
    LaunchedEffect(state.currentScene, state.isPlaying, state.sceneDurationMs, state.finished) {
        if (state.currentScene != lastScene) {
            progress.snapTo(0f)
            lastScene = state.currentScene
        }
        if (state.finished) {
            progress.snapTo(1f); return@LaunchedEffect
        }
        if (!state.isPlaying || state.sceneDurationMs <= 0) return@LaunchedEffect
        val remaining = ((1f - progress.value) * state.sceneDurationMs).toLong().coerceAtLeast(0L)
        progress.animateTo(1f, tween(remaining.toInt(), easing = LinearEasing))
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        // Header
        Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleIconButton(Icons.Default.Close, "Close") {
                viewModel.reset(); onBack()
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    state.lesson?.title ?: "Lesson",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    fontFamily = BricolageGrotesqueFamily,
                    maxLines = 1
                )
                Text(
                    "Scene ${state.currentScene + 1} of ${state.sceneCount}",
                    fontSize = 11.sp,
                    color = ProLearnColors.Muted,
                    fontFamily = BricolageGrotesqueFamily
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Chalkboard stage, with the floating "ask a doubt" button
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (scene != null) {
                Chalkboard(
                    scene = scene,
                    sceneKey = state.currentScene,
                    revealedLines = state.revealedLines,
                    lineDurations = state.lineDurations,
                    accent = accent,
                    modifier = Modifier.fillMaxSize()
                )
            }
            DoubtButton(
                onClick = { viewModel.openDoubt() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 12.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Scene progress bar
        Box(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(100))
                .background(ProLearnColors.Border)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(progress.value.coerceIn(0f, 1f))
                    .height(4.dp)
                    .clip(RoundedCornerShape(100))
                    .background(Ink)
            )
        }

        Spacer(Modifier.height(14.dp))
        SceneDots(
            count = state.sceneCount,
            current = state.currentScene,
            onSeek = viewModel::seekToScene,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(16.dp))

        // Transport controls
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleIconButton(Icons.Default.SkipPrevious, "Previous", size = 48) { viewModel.previous() }
            Spacer(Modifier.width(20.dp))
            // Play / pause / replay
            val interaction = remember { MutableInteractionSource() }
            Box(
                Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Ink)
                    .clickable(interactionSource = interaction, indication = null) { viewModel.togglePlay() },
                contentAlignment = Alignment.Center
            ) {
                val icon = when {
                    state.finished -> Icons.Default.Refresh
                    state.isPlaying -> Icons.Default.Pause
                    else -> Icons.Default.PlayArrow
                }
                Icon(icon, "Play/Pause", tint = Color.White, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.width(20.dp))
            CircleIconButton(Icons.Default.SkipNext, "Next", size = 48) { viewModel.next() }
        }

        Spacer(Modifier.height(16.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .clickableNoRipple { viewModel.reset() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "New lesson",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = ProLearnColors.MutedDark,
                fontFamily = BricolageGrotesqueFamily
            )
        }
        Spacer(Modifier.height(12.dp).navigationBarsPadding())
    }
}

// ─── Start poster ─────────────────────────────────────────────────────────────

@Composable
private fun StartPoster(
    state: VideoUiState,
    onStart: () -> Unit,
    onClose: () -> Unit
) {
    val boardTop = Color(0xFF2C3D33)
    val boardBottom = Color(0xFF1E2A23)
    val chalk = Color(0xFFF1EEDF)

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        TopBar(title = "Your lesson is ready", onBack = onClose)
        Spacer(Modifier.weight(0.7f))

        // Chalkboard poster
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF7A5230), Color(0xFF4A3015))))
                .padding(10.dp)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.verticalGradient(listOf(boardTop, boardBottom)))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Today's lesson",
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    color = chalk.copy(alpha = 0.6f),
                    fontFamily = BricolageGrotesqueFamily
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    state.lesson?.title ?: state.topic,
                    fontSize = 34.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = chalk,
                    textAlign = TextAlign.Center,
                    fontFamily = ChalkFontFamily
                )
                state.lesson?.subtitle?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        it,
                        fontSize = 20.sp,
                        lineHeight = 25.sp,
                        color = chalk.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        fontFamily = ChalkFontFamily
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "${state.sceneCount} chalkboards • taught by ${state.lesson?.teacherName ?: "your teacher"}",
                    fontSize = 12.sp,
                    color = chalk.copy(alpha = 0.55f),
                    fontFamily = BricolageGrotesqueFamily
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        ProLearnButton(text = "Start lesson", onClick = onStart)
        Spacer(Modifier.height(10.dp))
        Text(
            "Tap the “?” anytime during the lesson to ask a doubt.",
            fontSize = 12.sp,
            color = ProLearnColors.Muted,
            textAlign = TextAlign.Center,
            fontFamily = BricolageGrotesqueFamily,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.weight(1f))
    }
}

// ─── Ask-a-doubt ──────────────────────────────────────────────────────────────

@Composable
private fun DoubtButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Ink)
                .border(2.dp, Color.White, CircleShape)
                .clickable(interactionSource = interaction, indication = null, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Forum,
                contentDescription = "Ask a doubt",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(5.dp))
        Box(
            Modifier
                .clip(RoundedCornerShape(100))
                .background(Ink.copy(alpha = 0.85f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text("Ask", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White, fontFamily = BricolageGrotesqueFamily)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DoubtSheet(state: VideoUiState, viewModel: VideoGeneratorViewModel) {
    val sheetState = rememberModalBottomSheetState()
    var input by remember { mutableStateOf("") }
    val scroll = rememberScrollState()
    val teacher = state.lesson?.teacherName ?: "your teacher"

    LaunchedEffect(state.doubtMessages.size, state.doubtLoading) {
        scroll.animateScrollTo(scroll.maxValue)
    }

    ModalBottomSheet(
        onDismissRequest = { viewModel.closeDoubt() },
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Ask $teacher",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    fontFamily = BricolageGrotesqueFamily
                )
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(100))
                        .background(ProLearnColors.Surface)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "lesson paused",
                        fontSize = 11.sp,
                        color = ProLearnColors.MutedDark,
                        fontFamily = BricolageGrotesqueFamily
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 380.dp)
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (state.doubtMessages.isEmpty() && !state.doubtLoading) {
                    Text(
                        "Stuck on something? Ask any doubt about this lesson and $teacher will explain it simply.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = ProLearnColors.MutedDark,
                        fontFamily = BricolageGrotesqueFamily
                    )
                }
                state.doubtMessages.forEach { DoubtBubble(it) }
                if (state.doubtLoading) {
                    DoubtBubble(DoubtMessage(fromUser = false, text = "$teacher is thinking…"))
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                ProLearnTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = "Type your doubt…",
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(10.dp))
                val sendInteraction = remember { MutableInteractionSource() }
                val canSend = input.trim().isNotEmpty() && !state.doubtLoading
                Box(
                    Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (canSend) Ink else ProLearnColors.Disabled)
                        .clickable(interactionSource = sendInteraction, indication = null, enabled = canSend) {
                            viewModel.askDoubt(input)
                            input = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("↑", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = BricolageGrotesqueFamily)
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun DoubtBubble(message: DoubtMessage) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            Modifier
                .widthIn(max = 290.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (message.fromUser) Ink else ProLearnColors.Surface)
                .then(
                    if (message.fromUser) Modifier
                    else Modifier.border(1.dp, ProLearnColors.Border, RoundedCornerShape(16.dp))
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                message.text,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = if (message.fromUser) Color.White else ProLearnColors.Black,
                fontFamily = BricolageGrotesqueFamily
            )
        }
    }
}

// ─── Shared bits ──────────────────────────────────────────────────────────────

@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleIconButton(Icons.Default.Close, "Close", onClick = onBack)
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Ink,
            fontFamily = BricolageGrotesqueFamily
        )
    }
}

@Composable
private fun CircleIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    size: Int = 40,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, ProLearnColors.Border, CircleShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription, tint = Ink, modifier = Modifier.size((size * 0.45f).dp))
    }
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = this.then(
    Modifier.clickable(
        interactionSource = MutableInteractionSource(),
        indication = null,
        onClick = onClick
    )
)
