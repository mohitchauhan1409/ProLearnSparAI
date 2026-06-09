package com.prolearn.spar.ui.screens.arena

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.R
import com.prolearn.spar.ui.theme.BricolageGrotesqueFamily
import com.prolearn.spar.ui.theme.ProLearnColors

@Composable
fun ArenaHome(
    onBattleground: () -> Unit,
    onChallenges: () -> Unit,
    onGames: () -> Unit
) {
    val xp by ArenaXpStore.totalXp.collectAsState()
    val rank = ArenaXpStore.rankFor(xp)
    val nextRankXp = when (rank) {
        "Bronze" -> 250
        "Silver" -> 500
        "Gold" -> 800
        "Diamond" -> 1200
        else -> xp
    }
    val rankProgress = if (rank == "Legend") 1f else (xp.toFloat() / nextRankXp).coerceIn(0f, 1f)

    ArenaList {
        item {
            ArenaHero(
                xp = xp,
                rank = rank,
                progress = rankProgress,
                nextRankXp = nextRankXp,
                onBattle = onBattleground
            )
        }
        item {
            SectionTitle(
                "Choose your format",
                "Every Arena mode turns revision into a visible skill loop."
            )
        }
        item {
            ArenaDestinationCard(
                title = "Battleground",
                kicker = "LIVE MATCHMAKING",
                body = "Jump into Erangel squads or a 1v1 Battle Room. Scores move after every answer.",
                artRes = R.drawable.arena_art_battleground,
                accent = Coral,
                reward = "Up to 100 XP",
                players = "60+ online",
                primary = true,
                button = "Find battle",
                onClick = onBattleground
            )
        }
        item {
            ArenaDestinationCard(
                title = "Challenges",
                kicker = "FOCUS MISSIONS",
                body = "Read, watch, answer timestamp checks, and prove recall before XP unlocks.",
                artRes = R.drawable.arena_art_challenges,
                accent = Blue,
                reward = "50 XP each",
                players = "2 ready",
                primary = false,
                button = "Open missions",
                onClick = onChallenges
            )
        }
        item {
            ArenaDestinationCard(
                title = "Games",
                kicker = "PLAY TO MASTER",
                body = "Knowledge Ludo and Word Battlefield turn syllabus recall into real game moves.",
                artRes = R.drawable.arena_art_games,
                accent = Moss,
                reward = "Up to 70 XP",
                players = "4 modes",
                primary = false,
                button = "Enter arcade",
                onClick = onGames
            )
        }
        item {
            ArenaEliteSection()
        }
        item {
            BattleLogSection()
        }
    }
}

private data class ArenaElitePlayer(
    val rank: String,
    val name: String,
    val xp: String,
    val avatarRes: Int,
    val streak: String,
    val accent: Color
)

private data class BattleLogEntry(
    val title: String,
    val meta: String,
    val xp: String,
    val won: Boolean,
    val accent: Color
)

private val arenaElitePlayers = listOf(
    ArenaElitePlayer("01", "ApexPredator", "2,450 XP", R.drawable.arena_avatar_apex, "12 win streak", Gold),
    ArenaElitePlayer("02", "StormShadow", "2,120 XP", R.drawable.arena_avatar_storm, "8 win streak", Blue),
    ArenaElitePlayer("03", "CyberViper", "1,980 XP", R.drawable.arena_avatar_cyber, "6 win streak", Moss)
)

private val battleLogEntries = listOf(
    BattleLogEntry("Victory in Erangel", "Battleground · 2h ago", "+80 XP", true, Moss),
    BattleLogEntry("Defeat in 1v1 Room", "Battleground · 5h ago", "-15 XP", false, Coral),
    BattleLogEntry("Mission: Timestamp Master", "Challenges · 1d ago", "+50 XP", true, Blue)
)

@Composable
private fun ArenaEliteSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeaderWithAction("Arena Elite", "View All")
        arenaElitePlayers.forEachIndexed { index, player ->
            ArenaEliteRow(player, highlighted = index == 0)
        }
    }
}

@Composable
private fun SectionHeaderWithAction(title: String, action: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            title,
            fontSize = 21.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Ink,
            fontFamily = BricolageGrotesqueFamily
        )
        Text(
            action,
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(Ink.copy(alpha = 0.06f))
                .border(1.dp, Ink.copy(alpha = 0.08f), RoundedCornerShape(100.dp))
                .padding(horizontal = 11.dp, vertical = 7.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Moss,
            maxLines = 1
        )
    }
}

