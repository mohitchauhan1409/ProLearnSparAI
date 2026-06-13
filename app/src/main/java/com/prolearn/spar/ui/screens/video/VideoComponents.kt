package com.prolearn.spar.ui.screens.video

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.domain.model.DiagramType
import com.prolearn.spar.domain.model.SceneDiagram
import com.prolearn.spar.domain.model.SceneVisual
import com.prolearn.spar.domain.model.VideoScene
import com.prolearn.spar.ui.theme.BricolageGrotesqueFamily
import com.prolearn.spar.ui.theme.ChalkFontFamily
import com.prolearn.spar.ui.theme.ProLearnColors
import kotlinx.coroutines.delay

// ── Chalkboard palette ──────────────────────────────────────────────────────
private val BoardTop = Color(0xFF2C3D33)
private val BoardBottom = Color(0xFF1E2A23)
private val Chalk = Color(0xFFF1EEDF)
private val ChalkDim = Color(0xFFE7E3D2)
private val WoodLight = Color(0xFF7A5230)
private val WoodDark = Color(0xFF4A3015)
private val TrayColor = Color(0xFF3A2510)

/** A chalk colour + a short label for the scene type. */
internal data class ChalkAccent(val color: Color, val label: String)

internal fun accentFor(visual: String): ChalkAccent = when (SceneVisual.normalize(visual)) {
    SceneVisual.INTRO -> ChalkAccent(Color(0xFFCFE8C4), "Welcome")
    SceneVisual.EXAMPLE -> ChalkAccent(Color(0xFFBFD8EE), "Example")
    SceneVisual.FORMULA -> ChalkAccent(Color(0xFFF3E3A6), "Key idea")
    SceneVisual.COMPARISON -> ChalkAccent(Color(0xFFE6D4F2), "Compare")
    SceneVisual.SUMMARY -> ChalkAccent(Color(0xFFF3C9D4), "Recap")
    else -> ChalkAccent(Chalk, "Concept")
}

/**
 * The chalkboard "stage". The lesson is written out in handwritten chalk, line by
 * line, in sync with the narration. [lineDurations] paces each line's writing to the
 * time its sentence is spoken, so the chalk and the voice stay together.
 */
