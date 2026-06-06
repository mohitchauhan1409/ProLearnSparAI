package com.prolearn.spar.ui.screens.spar

import android.Manifest
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.prolearn.spar.R
import com.prolearn.spar.domain.model.Message
import com.prolearn.spar.domain.model.SparConfig
import com.prolearn.spar.domain.model.SparState
import com.prolearn.spar.ui.components.spar.VoiceWaveform
import kotlinx.coroutines.delay

private val DeepInk = Color(0xFF08110F)
private val Ink = Color(0xFF151616)
private val Moss = Color(0xFF4E7D68)
private val MintGlow = Color(0xFFEAF6D8)
private val SkyMist = Color(0xFFEAF3FF)
private val BlushMist = Color(0xFFFFEFF3)
private val Glass = Color(0x1AFFFFFF)
private val GlassStrong = Color(0x2BFFFFFF)
private val GlassStroke = Color(0x4DFFFFFF)
private val SoftText = Color(0xFFDCE6DD)
private val MutedText = Color(0xFF9DAAA1)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LiveSparScreen(
    config: SparConfig,
    onNavigateToReport: () -> Unit,
    viewModel: LiveSparViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val state by viewModel.state.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val audioLevel by viewModel.audioLevel.collectAsState()
    val timerFormatted by viewModel.timerFormatted.collectAsState()
    val partialTranscript by viewModel.partialTranscript.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val isRecognizerActive by viewModel.isRecognizerActive.collectAsState()

    val micPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    var showTypeDialog by remember { mutableStateOf(false) }
    var showYoutubeDialog by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val bytes = context.readUriBytes(uri) ?: return@rememberLauncherForActivityResult
        viewModel.submitImage(
            name = context.displayName(uri) ?: "study-image",
            mimeType = context.contentResolver.getType(uri) ?: "image/jpeg",
            bytes = bytes
        )
    }
    val documentPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val bytes = context.readUriBytes(uri) ?: return@rememberLauncherForActivityResult
        viewModel.submitDocument(
            name = context.displayName(uri) ?: "study-document.pdf",
            mimeType = context.contentResolver.getType(uri) ?: "application/pdf",
            bytes = bytes
        )
    }

    LaunchedEffect(Unit) {
        viewModel.startSession(config)
    }

    LaunchedEffect(state) {
        if (state == SparState.SessionComplete) {
            delay(120)
            onNavigateToReport()
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(messages.size, state, partialTranscript) {
        if (messages.isNotEmpty() || partialTranscript.isNotBlank()) {
            listState.animateScrollToItem(0)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(DeepInk, Color(0xFF10231D), Color(0xFF161915)),
                    start = Offset.Zero,
                    end = Offset(900f, 1500f)
                )
            )
    ) {
        AmbientGlow()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            LiveTopBar(
                config = config,
                timerFormatted = timerFormatted,
                questionCounter = viewModel.questionCounter,
                onEndSession = { viewModel.endSession() }
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                reverseLayout = true,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 232.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (partialTranscript.isNotBlank()) {
                    item(key = "partial") {
                        PartialTranscriptBubble(text = partialTranscript)
                    }
                }

                if (state == SparState.AiThinking || state == SparState.AiEvaluating) {
                    item(key = "typing") {
                        TutorThinkingCard(config = config)
                    }
                }

                items(
                    items = messages.reversed(),
                    key = { msg -> "${msg.timestamp}_${msg.role}" }
                ) { message ->
                    GlassMessageBubble(message = message, config = config)
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, DeepInk.copy(alpha = 0.92f), DeepInk)
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            if (!micPermission.status.isGranted) {
                MicPermissionCard(
                    showRationale = micPermission.status.shouldShowRationale,
                    onRequestPermission = { micPermission.launchPermissionRequest() }
                )
            } else {
                TutorControlDock(
                    state = state,
                    statusMessage = statusMessage,
                    audioLevel = audioLevel,
                    isRecognizerActive = isRecognizerActive,
                    onType = { showTypeDialog = true },
                    onPickImage = { imagePicker.launch("image/*") },
                    onPickDocument = { documentPicker.launch(arrayOf("application/pdf")) },
                    onYoutube = { showYoutubeDialog = true },
                    onStartListening = { viewModel.startListening() },
                    onStopListening = { viewModel.stopListening() },
                    onRetry = { viewModel.retryConnection() }
                )
            }
        }
    }

    TextComposerDialog(
        visible = showTypeDialog,
        title = "Type to tutor",
        placeholder = "Ask anything, paste a question, or explain your doubt...",
        action = "Send",
        onDismiss = { showTypeDialog = false },
        onSubmit = {
            showTypeDialog = false
            viewModel.submitTypedMessage(it)
        }
    )
    TextComposerDialog(
        visible = showYoutubeDialog,
        title = "Study YouTube video",
        placeholder = "Paste YouTube link, topic, or timestamp...",
        action = "Study",
        onDismiss = { showYoutubeDialog = false },
        onSubmit = {
            showYoutubeDialog = false
            viewModel.submitYoutubeLink(it)
        }
    )
}