@Composable
private fun ArenaEliteRow(player: ArenaElitePlayer, highlighted: Boolean) {
    val borderColor = if (highlighted) Gold.copy(alpha = 0.48f) else Ink.copy(alpha = 0.06f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .shadow(
                if (highlighted) 12.dp else 8.dp,
                RoundedCornerShape(26.dp),
                ambientColor = player.accent.copy(alpha = if (highlighted) 0.13f else 0.06f),
                spotColor = player.accent.copy(alpha = if (highlighted) 0.13f else 0.06f)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.92f),
                        Paper,
                        player.accent.copy(alpha = if (highlighted) 0.14f else 0.08f)
                    ),
                    start = Offset.Zero,
                    end = Offset(760f, 240f)
                )
            )
            .border(1.dp, borderColor, RoundedCornerShape(26.dp))
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.CenterEnd)
                .clip(CircleShape)
                .background(player.accent.copy(alpha = 0.05f))
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                player.rank,
                color = if (highlighted) Gold else ProLearnColors.MutedDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.width(36.dp)
            )
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(player.accent.copy(alpha = 0.16f))
                    .border(2.dp, player.accent.copy(alpha = 0.64f), CircleShape)
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = player.avatarRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    player.name,
                    color = Ink,
                    fontSize = 17.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(player.accent.copy(alpha = 0.11f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = player.accent,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        player.streak,
                        color = player.accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    player.xp,
                    color = Ink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Text(
                    "season XP",
                    color = ProLearnColors.MutedDark,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun BattleLogSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeaderWithAction("Your Battle Log", "History")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    12.dp,
                    RoundedCornerShape(26.dp),
                    ambientColor = Ink.copy(alpha = 0.07f),
                    spotColor = Ink.copy(alpha = 0.07f)
                )
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Color.White.copy(alpha = 0.9f)
                )
                .border(1.dp, GlassStroke, RoundedCornerShape(26.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            battleLogEntries.forEach { entry ->
                BattleLogRow(entry)
            }
        }
    }
}

@Composable
private fun BattleLogRow(entry: BattleLogEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.74f))
            .border(1.dp, entry.accent.copy(alpha = 0.13f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BattleStatusBadge(entry.won, entry.accent)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                entry.title,
                color = Ink,
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                entry.meta,
                color = ProLearnColors.MutedDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(entry.accent.copy(alpha = 0.12f))
                .padding(horizontal = 9.dp, vertical = 6.dp)
        ) {
            Text(
                entry.xp,
                color = entry.accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BattleStatusBadge(won: Boolean, accent: Color) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.13f))
            .border(1.dp, accent.copy(alpha = 0.34f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(25.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.88f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (won) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(if (won) 19.dp else 17.dp)
            )
        }
    }
}

@Composable
fun ArenaHero(
    xp: Int,
    rank: String,
    progress: Float,
    nextRankXp: Int,
    onBattle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                22.dp,
                RoundedCornerShape(30.dp),
                ambientColor = Ink.copy(alpha = 0.10f),
                spotColor = Ink.copy(alpha = 0.10f)
            )
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.linearGradient(
                    listOf(Paper, Color(0xFFF4F0E7), Color(0xFFEDE9DF)),
                    start = Offset.Zero,
                    end = Offset(780f, 520f)
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.88f), RoundedCornerShape(30.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(Ink.copy(alpha = 0.06f))
                            .border(1.dp, Ink.copy(alpha = 0.08f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Arena · Skill league",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Ink,
                            fontFamily = BricolageGrotesqueFamily
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Learn. Compete. Win.",
                        fontSize = 34.sp,
                        lineHeight = 35.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic,
                        color = Ink,
                        fontFamily = BricolageGrotesqueFamily
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Pick a mode, answer under pressure, and turn every correct move into Arena XP.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = ProLearnColors.MutedDark,
                        fontFamily = BricolageGrotesqueFamily
                    )
                }
            }

            Column {
                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GlassMetric("XP", "$xp", Icons.Default.Bolt, Gold, Modifier.weight(1f))
                    GlassMetric(
                        "Rank",
                        rank,
                        Icons.Default.MilitaryTech,
                        Violet,
                        Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(24.dp))
                PremiumButton("Start live battle", Icons.Default.PlayArrow, onBattle)
            }
        }
    }
}

@Composable
fun GlassMetric(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.68f))
            .border(1.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(22.dp))
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(icon, accent, 44)
        Spacer(Modifier.width(9.dp))
        Column {
            Text(label, fontSize = 11.sp, color = ProLearnColors.MutedDark, maxLines = 1)
            Text(
                value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Ink,
                fontStyle = FontStyle.Italic,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ArenaDestinationCard(
    title: String,
    kicker: String,
    body: String,
    artRes: Int,
    accent: Color,
    reward: String,
    players: String,
    primary: Boolean,
    button: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.985f else 1f,
        spring(dampingRatio = 0.72f),
        label = "destinationScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(202.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                12.dp,
                RoundedCornerShape(26.dp),
                ambientColor = Ink.copy(alpha = 0.07f),
                spotColor = Ink.copy(alpha = 0.07f)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    listOf(Paper, Color(0xFFF7F3EB), Color.White.copy(alpha = 0.92f)),
                    start = Offset.Zero,
                    end = Offset(760f, 360f)
                )
            )
            .border(1.dp, Ink.copy(alpha = 0.07f), RoundedCornerShape(26.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)) {
                    Text(
                        kicker,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accent,
                        maxLines = 1
                    )
                    Text(
                        title,
                        fontSize = 24.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Ink
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        body,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = ProLearnColors.MutedDark,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Image(
                    painter = painterResource(id = artRes),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.Top)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DestinationChip(reward, Icons.Default.Star, accent)
                Spacer(Modifier.width(8.dp))
                DestinationChip(players, Icons.Default.Groups, Ink)
                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Ink)
                        .padding(horizontal = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        button,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DestinationChip(text: String, icon: ImageVector, tint: Color) {
    Row(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(Color.White.copy(alpha = 0.74f))
            .border(1.dp, Color.White, RoundedCornerShape(100.dp))
            .padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(5.dp))
        Text(text, color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}
