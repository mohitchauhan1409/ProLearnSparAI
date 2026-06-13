package com.prolearn.spar.ui.screens.arena

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.prolearn.spar.R
import kotlinx.coroutines.launch

@Composable
fun GamesHomeScreen(
    snackbarHostState: SnackbarHostState,
    selectedSubject: String,
    onSubject: (String) -> Unit,
    selectedDifficulty: String,
    onDifficulty: (String) -> Unit,
    onBack: () -> Unit,
    onClueFlip: () -> Unit,
    onXpSpinwheel: () -> Unit
) {
    var setupGame by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize()) {
        ArenaList {
            item {
                ArenaHeader(
                    "Games",
                    "Smart mini-games, real XP stakes.",
                    "Arena"
                ) { onBack() }
            }
            item {
                ArenaFeatureCard(
                    title = "Clue Flip",
                    eyebrow = "Card Logic",
                    body = "Read the question, scan nine hint cards, and flip the right answer before your chances run out.",
                    meta = "Solo · 3 rounds · Up to 90 XP",
                    icon = Icons.Default.AutoAwesome,
                    accent = Moss,
                    imageRes = R.drawable.arena_preview_hint_cards,
                    button = "Play"
                ) {
                    setupGame = "flip"
                }
            }
            item {
                ArenaFeatureCard(
                    title = "XP Spinwheel",
                    eyebrow = "Risk Reward",
                    body = "Spend 5 XP to spin a challenge wheel, solve the landed question set, and bank the reward.",
                    meta = "Solo · 5 XP spin cost · Reward wheel",
                    icon = Icons.Default.Bolt,
                    accent = Coral,
                    imageRes = R.drawable.arena_preview_spin_solve,
                    button = "Spin"
                ) {
                    setupGame = "spin"
                }
            }
            item {
                ArenaFeatureCard(
                    title = "Knowledge Ludo",
                    eyebrow = "Board Battle",
                    body = "A deeper board-race version is being rebuilt with smarter turns, traps, and syllabus rewards.",
                    meta = "4 players · All subjects · Coming soon",
                    icon = Icons.Default.GridOn,
                    accent = Blue,
                    imageRes = R.drawable.arena_preview_knowledge_ludo,
                    button = "Coming soon",
                    comingSoon = true
                ) {
                    scope.launch { snackbarHostState.showSnackbar("Knowledge Ludo is coming soon") }
                }
            }
            item {
                ArenaFeatureCard(
                    title = "Word Battlefield",
                    eyebrow = "Term Control",
                    body = "The territory-word battle is being upgraded with better tiles, rival turns, and subject decks.",
                    meta = "2 players · Science terms · Coming soon",
                    icon = Icons.Default.Shield,
                    accent = Violet,
                    imageRes = R.drawable.arena_preview_word_battlefield,
                    button = "Coming soon",
                    comingSoon = true
                ) {
                    scope.launch { snackbarHostState.showSnackbar("Word Battlefield is coming soon") }
                }
            }
        }
        if (setupGame != null) {
            GameSetupSheet(
                title = if (setupGame == "flip") "Set up Clue Flip" else "Set up XP Spinwheel",
                subtitle = if (setupGame == "flip") {
                    "Choose your subject deck and difficulty before entering the card table."
                } else {
                    "Pick a subject deck. Every spin costs 5 XP before the wheel rolls."
                },
                accent = Moss,
                subject = selectedSubject,
                onSubject = onSubject,
                difficulty = selectedDifficulty,
                onDifficulty = onDifficulty,
                showDifficulty = setupGame == "flip",
                primary = if (setupGame == "flip") "Start card table" else "Enter wheel",
                onStart = {
                    if (setupGame == "flip") onClueFlip() else onXpSpinwheel()
                    setupGame = null
                },
                onDismiss = { setupGame = null }
            )
        }
    }
}