@Composable
internal fun Chalkboard(
    scene: VideoScene,
    sceneKey: Int,
    revealedLines: Int,
    lineDurations: List<Int>,
    accent: ChalkAccent,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(WoodLight, WoodDark)))
            .padding(10.dp)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.verticalGradient(listOf(BoardTop, BoardBottom)))
        ) {
            // faint chalk dust
            Box(
                Modifier
                    .size(170.dp)
                    .padding(start = 28.dp, top = 36.dp)
                    .clip(RoundedCornerShape(100))
                    .background(Color.White.copy(alpha = 0.022f))
            )

            Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 18.dp)) {
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // scene-type tag
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(7.dp).clip(RoundedCornerShape(100)).background(accent.color)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            accent.label.uppercase(),
                            fontSize = 11.sp,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold,
                            color = accent.color.copy(alpha = 0.85f),
                            fontFamily = BricolageGrotesqueFamily
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // heading
                    ChalkWriteText(
                        text = scene.heading,
                        restartKey = "h$sceneKey",
                        perCharMs = 42,
                        cursorColor = accent.color,
                        fontSize = 32.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        Modifier
                            .fillMaxWidth(0.6f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(100))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        accent.color.copy(alpha = 0.9f),
                                        accent.color.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // featured term
                    scene.keyTerm?.let { term ->
                        Spacer(Modifier.height(16.dp))
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.5.dp, accent.color.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 16.dp, vertical = 11.dp)
                        ) {
                            Text(
                                term,
                                fontSize = 25.sp,
                                lineHeight = 29.sp,
                                fontWeight = FontWeight.Bold,
                                color = accent.color,
                                fontFamily = ChalkFontFamily
                            )
                        }
                    }

                    // explanation lines (written in sync with speech)
                    if (scene.lines.isNotEmpty()) {
                        Spacer(Modifier.height(18.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                            scene.lines.forEachIndexed { i, line ->
                                if (i < revealedLines && line.write.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.Top) {
                                        Text(
                                            "›",
                                            fontSize = 25.sp,
                                            color = accent.color,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = ChalkFontFamily
                                        )
                                        Spacer(Modifier.width(11.dp))
                                        val perChar = ((lineDurations.getOrNull(i) ?: 900) /
                                            line.write.length.coerceAtLeast(1)).coerceIn(45, 200).toLong()
                                        ChalkWriteText(
                                            text = line.write,
                                            restartKey = "l$sceneKey-$i",
                                            perCharMs = perChar,
                                            cursorColor = accent.color,
                                            fontSize = 24.sp,
                                            lineHeight = 29.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = ChalkDim
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // diagram
                    scene.diagram?.let { d ->
                        if (d.isPresent) {
                            Spacer(Modifier.height(20.dp))
                            ChalkDiagram(d, accent.color, sceneKey)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }

                ChalkTray()
            }
        }
    }
}

/** A line of chalk text that "writes itself" character by character, with a chalk cursor. */
@Composable
private fun ChalkWriteText(
    text: String,
    restartKey: Any,
    perCharMs: Long,
    cursorColor: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight,
    color: Color = Chalk,
    modifier: Modifier = Modifier
) {
    var shown by remember(restartKey) { mutableStateOf("") }
    var writing by remember(restartKey) { mutableStateOf(true) }

    LaunchedEffect(restartKey, text) {
        shown = ""; writing = true
        for (i in 1..text.length) {
            shown = text.substring(0, i)
            delay(perCharMs)
        }
        writing = false
    }

    val blink = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by blink.animateFloat(
        0.2f, 1f,
        infiniteRepeatable(tween(480), RepeatMode.Reverse),
        label = "cursorAlpha"
    )

    Text(
        text = buildAnnotatedString {
            append(shown)
            if (writing) {
                withStyle(SpanStyle(color = cursorColor.copy(alpha = cursorAlpha))) { append("▍") }
            }
        },
        fontSize = fontSize,
        lineHeight = lineHeight,
        fontWeight = fontWeight,
        color = color,
        fontFamily = ChalkFontFamily,
        modifier = modifier
    )
}

// ── Diagrams ────────────────────────────────────────────────────────────────

@Composable
private fun ChalkDiagram(diagram: SceneDiagram, color: Color, sceneKey: Int) {
    val appear = remember(sceneKey) { Animatable(0f) }
    LaunchedEffect(sceneKey) { appear.animateTo(1f, tween(550)) }

    Column(
        Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = appear.value
                translationY = (1f - appear.value) * 24f
            }
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val type = DiagramType.normalize(diagram.type)
        when (type) {
            DiagramType.PARTS -> PartsDiagram(diagram.nodes, color)
            else -> FlowOrCycleDiagram(diagram.nodes, color, isCycle = type == DiagramType.CYCLE)
        }
        if (!diagram.caption.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(
                diagram.caption!!,
                fontSize = 16.sp,
                color = ChalkDim.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                fontFamily = ChalkFontFamily
            )
        }
    }
}

@Composable
private fun FlowOrCycleDiagram(nodes: List<String>, color: Color, isCycle: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        nodes.forEachIndexed { i, node ->
            DiagramNode(node, color)
            if (i < nodes.size - 1) ChalkArrow(color)
        }
        if (isCycle && nodes.size > 1) {
            ChalkArrow(color)
            Text(
                "↺ back to the start",
                fontSize = 17.sp,
                color = color,
                fontFamily = ChalkFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PartsDiagram(nodes: List<String>, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (nodes.isNotEmpty()) {
            DiagramNode(nodes.first(), color, emphasised = true)
            if (nodes.size > 1) {
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    nodes.drop(1).forEach { part ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("•", fontSize = 20.sp, color = color, fontFamily = ChalkFontFamily)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                part,
                                fontSize = 19.sp,
                                color = ChalkDim,
                                fontFamily = ChalkFontFamily
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagramNode(label: String, color: Color, emphasised: Boolean = false) {
    Box(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .border(if (emphasised) 2.dp else 1.5.dp, color.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            label,
            fontSize = if (emphasised) 21.sp else 19.sp,
            color = if (emphasised) color else Chalk,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            fontFamily = ChalkFontFamily
        )
    }
}

@Composable
private fun ChalkArrow(color: Color) {
    Text(
        "↓",
        fontSize = 20.sp,
        color = color.copy(alpha = 0.9f),
        fontFamily = ChalkFontFamily,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
private fun ChalkTray() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(13.dp)
            .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp, topStart = 3.dp, topEnd = 3.dp))
            .background(Brush.verticalGradient(listOf(WoodLight.copy(alpha = 0.9f), TrayColor)))
    ) {
        Row(
            Modifier.align(Alignment.CenterStart).padding(start = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ChalkStick(Chalk)
            ChalkStick(Color(0xFFF3E3A6))
            ChalkStick(Color(0xFFF3C9D4))
        }
    }
}

@Composable
private fun ChalkStick(color: Color) {
    Box(
        Modifier
            .width(20.dp)
            .height(6.dp)
            .clip(RoundedCornerShape(100))
            .background(color.copy(alpha = 0.92f))
    )
}

/** Row of scene markers; tap to jump. Dark-on-light, sits below the board. */
@Composable
internal fun SceneDots(
    count: Int,
    current: Int,
    onSeek: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { i ->
            val active = i == current
            val done = i < current
            val interaction = remember { MutableInteractionSource() }
            Box(
                Modifier
                    .then(if (active) Modifier.size(width = 22.dp, height = 6.dp) else Modifier.size(6.dp))
                    .clip(RoundedCornerShape(100))
                    .background(
                        when {
                            active -> ProLearnColors.Black
                            done -> ProLearnColors.MutedDark
                            else -> ProLearnColors.Border
                        }
                    )
                    .clickable(interactionSource = interaction, indication = null, onClick = { onSeek(i) })
            )
        }
    }
}
