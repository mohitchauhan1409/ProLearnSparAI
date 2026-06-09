package com.prolearn.spar.ui.screens.arena.battleground

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.R
import com.prolearn.spar.ui.screens.arena.ArenaFeatureCard
import com.prolearn.spar.ui.screens.arena.ArenaHeader
import com.prolearn.spar.ui.screens.arena.ArenaList
import com.prolearn.spar.ui.screens.arena.BattleArena
import com.prolearn.spar.ui.screens.arena.Blue
import com.prolearn.spar.ui.screens.arena.Coral
import com.prolearn.spar.ui.screens.arena.Gold
import com.prolearn.spar.ui.screens.arena.IconBadge
import com.prolearn.spar.ui.screens.arena.Ink
import com.prolearn.spar.ui.screens.arena.Moss
import com.prolearn.spar.ui.screens.arena.PremiumPanel
import com.prolearn.spar.ui.screens.arena.SoftBorder
import com.prolearn.spar.ui.screens.arena.StatPill
import com.prolearn.spar.ui.screens.arena.Violet
import com.prolearn.spar.ui.screens.arena.battleArenas
import com.prolearn.spar.ui.theme.BricolageGrotesqueFamily
import com.prolearn.spar.ui.theme.ProLearnColors
import kotlinx.coroutines.launch

@Composable
internal fun BattlegroundHomeScreen(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onSelectKind: (BattleKind) -> Unit
) {
    val scope = rememberCoroutineScope()

    ArenaList {
        item { ArenaHeader("Battleground", "Choose your combat style.", "Arena") { onBack() } }
        item {
            ArenaFeatureCard(
                title = "Learn Erangel",
                eyebrow = "Squad Battle",
                body = "Drop into a many-vs-many quiz battle with live rankings, fast streaks, and clutch comeback moments.",
                meta = "Many players · Live leaderboard · 100 XP",
                icon = Icons.Default.Groups,
                accent = Coral,
                imageRes = R.drawable.arena_preview_erangel,
                button = "Start Battle"
            ) { onSelectKind(BattleKind.Erangel) }
        }
        item {
            ArenaFeatureCard(
                title = "Battle Room",
                eyebrow = "1v1 Duel",
                body = "Face one rival in a focused answer duel where every correct response hits harder.",
                meta = "Two players · Health duel · 80 XP",
                icon = Icons.Default.Shield,
                accent = Violet,
                imageRes = R.drawable.arena_preview_battle_room,
                button = "Start Battle"
            ) { onSelectKind(BattleKind.Duel) }
        }
        item {
            ArenaFeatureCard(
                title = "Custom Battle",
                eyebrow = "Private Room",
                body = "Create a room, invite friends or classmates, and let teachers host class battles on their terms.",
                meta = "Invite link · Class rooms · Host controls",
                icon = Icons.Default.GridOn,
                accent = Moss,
                imageRes = R.drawable.arena_preview_custom_battle,
                button = "Notify me",
                comingSoon = true
            ) { scope.launch { snackbarHostState.showSnackbar("Custom Battle notifications enabled") } }
        }
        item {
            ArenaFeatureCard(
                title = "Scheduled Battle",
                eyebrow = "Tournament Slot",
                body = "Plan a battle in advance, send invites, and meet the room exactly when the timer opens.",
                meta = "Calendar match · Reminders · Invites",
                icon = Icons.Default.Timer,
                accent = Blue,
                imageRes = R.drawable.arena_preview_scheduled_battle,
                button = "Notify me",
                comingSoon = true
            ) { scope.launch { snackbarHostState.showSnackbar("Scheduled Battle notifications enabled") } }
        }
        item {
            ArenaFeatureCard(
                title = "Watch Live",
                eyebrow = "Spectator Mode",
                body = "Watch top students battle live, react to tight rounds, and catch featured learning matches.",
                meta = "Live matches · Reactions · Replays",
                icon = Icons.Default.Videocam,
                accent = Gold,
                imageRes = R.drawable.arena_preview_watch_live,
                button = "Notify me",
                comingSoon = true
            ) { scope.launch { snackbarHostState.showSnackbar("Watch Live notifications enabled") } }
        }
    }
}

