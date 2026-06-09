package com.prolearn.spar.ui.screens.arena.challenges

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.prolearn.spar.R
import com.prolearn.spar.ui.screens.arena.ArenaFeatureCard
import com.prolearn.spar.ui.screens.arena.ArenaHeader
import com.prolearn.spar.ui.screens.arena.ArenaList
import com.prolearn.spar.ui.screens.arena.ArenaXpStore
import com.prolearn.spar.ui.screens.arena.Blue
import com.prolearn.spar.ui.screens.arena.Coral
import com.prolearn.spar.ui.screens.arena.Moss
import com.prolearn.spar.ui.screens.arena.Violet
import kotlinx.coroutines.launch

private enum class ChallengePage { Home, Pdf, Video, Summary }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesScreen(
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var page by rememberSaveable { mutableStateOf(ChallengePage.Home) }
    var setup by remember { mutableStateOf<ChallengeSetup?>(null) }
    var result by remember { mutableStateOf<ChallengeResult?>(null) }
    var setupType by remember { mutableStateOf<ChallengeType?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = page != ChallengePage.Home) {
        setup = null
        result = null
        page = ChallengePage.Home
    }

    AnimatedContent(
        targetState = page,
        transitionSpec = {
            val direction = if (targetState.ordinal >= initialState.ordinal) {
                AnimatedContentTransitionScope.SlideDirection.Left
            } else {
                AnimatedContentTransitionScope.SlideDirection.Right
            }
            (slideIntoContainer(
                direction,
                animationSpec = tween(320, easing = FastOutSlowInEasing)
            ) + fadeIn(tween(160))) togetherWith
                (slideOutOfContainer(
                    direction,
                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                ) + fadeOut(tween(160)))
        },
        label = "challengePageTransition"
    ) { targetPage ->
        when (targetPage) {
            ChallengePage.Home -> ChallengeHomeScreen(
                onBack = onBack,
                onPdf = { setupType = ChallengeType.Pdf },
                onVideo = { setupType = ChallengeType.YouTube },
                snackbarHostState = snackbarHostState
            )

            ChallengePage.Pdf -> PdfChallengeScreen(
                setup = setup ?: demoPdfSetup(),
                onBack = { page = ChallengePage.Home },
                onComplete = { completed ->
                    result = completed
                    ArenaXpStore.addXp(completed.xpEarned)
                    page = ChallengePage.Summary
                }
            )

            ChallengePage.Video -> VideoChallengeScreen(
                setup = setup ?: demoVideoSetup(),
                onBack = { page = ChallengePage.Home },
                onComplete = { completed ->
                    result = completed
                    ArenaXpStore.addXp(completed.xpEarned)
                    page = ChallengePage.Summary
                }
            )

            ChallengePage.Summary -> ChallengeSummaryScreen(
                result = result ?: return@AnimatedContent,
                onStartNewChallenge = {
                    setup = null
                    result = null
                    page = ChallengePage.Home
                },
                onBack = {
                    setup = null
                    result = null
                    page = ChallengePage.Home
                }
            )
        }
    }

    val type = setupType
    if (type != null) {
        ChallengeSetupSheet(
            type = type,
            onDismiss = { setupType = null },
            onStart = { selected ->
                setup = selected
                result = null
                setupType = null
                page = if (type == ChallengeType.Pdf) ChallengePage.Pdf else ChallengePage.Video
            },
            sheetState = sheetState
        )
    }
}

@Composable
private fun ChallengeHomeScreen(
    onBack: () -> Unit,
    onPdf: () -> Unit,
    onVideo: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    ArenaList {
        item { ArenaHeader("Challenges", "Study, test, recall, earn.", "Arena") { onBack() } }
        item {
            ArenaFeatureCard(
                title = "PDF Challenge",
                eyebrow = "Read Sprint",
                body = "Choose a level and topic, read a compact demo PDF, then prove recall with MCQs.",
                meta = "Physics demo · 4 min · up to 60 XP",
                icon = Icons.Default.PictureAsPdf,
                accent = Blue,
                imageRes = R.drawable.arena_preview_pdf_challenge,
                button = "Start"
            ) { onPdf() }
        }
        item {
            ArenaFeatureCard(
                title = "YouTube Challenge",
                eyebrow = "Video Checkpoints",
                body = "Pick your challenge setup, watch a focused lesson, then answer timestamp-style questions.",
                meta = "Physics demo · 25m 30s · up to 60 XP",
                icon = Icons.Default.PlayCircle,
                accent = Coral,
                imageRes = R.drawable.arena_preview_youtube_challenge,
                button = "Start"
            ) { onVideo() }
        }
        item {
            ArenaFeatureCard(
                title = "Image Challenge",
                eyebrow = "Visual Recall",
                body = "Label diagrams, circuits, microscope slides, and figures with exam-style prompts.",
                meta = "NEET · Boards · Diagram mastery",
                icon = Icons.Default.Science,
                accent = Moss,
                imageRes = R.drawable.arena_preview_image_challenge,
                button = "Coming soon",
                comingSoon = true
            ) {}
        }
        item {
            ArenaFeatureCard(
                title = "Live Debate Challenge",
                eyebrow = "Argument Arena",
                body = "Get a topic, build your argument, and let students vote on the stronger case.",
                meta = "CUET · Critical thinking · Peer votes",
                icon = Icons.Default.Groups,
                accent = Violet,
                imageRes = R.drawable.arena_preview_watch_live,
                button = "Notify me",
                comingSoon = true
            ) {
                scope.launch { snackbarHostState.showSnackbar("Live Debate Challenge notifications enabled") }
            }
        }
    }
}
