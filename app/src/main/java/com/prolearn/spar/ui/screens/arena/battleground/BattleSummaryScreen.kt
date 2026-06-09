package com.prolearn.spar.ui.screens.arena.battleground

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.ui.screens.arena.ArenaPlayer
import com.prolearn.spar.ui.screens.arena.ArenaXpStore
import com.prolearn.spar.ui.screens.arena.BattleArena
import com.prolearn.spar.ui.screens.arena.Confetti
import com.prolearn.spar.ui.screens.arena.GlassStroke
import com.prolearn.spar.ui.screens.arena.Gold
import com.prolearn.spar.ui.screens.arena.Ink
import com.prolearn.spar.ui.screens.arena.Mint
import com.prolearn.spar.ui.screens.arena.Moss
import com.prolearn.spar.ui.screens.arena.Paper
import com.prolearn.spar.ui.screens.arena.SoftBorder
import com.prolearn.spar.ui.theme.BricolageGrotesqueFamily
import com.prolearn.spar.ui.theme.ProLearnColors

@Composable
internal fun BattleSummaryScreen(
    kind: BattleKind,
    arena: BattleArena,
    players: List<ArenaPlayer>,
    score: Int,
    baseXp: Int,
    onPlayAgain: () -> Unit,
    onChangeArena: () -> Unit
) {
    val yourRank = players.indexOfFirst { it.name == "You" }.let { if (it == -1) players.size else it + 1 }
    val podiumBonus = if (kind == BattleKind.Erangel) when (yourRank) {
        1 -> 50
        2 -> 40
        3 -> 30
        else -> 0
    } else if (yourRank == 1) 50 else 0
    val earned = (baseXp + podiumBonus).coerceAtLeast(0)
    var awarded by rememberSaveable(arena.id, kind) { mutableStateOf(false) }
    val trophyScale by rememberInfiniteTransition(label = "summaryTrophy").animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "trophyScale"
    )

    LaunchedEffect(Unit) {
        if (!awarded) {
            ArenaXpStore.addXp(earned)
            awarded = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                18.dp,
                RoundedCornerShape(30.dp),
                ambientColor = (if (yourRank == 1) Gold else arena.accent).copy(alpha = 0.16f),
                spotColor = (if (yourRank == 1) Gold else arena.accent).copy(alpha = 0.16f)
            )
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Paper,
                        if (yourRank == 1) Gold.copy(alpha = 0.16f) else arena.accent.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.92f)
                    )
                )
            )
            .border(1.dp, GlassStroke, RoundedCornerShape(30.dp))
            .padding(16.dp)
    ) {
        Confetti()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(118.dp)
                    .graphicsLayer { scaleX = trophyScale; scaleY = trophyScale }
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.78f))
                    .border(1.dp, Gold.copy(alpha = 0.30f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (yourRank == 1) Icons.Default.EmojiEvents else Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = if (yourRank == 1) Gold else arena.accent,
                    modifier = Modifier.size(58.dp)
                )
            }
        }
        Text(
            if (yourRank == 1) "Victory" else "Battle complete",
            fontSize = 34.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            fontFamily = BricolageGrotesqueFamily
        )
        Text(
            "Rank #$yourRank · Score $score · XP earned $earned",
            color = ProLearnColors.MutedDark,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            SummaryMetric("Rank", "#$yourRank", Modifier.weight(1f))
            SummaryMetric("Score", score.toString(), Modifier.weight(1f))
            SummaryMetric("XP", "+$earned", Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Text("Leaderboard", color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        players.take(if (kind == BattleKind.Erangel) 5 else 2).forEachIndexed { index, player ->
            PodiumRow(index + 1, player, player.name == "You")
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            SummaryAction(
                text = "Change Arena",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                accent = arena.accent,
                filled = false,
                modifier = Modifier.weight(1f),
                onClick = onChangeArena
            )
            SummaryAction(
                text = "Play Again",
                icon = Icons.Default.Replay,
                accent = Ink,
                filled = true,
                modifier = Modifier.weight(1f),
                onClick = onPlayAgain
            )
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.72f))
            .border(1.dp, SoftBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = ProLearnColors.MutedDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Ink, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
    }
}

@Composable
private fun PodiumRow(rank: Int, player: ArenaPlayer, isYou: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isYou) Mint.copy(alpha = 0.82f) else Color.White.copy(alpha = 0.62f))
            .border(1.dp, if (isYou) Moss.copy(alpha = 0.24f) else SoftBorder, RoundedCornerShape(18.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("#$rank", color = if (rank <= 3) Gold else Ink, fontWeight = FontWeight.ExtraBold, modifier = Modifier.width(34.dp))
        SummaryAvatar(player, size = 36)
        Spacer(Modifier.width(10.dp))
        Text(player.name, Modifier.weight(1f), color = Ink, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("${player.score}", color = Moss, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun SummaryAvatar(player: ArenaPlayer, modifier: Modifier = Modifier, size: Int = 44) {
    Box(
        modifier
            .size(size.dp)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(Mint)
            .border(2.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
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
    }
}

@Composable
private fun SummaryAction(
    text: String,
    icon: ImageVector,
    accent: Color,
    filled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (filled) accent else Color.White.copy(alpha = 0.76f))
            .border(1.dp, if (filled) accent else accent.copy(alpha = 0.24f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text,
            color = if (filled) Color.White else Ink,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            maxLines = 1
        )
    }
}