@Composable
private fun AmbientGlow() {
    Box(
        Modifier
            .size(260.dp)
            .offset(x = (-96).dp, y = 88.dp)
            .clip(CircleShape)
            .background(MintGlow.copy(alpha = 0.12f))
    )
    Box(
        Modifier
            .size(230.dp)
            .offset(x = 238.dp, y = 22.dp)
            .clip(CircleShape)
            .background(SkyMist.copy(alpha = 0.12f))
    )
    Box(
        Modifier
            .size(190.dp)
            .offset(x = 252.dp, y = 520.dp)
            .clip(CircleShape)
            .background(BlushMist.copy(alpha = 0.09f))
    )
}

@Composable
private fun LiveTopBar(
    config: SparConfig,
    timerFormatted: String,
    questionCounter: String,
    onEndSession: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = teacherPortrait(config.voiceId)),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .border(1.dp, MintGlow.copy(alpha = 0.5f), CircleShape)
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    config.voiceName.cleanTeacherName(),
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${config.sessionType} · ${config.difficulty} · $timerFormatted",
                    color = MutedText,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFFFFE1E1).copy(alpha = 0.13f))
                    .border(1.dp, Color(0xFFFFB2B2).copy(alpha = 0.34f), RoundedCornerShape(100.dp))
                    .clickable { onEndSession() }
                    .padding(horizontal = 11.dp, vertical = 7.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Close, null, tint = Color(0xFFFFD7D7), modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "End",
                        color = Color(0xFFFFD7D7),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetaChip(Icons.Default.School, config.subject)
            MetaChip(Icons.Default.AutoAwesome, if (config.chapter == "Generic") "Open chapter" else config.chapter)
            MetaChip(Icons.Default.GraphicEq, questionCounter)
        }
    }
}