@Composable
internal fun ArenaPickerScreen(
    kind: BattleKind,
    selectedArena: BattleArena,
    onPlaybook: (BattleArena) -> Unit,
    onStart: (BattleArena) -> Unit
) {
    val arenaRows = battleArenas.chunked(2)

    ArenaList {
        item {
            ArenaHeader(
                title = if (kind == BattleKind.Erangel) "Select Subject" else "1v1 Battle Room",
                subtitle = "Choose a subject and begin your battle.",
                action = "Read playbook",
                onAction = { onPlaybook(selectedArena) }
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
        items(arenaRows) { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                row.forEach { arena ->
                    SubjectArenaCard(
                        arena = arena,
                        modifier = Modifier.weight(1f),
                        onStart = { onStart(arena) }
                    )
                }
            }
        }
    }
}

@Composable
internal fun BattlePlaybookScreen(
    kind: BattleKind,
    arena: BattleArena,
    onBack: () -> Unit,
    onStart: () -> Unit
) {
    ArenaList {
        item { ArenaHeader("Playbook", arena.subject, "Arenas") { onBack() } }
        item {
            PremiumPanel(accent = arena.accent) {
                Text(
                    "How this battle works",
                    fontSize = 28.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Ink,
                    fontFamily = BricolageGrotesqueFamily
                )
                Spacer(Modifier.height(10.dp))
                PlaybookRow(Icons.Default.Groups, "Matchmaking", "You are matched with ${if (kind == BattleKind.Erangel) "9 other students" else "1 student"} near your internal ranking level, based on accuracy, speed, streaks, and recent battle form.")
                PlaybookRow(Icons.AutoMirrored.Filled.MenuBook, "Question source", "${arena.questionPool}. Every question shows whether it is PYQ-tagged or specially curated.")
                PlaybookRow(Icons.Default.Timer, "Question rhythm", "First 10 seconds are reading-only. Options unlock after that. Each question remains live for the full 60 seconds.")
                PlaybookRow(Icons.Default.LocalFireDepartment, "XP scoring", "Correct answer adds 5 XP. Wrong answer deducts 1 XP. Speed and streaks push leaderboard score.")
                PlaybookRow(Icons.Default.EmojiEvents, "Winner rewards", if (kind == BattleKind.Erangel) "Top 3 receive podium bonuses: #1 +50 XP, #2 +40 XP, #3 +30 XP." else "The duel winner receives a +50 XP bonus. The runner-up keeps earned question XP.")
                PlaybookRow(Icons.Default.Shield, "Fairness", "Everyone sees the same question at the same time, with answer timing visible below the question.")
                Spacer(Modifier.height(14.dp))
                SolidBattleAction("Start ${if (kind == BattleKind.Erangel) "Erangel" else "1v1 Battle"}", Icons.AutoMirrored.Filled.ArrowForward, Modifier.fillMaxWidth(), onStart)
            }
        }
    }
}

@Composable
private fun SubjectArenaCard(
    arena: BattleArena,
    modifier: Modifier = Modifier,
    onStart: () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)

    Image(
        painter = painterResource(arena.imageRes),
        contentDescription = arena.subject,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .aspectRatio(1f)
            .shadow(7.dp, shape)
            .clip(shape)
            .border(1.dp, Color.White.copy(alpha = 0.82f), shape)
            .clickable(onClick = onStart)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BattleInfoChips(arena: BattleArena, kind: BattleKind) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InfoChip("${if (kind == BattleKind.Erangel) arena.players else 2} players", arena.accent)
        InfoChip("${if (kind == BattleKind.Erangel) arena.maxXp else 80} max XP", Gold)
        InfoChip(arena.exam, Blue)
        InfoChip(arena.questionPool, Moss)
    }
}

@Composable
private fun BattleSummaryStrip(kind: BattleKind, arena: BattleArena) {
    PremiumPanel(accent = arena.accent) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatPill("Players", if (kind == BattleKind.Erangel) "10" else "2", Icons.Default.Groups, arena.accent, Modifier.weight(1f))
            StatPill("Max XP", "${if (kind == BattleKind.Erangel) arena.maxXp else 80}", Icons.Default.WorkspacePremium, Gold, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatPill("Timer", "10s + 60s", Icons.Default.Timer, Coral, Modifier.weight(1f))
            StatPill("Match", arena.difficulty, Icons.Default.Radar, Moss, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        BattleInfoChips(arena, kind)
    }
}

@Composable
private fun PlaybookRow(icon: ImageVector, title: String, body: String) {
    Row(Modifier.padding(vertical = 9.dp), verticalAlignment = Alignment.Top) {
        IconBadge(icon, Coral, 34)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            Text(body, color = ProLearnColors.MutedDark, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun InfoChip(text: String, accent: Color) {
    Text(
        text,
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .border(1.dp, accent.copy(alpha = 0.20f), RoundedCornerShape(100.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        color = Ink,
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
internal fun SolidBattleAction(text: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(1.dp, SoftBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text, color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, maxLines = 1)
        Spacer(Modifier.width(7.dp))
        Icon(icon, null, tint = Ink, modifier = Modifier.height(17.dp))
    }
}
