package com.prolearn.spar.ui.screens.spar

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.prolearn.spar.domain.model.Message
import com.prolearn.spar.domain.model.SparConfig
import com.prolearn.spar.domain.model.SparState
import com.prolearn.spar.ui.components.spar.MessageBubble
import com.prolearn.spar.ui.components.spar.VoiceWaveform
import com.prolearn.spar.ui.theme.ProLearnColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LiveSparScreen(
    config: SparConfig,
    onNavigateToReport: () -> Unit,
    viewModel: LiveSparViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val audioLevel by viewModel.audioLevel.collectAsState()
    val timerFormatted by viewModel.timerFormatted.collectAsState()
    val partialTranscript by viewModel.partialTranscript.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val isRecognizerActive by viewModel.isRecognizerActive.collectAsState()

    val micPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    // Start session once
    LaunchedEffect(Unit) {
        viewModel.startSession(config)
    }

    // Navigate when complete
    LaunchedEffect(state) {
        if (state == SparState.SessionComplete) {
            delay(800)
            onNavigateToReport()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "sparTransitions")
    val pulseRingScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Restart),
        label = "pulseRingScale"
    )
    val pulseRingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Restart),
        label = "pulseRingAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProLearnColors.White)
            .safeDrawingPadding()
    ) {
        // ── Top Bar ──────────────────────────────────────────────────────────
        TopBar(
            config = config,
            timerFormatted = timerFormatted,
            questionCounter = viewModel.questionCounter,
            hintsRemaining = viewModel.hintsRemaining,
            onHintClick = { viewModel.requestHint() }
        )

        // ── Message List ─────────────────────────────────────────────────────
        val listState = rememberLazyListState()
        LaunchedEffect(messages.size, state) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(0)
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Partial transcript bubble (live, while user speaks)
            if (partialTranscript.isNotBlank()) {
                item(key = "partial") {
                    PartialTranscriptBubble(text = partialTranscript)
                }
            }

            // Typing indicator when AI is thinking/evaluating
            if (state == SparState.AiThinking || state == SparState.AiEvaluating) {
                item(key = "typing") {
                    TypingIndicator()
                }
            }

            items(
                items = messages.reversed(),
                key = { msg -> "${msg.timestamp}_${msg.role}" }
            ) { message ->
                MessageBubble(message)
            }
        }

        // ── Permission Denied Card ───────────────────────────────────────────
        if (!micPermission.status.isGranted) {
            MicPermissionCard(
                showRationale = micPermission.status.shouldShowRationale,
                onRequestPermission = { micPermission.launchPermissionRequest() }
            )
        } else {
            // ── Status Chip ──────────────────────────────────────────────────
            AnimatedVisibility(
                visible = statusMessage.isNotBlank() || isActiveState(state),
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut()
            ) {
                StateChipRow(state = state, statusMessage = statusMessage)
            }

            Spacer(Modifier.height(8.dp))

            // ── Error Retry Row ──────────────────────────────────────────────
            if (state is SparState.Error) {
                val err = state as SparState.Error
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(ProLearnColors.ErrorSurface)
                            .border(1.dp, ProLearnColors.Error.copy(alpha = 0.3f), RoundedCornerShape(100.dp))
                            .clickable { viewModel.retryConnection() }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "${err.message} · Tap to retry",
                            fontSize = 12.sp,
                            color = ProLearnColors.Error
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // ── Waveform ─────────────────────────────────────────────────────
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                contentAlignment = Alignment.Center
            ) {
                VoiceWaveform(
                    audioLevel = audioLevel,
                    isActive = state is SparState.AiSpeaking || state is SparState.StudentSpeaking
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Mic Button ───────────────────────────────────────────────────
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                val isSpeaking = state == SparState.StudentSpeaking || state == SparState.StudentListening
                val canTap = state == SparState.StudentListening ||
                        state == SparState.StudentSpeaking ||
                        state == SparState.Idle ||
                        state is SparState.Error
                // Pulse ring (only when actively listening)
                if (isSpeaking) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .scale(pulseRingScale)
                            .clip(CircleShape)
                            .background(ProLearnColors.Black.copy(alpha = pulseRingAlpha))
                    )
                }

                // Mic button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isSpeaking -> ProLearnColors.Black
                                !canTap -> ProLearnColors.Disabled
                                else -> ProLearnColors.White
                            }
                        )
                        .border(
                            width = if (canTap && !isSpeaking) 1.5.dp else 0.dp,
                            color = ProLearnColors.Black,
                            shape = CircleShape
                        )
                        .then(
                            if (canTap) Modifier.clickable {
                                // Use isRecognizerActive — not state — to decide start vs stop
                                if (isRecognizerActive) {
                                    viewModel.stopListening()
                                } else {
                                    viewModel.startListening()
                                }
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRecognizerActive) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isRecognizerActive) "Stop listening" else "Start speaking",
                        tint = when {
                            isSpeaking -> ProLearnColors.White
                            !canTap -> ProLearnColors.Muted
                            else -> ProLearnColors.Black
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

// ── Top Bar ──────────────────────────────────────────────────────────────────

@Composable
private fun TopBar(
    config: SparConfig,
    timerFormatted: String,
    questionCounter: String,
    hintsRemaining: Int,
    onHintClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subject · Chapter pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(ProLearnColors.Surface)
                    .border(1.dp, ProLearnColors.Border, RoundedCornerShape(100.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    "${config.subject} · ${config.chapter}",
                    fontSize = 11.sp,
                    color = ProLearnColors.Black,
                    maxLines = 1
                )
            }

            Spacer(Modifier.weight(1f))

            // Timer
            Text(
                timerFormatted,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = ProLearnColors.Black
            )

            Spacer(Modifier.weight(1f))

            // Question counter + hint button
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(ProLearnColors.Surface)
                        .border(1.dp, ProLearnColors.Border, RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(questionCounter, fontSize = 11.sp, color = ProLearnColors.Black)
                }

                Spacer(Modifier.width(8.dp))

                // Hint button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (hintsRemaining > 0) ProLearnColors.Surface
                            else ProLearnColors.Disabled
                        )
                        .border(1.dp, ProLearnColors.Border, CircleShape)
                        .then(
                            if (hintsRemaining > 0) Modifier.clickable { onHintClick() }
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (hintsRemaining > 0) ProLearnColors.Black else ProLearnColors.Muted
                    )
                }

                // Hint count badge
                if (hintsRemaining > 0) {
                    Text(
                        "$hintsRemaining",
                        fontSize = 9.sp,
                        color = ProLearnColors.Muted,
                        modifier = Modifier.padding(start = 3.dp)
                    )
                }
            }
        }

        // Thin divider
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ProLearnColors.Border)
        )
    }
}