@Composable
private fun MetaChip(icon: ImageVector, label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Glass)
            .border(1.dp, GlassStroke, RoundedCornerShape(100.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MintGlow, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(5.dp))
        Text(
            label,
            color = SoftText,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun GlassMessageBubble(message: Message, config: SparConfig) {
    val isQueued = message.role == "queued"
    val isUser = message.role == "user" || isQueued
    val bubbleShape = if (isUser) {
        RoundedCornerShape(22.dp, 8.dp, 22.dp, 22.dp)
    } else {
        RoundedCornerShape(8.dp, 22.dp, 22.dp, 22.dp)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            Image(
                painter = painterResource(id = teacherPortrait(config.voiceId)),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.width(9.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.84f else 0.88f)
                .clip(bubbleShape)
                .background(
                    when {
                        isQueued -> Glass
                        isUser -> Moss
                        else -> GlassStrong
                    }
                )
                .border(
                    1.dp,
                    when {
                        isQueued -> MintGlow.copy(alpha = 0.22f)
                        isUser -> MintGlow.copy(alpha = 0.32f)
                        else -> GlassStroke
                    },
                    bubbleShape
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            if (isQueued) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Pending, null, tint = MintGlow, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "QUEUED",
                        color = MintGlow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
            }
            if (message.isHint) {
                Text(
                    "HINT",
                    color = MintGlow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(4.dp))
            }
            Text(
                message.text,
                color = when {
                    isQueued -> SoftText.copy(alpha = 0.84f)
                    isUser -> Color.White
                    else -> SoftText
                },
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun PartialTranscriptBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.84f)
                .clip(RoundedCornerShape(22.dp, 8.dp, 22.dp, 22.dp))
                .background(Moss.copy(alpha = 0.72f))
                .border(1.dp, MintGlow.copy(alpha = 0.28f), RoundedCornerShape(22.dp, 8.dp, 22.dp, 22.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text,
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
private fun TutorThinkingCard(config: SparConfig) {
    val infiniteTransition = rememberInfiniteTransition(label = "thinking")
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = teacherPortrait(config.voiceId)),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
        )
        Spacer(Modifier.width(9.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp, 22.dp, 22.dp, 22.dp))
                .background(GlassStrong)
                .border(1.dp, GlassStroke, RoundedCornerShape(8.dp, 22.dp, 22.dp, 22.dp))
                .padding(horizontal = 14.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                val offset by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = -5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(520, delayMillis = index * 150),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dot_$index"
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .offset(y = offset.dp)
                        .clip(CircleShape)
                        .background(MintGlow)
                )
            }
        }
    }
}

@Composable
private fun TutorControlDock(
    state: SparState,
    statusMessage: String,
    audioLevel: Float,
    isRecognizerActive: Boolean,
    onType: () -> Unit,
    onPickImage: () -> Unit,
    onPickDocument: () -> Unit,
    onYoutube: () -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onRetry: () -> Unit
) {
    val canTap = state == SparState.StudentListening ||
            state == SparState.StudentSpeaking ||
            state == SparState.Idle ||
            state is SparState.AiSpeaking ||
            state is SparState.Error
    val isListening = state == SparState.StudentListening || state == SparState.StudentSpeaking
    val isAudioActive = state is SparState.AiSpeaking || state == SparState.StudentSpeaking
    val micSize by animateDpAsState(if (isListening) 60.dp else 56.dp, spring(), label = "micSize")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GlassStrong, RoundedCornerShape(26.dp))
            .border(1.dp, GlassStroke, RoundedCornerShape(26.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state is SparState.Error) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFFFE1E1).copy(alpha = 0.16f))
                    .border(1.dp, Color(0xFFFFA3A3).copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                    .clickable { onRetry() }
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("${state.message} · Tap to retry", color = Color(0xFFFFD2D2), fontSize = 12.sp)
            }
        } else {
            StatePill(state = state, statusMessage = statusMessage)
            Spacer(Modifier.height(6.dp))
            VoiceWaveform(
                audioLevel = audioLevel,
                isActive = isAudioActive,
                barCount = 13,
                maxBarHeight = 22.dp,
                barWidth = 2.dp,
                modifier = Modifier.height(24.dp)
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockAction(Icons.Default.Edit, "Type", onType)
            DockAction(Icons.Default.Image, "Image", onPickImage)
            MicDockAction(
                size = micSize,
                canTap = canTap,
                isListening = isListening,
                isRecognizerActive = isRecognizerActive,
                onClick = {
                    if (isRecognizerActive) onStopListening() else onStartListening()
                }
            )
            DockAction(Icons.Default.PictureAsPdf, "PDF", onPickDocument)
            DockAction(Icons.Default.PlayCircleFilled, "YouTube", onYoutube)
        }
    }
}

@Composable
private fun MicDockAction(
    size: androidx.compose.ui.unit.Dp,
    canTap: Boolean,
    isListening: Boolean,
    isRecognizerActive: Boolean,
    onClick: () -> Unit
) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(68.dp)) {
        ListeningPulse(visible = isListening)
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(if (isListening) MintGlow else if (canTap) Color.White else Glass)
                .border(
                    width = 1.dp,
                    color = if (canTap) MintGlow.copy(alpha = 0.55f) else GlassStroke,
                    shape = CircleShape
                )
                .then(if (canTap) Modifier.clickable { onClick() } else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isRecognizerActive) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = if (isRecognizerActive) "Stop listening" else "Start speaking",
                tint = if (isListening) DeepInk else if (canTap) Ink else MutedText,
                modifier = Modifier.size(25.dp)
            )
        }
    }
}

