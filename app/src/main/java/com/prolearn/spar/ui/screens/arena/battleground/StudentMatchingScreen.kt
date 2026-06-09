package com.prolearn.spar.ui.screens.arena.battleground

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.ui.screens.arena.ArenaPlayer
import com.prolearn.spar.ui.screens.arena.BattleArena
import com.prolearn.spar.ui.screens.arena.Coral
import com.prolearn.spar.ui.screens.arena.Gold
import com.prolearn.spar.ui.screens.arena.Ink
import com.prolearn.spar.ui.screens.arena.Mint
import com.prolearn.spar.ui.screens.arena.Moss
import com.prolearn.spar.ui.screens.arena.Paper
import com.prolearn.spar.ui.screens.arena.PremiumPanel
import com.prolearn.spar.ui.screens.arena.SoftBorder
import com.prolearn.spar.ui.theme.BricolageGrotesqueFamily
import com.prolearn.spar.ui.theme.ProLearnColors
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun StudentMatchingScreen(
    kind: BattleKind,
    arena: BattleArena,
    players: List<ArenaPlayer>,
    onComplete: () -> Unit
) {
    val visiblePlayers = remember(kind, players) {
        if (kind == BattleKind.Erangel) players else players.take(2)
    }
    var foundCount by remember(kind, arena.id) { mutableIntStateOf(if (kind == BattleKind.Duel) 1 else 0) }
    val pulse by rememberInfiniteTransition(label = "matchPulse").animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(760, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val rotate by rememberInfiniteTransition(label = "matchOrbit").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4200, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "orbit"
    )

    LaunchedEffect(kind, arena.id) {
        foundCount = if (kind == BattleKind.Duel) 1 else 0
        repeat(MatchmakingSecondsTotal) { second ->
            delay(1000)
            foundCount = if (kind == BattleKind.Duel) {
                if (second + 1 >= 7) 2 else 1
            } else {
                (second + 1).coerceAtMost(visiblePlayers.size)
            }
        }
        onComplete()
    }

    PremiumPanel(accent = arena.accent, bgColor = Color.Transparent) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp, top = 140.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .size(132.dp)
                        .graphicsLayer { scaleX = pulse; scaleY = pulse }
                        .clip(CircleShape)
                        .background(arena.accent.copy(alpha = 0.14f))
                        .border(1.dp, arena.accent.copy(alpha = 0.22f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Radar, null, tint = arena.accent, modifier = Modifier.size(56.dp))
                }

                visiblePlayers.forEachIndexed { index, player ->
                    val angle = Math.toRadians((index * (360.0 / visiblePlayers.size)) + rotate)
                    MatchingStudentAvatar(
                        player = player,
                        revealed = index < foundCount,
                        modifier = Modifier.offset(
                            x = (cos(angle) * 128).dp,
                            y = (sin(angle) * 128).dp
                        ),
                        size = 50
                    )
                }
            }

            Spacer(Modifier.height(26.dp))
            Text(
                if (kind == BattleKind.Erangel) "Finding 10 rank-matched students" else "Finding your duel opponent",
                color = Ink,
                fontSize = 24.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
                fontFamily = BricolageGrotesqueFamily
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "${arena.subject} · ${arena.exam} · ${arena.difficulty}",
                color = ProLearnColors.MutedDark,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(12.dp))
            MatchingProgress(foundCount, visiblePlayers.size, arena.accent)
        }
    }
}