// ── State Chip Row ────────────────────────────────────────────────────────────

private fun isActiveState(state: SparState) = state is SparState.AiThinking ||
        state is SparState.AiEvaluating ||
        state is SparState.StudentListening ||
        state is SparState.AiSpeaking

@Composable
private fun StateChipRow(state: SparState, statusMessage: String) {
    val (text, icon) = when (state) {
        is SparState.AiThinking -> "Thinking..." to Icons.Default.Pending
        is SparState.AiEvaluating -> "Evaluating your answer..." to Icons.Default.Psychology
        is SparState.StudentListening -> "Listening — tap mic to speak" to Icons.Default.Mic
        is SparState.AiSpeaking -> "AI speaking" to Icons.Default.Mic
        else -> if (statusMessage.isNotBlank()) statusMessage to Icons.Default.Pending
        else return
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(ProLearnColors.Surface)
                .border(1.dp, ProLearnColors.Border, RoundedCornerShape(100.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon, null,
                    tint = ProLearnColors.Muted,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(text, fontSize = 12.sp, color = ProLearnColors.MutedDark)
            }
        }
    }
}

// ── Partial Transcript Bubble ─────────────────────────────────────────────────

@Composable
private fun PartialTranscriptBubble(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 64.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .background(
                    ProLearnColors.Black.copy(alpha = 0.55f),
                    RoundedCornerShape(12.dp, 4.dp, 12.dp, 12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                color = ProLearnColors.White,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

// ── Typing Indicator ──────────────────────────────────────────────────────────

@Composable
private fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 40.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Small AI avatar
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(ProLearnColors.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("AI", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = ProLearnColors.White)
        }
        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .background(ProLearnColors.Surface, RoundedCornerShape(4.dp, 12.dp, 12.dp, 12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val dotOffset by infiniteTransition.animateFloat(
                        initialValue = 0f, targetValue = -5f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(500, delayMillis = index * 160),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .offset(y = dotOffset.dp)
                            .background(ProLearnColors.Muted, CircleShape)
                    )
                }
            }
        }
    }
}

// ── Mic Permission Card ───────────────────────────────────────────────────────

@Composable
private fun MicPermissionCard(
    showRationale: Boolean,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(ProLearnColors.Surface, RoundedCornerShape(12.dp))
            .border(1.dp, ProLearnColors.Border, RoundedCornerShape(12.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.MicOff,
            contentDescription = null,
            tint = ProLearnColors.Muted,
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Microphone access needed",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = ProLearnColors.Black
        )
        Spacer(Modifier.height(6.dp))
        Text(
            if (showRationale)
                "ProLearn needs the mic to hear your answers. Please grant permission to spar."
            else
                "Tap below to allow microphone access for voice sparring.",
            fontSize = 13.sp,
            color = ProLearnColors.Muted,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ProLearnColors.Black)
                .clickable { onRequestPermission() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (showRationale) "Open Settings" else "Allow Microphone",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = ProLearnColors.White
            )
        }
    }
}