@Composable
private fun DockAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Glass)
            .border(1.dp, GlassStroke, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = label, tint = SoftText, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun ListeningPulse(visible: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.42f,
        animationSpec = infiniteRepeatable(tween(940), RepeatMode.Restart),
        label = "pulseScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.24f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(940), RepeatMode.Restart),
        label = "pulseAlpha"
    )
    if (visible) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(MintGlow.copy(alpha = alpha))
        )
    }
}

@Composable
private fun StatePill(state: SparState, statusMessage: String) {
    val (text, icon) = when (state) {
        is SparState.AiThinking -> statusMessage
            .ifBlank { "Preparing the first move..." } to Icons.Default.Pending
        is SparState.AiEvaluating -> statusMessage
            .ifBlank { "Reading your reasoning..." } to Icons.Default.Psychology
        is SparState.StudentListening -> "Your turn. Tap and speak." to Icons.Default.Mic
        is SparState.StudentSpeaking -> "Listening closely..." to Icons.Default.GraphicEq
        is SparState.AiSpeaking -> {
            val speakingText = if (statusMessage.startsWith("Queued")) {
                "Queued. Tutor is finishing..."
            } else {
                "Tutor is speaking..."
            }
            speakingText to Icons.Default.GraphicEq
        }
        is SparState.Error -> "Connection needs a retry" to Icons.Default.Pending
        else -> (statusMessage.ifBlank { "Ready when you are" }) to Icons.Default.AutoAwesome
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Glass)
            .border(1.dp, GlassStroke, RoundedCornerShape(100.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MintGlow, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(7.dp))
        Text(text, color = SoftText, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MicPermissionCard(
    showRationale: Boolean,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(GlassStrong)
            .border(1.dp, GlassStroke, RoundedCornerShape(28.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.MicOff, null, tint = MintGlow, modifier = Modifier.size(30.dp))
        Spacer(Modifier.height(10.dp))
        Text("Microphone access needed", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            if (showRationale) "Allow mic access so the tutor can hear your answers."
            else "Tap below to enable voice sparring.",
            color = MutedText,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MintGlow)
                .clickable { onRequestPermission() },
            contentAlignment = Alignment.Center
        ) {
            Text("Allow Microphone", color = DeepInk, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun teacherPortrait(voiceId: String): Int = when (voiceId) {
    "LHJy3mhZWsvhUjy0zUM1" -> R.drawable.teacher_pk_anil
    "MF4J4IDTRo0AxOO4dpFR" -> R.drawable.teacher_tripti
    "eUKPwd15VeaPJ9bDZ6iM" -> R.drawable.teacher_manav
    "P7vsEyTOpZ6YUTulin8m" -> R.drawable.teacher_simran
    else -> R.drawable.teacher_manav
}

private fun String.cleanTeacherName(): String =
    replace(Regex("\\s*\\([^)]*\\)\\s*$"), "").trim()

@Composable
private fun TextComposerDialog(
    visible: Boolean,
    title: String,
    placeholder: String,
    action: String,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    if (!visible) return
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF10231D),
        title = {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 21.sp
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (text.isNotBlank()) onSubmit(text)
                }),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Glass)
                    .border(1.dp, GlassStroke, RoundedCornerShape(18.dp))
                    .padding(14.dp),
                decorationBox = { innerTextField ->
                    Box {
                        if (text.isBlank()) {
                            Text(placeholder, color = MutedText, fontSize = 14.sp, lineHeight = 20.sp)
                        }
                        innerTextField()
                    }
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (text.isNotBlank()) onSubmit(text) }
            ) {
                Text(action, color = MintGlow, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MutedText)
            }
        }
    )
}

private fun Context.readUriBytes(uri: Uri): ByteArray? =
    runCatching { contentResolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull()

private fun Context.displayName(uri: Uri): String? =
    runCatching {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                } else null
            }
    }.getOrNull()