@Composable
internal fun BattleStartScreen(
    kind: BattleKind,
    arena: BattleArena,
    countdown: Int,
    players: List<ArenaPlayer>,
    onTick: (Int) -> Unit,
    onComplete: () -> Unit
) {
    val scale by rememberInfiniteTransition(label = "startPulse").animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(620, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "startScale"
    )
    val glow by rememberInfiniteTransition(label = "startGlow").animateFloat(
        initialValue = 0.10f,
        targetValue = 0.30f,
        animationSpec = infiniteRepeatable(tween(780, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "startGlowValue"
    )

    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000)
            onTick(countdown - 1)
        } else {
            onComplete()
        }
    }

    if (kind == BattleKind.Duel) {
        DuelBattleStartScreen(
            arena = arena,
            countdown = countdown,
            you = players.firstOrNull(),
            opponent = players.getOrNull(1),
            scale = scale,
            glow = glow
        )
        return
    }

    PremiumPanel(accent = arena.accent) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Groups, null, tint = arena.accent, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                if (kind == BattleKind.Erangel) "10 students locked" else "Duel opponent locked",
                modifier = Modifier.weight(1f),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )
            Text(
                arena.difficulty,
                color = ProLearnColors.MutedDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(14.dp))
        players.take(if (kind == BattleKind.Erangel) 5 else 2).forEach { LobbyPlayerRow(it) }
        Spacer(Modifier.height(30.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            arena.accent.copy(alpha = 0.10f + glow),
                            Gold.copy(alpha = 0.16f),
                            Paper
                        )
                    )
                )
                .border(1.dp, arena.accent.copy(alpha = 0.20f), RoundedCornerShape(30.dp)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.RocketLaunch, null, tint = arena.accent, modifier = Modifier.size(34.dp))
                Spacer(Modifier.height(10.dp))
                Text(
                    "Battle starts in",
                    color = ProLearnColors.MutedDark,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
                Text(
                    countdown.coerceAtLeast(0).toString(),
                    modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale },
                    color = Ink,
                    fontSize = 82.sp,
                    lineHeight = 84.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = BricolageGrotesqueFamily
                )
                Text(
                    if (countdown <= 2) "Get ready to answer" else "Syncing everyone into the room",
                    color = arena.accent,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun DuelBattleStartScreen(
    arena: BattleArena,
    countdown: Int,
    you: ArenaPlayer?,
    opponent: ArenaPlayer?,
    scale: Float,
    glow: Float
) {
    PremiumPanel(accent = arena.accent) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                "Duel locked",
                color = Ink,
                fontSize = 25.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = BricolageGrotesqueFamily
            )
            Text(
                "Your profile and opponent are synced into the same battle room.",
                color = ProLearnColors.MutedDark,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
            )
            Spacer(Modifier.height(14.dp))
            DuelProfileCard(label = "You", player = you, accent = Moss)
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Gold.copy(alpha = 0.36f + glow), Color.White.copy(alpha = 0.88f))
                        )
                    )
                    .border(1.dp, Gold.copy(alpha = 0.30f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("VS", color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            }
            DuelProfileCard(label = "Opponent", player = opponent, accent = Coral)
            Spacer(Modifier.height(22.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                arena.accent.copy(alpha = 0.10f + glow),
                                Gold.copy(alpha = 0.13f),
                                Paper
                            )
                        )
                    )
                    .border(1.dp, arena.accent.copy(alpha = 0.20f), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Bolt, null, tint = arena.accent, modifier = Modifier.size(30.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Battle starts in", color = ProLearnColors.MutedDark, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    Text(
                        countdown.coerceAtLeast(0).toString(),
                        modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale },
                        color = Ink,
                        fontSize = 64.sp,
                        lineHeight = 66.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = BricolageGrotesqueFamily
                    )
                }
            }
        }
    }
}

@Composable
private fun DuelProfileCard(label: String, player: ArenaPlayer?, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.76f))
            .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(24.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (player != null) {
            MatchingStudentAvatar(player, revealed = true, size = 58)
        } else {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, SoftBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AccountCircle, null, tint = SoftBorder, modifier = Modifier.size(40.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = accent, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            Text(player?.name.orEmpty(), color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, maxLines = 1)
            Text(player?.grade.orEmpty(), color = ProLearnColors.MutedDark, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text("locked", color = accent, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
    }
}

@Composable
private fun MatchingProgress(found: Int, total: Int, accent: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Color.White.copy(alpha = 0.82f))
            .border(1.dp, SoftBorder, RoundedCornerShape(100.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Timer, null, tint = accent, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(7.dp))
        Text(
            "$found/$total students joined",
            color = Ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun MatchingStudentAvatar(
    player: ArenaPlayer,
    revealed: Boolean,
    modifier: Modifier = Modifier,
    size: Int = 44
) {
    Box(
        modifier
            .size(size.dp)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(if (revealed) Mint else Color.White.copy(alpha = 0.92f))
            .border(2.dp, if (revealed) Color.White else SoftBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (revealed) {
            Image(
                painter = painterResource(player.avatarRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            Box(
                Modifier
                    .matchParentSize()
                    .background(Brush.verticalGradient(0f to Color.Transparent, 1f to Color(0x22000000)))
            )
        } else {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                tint = SoftBorder,
                modifier = Modifier.size((size * 0.72f).dp)
            )
        }
    }
}

@Composable
private fun LobbyPlayerRow(player: ArenaPlayer) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MatchingStudentAvatar(player, revealed = true, size = 42)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(player.name, color = Ink, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(player.grade, color = ProLearnColors.MutedDark, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        AnimatedVisibility(visible = true) {
            Text("locked", color = Moss, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
        }
    }
}
