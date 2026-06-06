package com.prolearn.spar.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.ui.components.ui.Avatar
import com.prolearn.spar.ui.theme.BricolageGrotesqueFamily
import com.prolearn.spar.ui.theme.ProLearnColors
import kotlin.math.absoluteValue

private val PageBg = Color(0xFFF8FAF7)
private val Ink = Color(0xFF151616)
private val Moss = Color(0xFF4E7D68)
private val LimeMist = Color(0xFFEAF6D8)
private val SkyMist = Color(0xFFEAF3FF)
private val BlushMist = Color(0xFFFFEFF3)
private val SoftBorder = Color(0xFFDDE5DC)
private val GlassStroke = Color(0x88FFFFFF)

private data class FeatureCard(
    val eyebrow: String,
    val title: String,
    val body: String,
    val icon: ImageVector,
    val colors: List<Color>,
    val accent: Color,
    val visual: FeatureVisual
)

private enum class FeatureVisual {
    Voice,
    Debate,
    Materials,
    Progress,
    Topics,
    Tutor
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToSparSetup: (String?) -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val viewModel: HomeViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val streak by viewModel.streak.collectAsState()
    val totalSessions by viewModel.totalSessions.collectAsState()
    val totalQuestions by viewModel.totalQuestions.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val greeting = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(PageBg, Color(0xFFF2F7F4), Color(0xFFFFFBF5)),
                    start = Offset.Zero,
                    end = Offset(900f, 1500f)
                )
            )
    ) {
        HomeAmbientGlow()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 128.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            stickyHeader {
                HomeTopBar(
                    greeting = greeting,
                    name = currentUser?.firstName.orEmpty(),
                    initials = currentUser?.avatarInitials ?: "?",
                    onProfile = onNavigateToProfile
                )
            }

            item {
                HeroSection(
                    streak = streak,
                    totalSessions = totalSessions,
                    totalQuestions = totalQuestions
                )
            }

            item {
                FeatureCarousel()
            }
        }

        FloatingActionDock(
            modifier = Modifier.align(Alignment.BottomCenter),
            onStart = { onNavigateToSparSetup(null) }
        )
    }
}

@Composable
private fun HomeTopBar(
    greeting: String,
    name: String,
    initials: String,
    onProfile: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        PageBg.copy(alpha = 0.98f),
                        PageBg.copy(alpha = 0.9f),
                        PageBg.copy(alpha = 0.72f)
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onProfile
                )
                .padding(end = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .shadow(
                        10.dp,
                        CircleShape,
                        ambientColor = Moss.copy(alpha = 0.18f),
                        spotColor = Moss.copy(alpha = 0.18f)
                    )
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, GlassStroke, CircleShape)
                    .padding(2.dp)
            ) {
                Avatar(initials = initials)
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    greeting,
                    fontSize = 11.sp,
                    color = ProLearnColors.Muted,
                    fontFamily = BricolageGrotesqueFamily
                )
                Text(
                    name.ifBlank { "Learner" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = BricolageGrotesqueFamily,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        IconButton(
            onClick = { },
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.72f))
                .border(1.dp, GlassStroke, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = Ink,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun HeroSection(
    streak: Int,
    totalSessions: Int,
    totalQuestions: Int
) {
    AnimatedHomeBlock(index = 0) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color.White.copy(alpha = 0.7f))
                    .border(1.dp, GlassStroke, RoundedCornerShape(100.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AutoAwesome, null, tint = Moss, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "Voice-first study, tuned for Exams",
                    fontSize = 12.sp,
                    color = ProLearnColors.MutedDark,
                    fontFamily = BricolageGrotesqueFamily
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Prolearn Spar AI",
                fontSize = 42.sp,
                lineHeight = 43.sp,
                fontFamily = BricolageGrotesqueFamily,
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Italic,
                color = Ink,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))

            Text(
                "Speak your answer. Get challenged in real time. Leave every session sharper than you entered.",
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontFamily = BricolageGrotesqueFamily,
                color = ProLearnColors.MutedDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 14.dp)
            )

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PremiumStat("Streak", streak.toString(), "days", Modifier.weight(1f))
                PremiumStat("Spars", totalSessions.toString(), "done", Modifier.weight(1f))
                PremiumStat("Questions", totalQuestions.toString(), "answered", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PremiumStat(
    label: String,
    value: String,
    suffix: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "statGlow")
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.38f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "statGlowAlpha"
    )

    Column(
        modifier = modifier
            .height(112.dp)
            .shadow(
                16.dp,
                RoundedCornerShape(24.dp),
                ambientColor = Moss.copy(alpha = 0.1f),
                spotColor = Moss.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.64f)),
                    start = Offset.Zero,
                    end = Offset(220f, 180f)
                )
            )
            .border(1.dp, GlassStroke, RoundedCornerShape(24.dp))
            .padding(horizontal = 9.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(LimeMist.copy(alpha = glow))
            )
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.72f))
                    .border(1.dp, GlassStroke, CircleShape)
            )
            Text(
                value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = BricolageGrotesqueFamily,
                color = Ink,
                maxLines = 1
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            fontSize = 11.sp,
            color = ProLearnColors.MutedDark,
            fontFamily = BricolageGrotesqueFamily,
            maxLines = 1
        )
        Text(
            suffix,
            fontSize = 9.sp,
            color = Moss,
            fontFamily = BricolageGrotesqueFamily,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            fontStyle = FontStyle.Italic,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
        )
    }
}

@Composable
private fun FeatureCarousel() {
    val features = remember {
        listOf(
            FeatureCard(
                eyebrow = "LIVE SPAR",
                title = "Argue with the AI",
                body = "Answer aloud and get pushed with follow-ups like a real tutor.",
                icon = Icons.Default.Mic,
                colors = listOf(Color(0xFFEAF6D8), Color(0xFFFFFFFF), Color(0xFFFFF7E8)),
                accent = Moss,
                visual = FeatureVisual.Voice
            ),
            FeatureCard(
                eyebrow = "THINKING DRILLS",
                title = "Reason under pressure",
                body = "The tutor probes weak steps until the concept actually clicks.",
                icon = Icons.Default.Psychology,
                colors = listOf(Color(0xFFEAF3FF), Color(0xFFFFFFFF), Color(0xFFF2EEFF)),
                accent = Color(0xFF4C6F9F),
                visual = FeatureVisual.Debate
            ),
            FeatureCard(
                eyebrow = "YOUR MATERIALS",
                title = "Study from anything",
                body = "Bring images, PDFs, and YouTube videos into the spar.",
                icon = Icons.Default.PictureAsPdf,
                colors = listOf(Color(0xFFFFF2DA), Color(0xFFFFFFFF), Color(0xFFEAF3FF)),
                accent = Color(0xFF8C673B),
                visual = FeatureVisual.Materials
            ),
            FeatureCard(
                eyebrow = "PROGRESS",
                title = "See mastery form",
                body = "Track sessions, questions, and momentum without visual noise.",
                icon = Icons.Default.GraphicEq,
                colors = listOf(Color(0xFFFFEFF3), Color(0xFFFFFFFF), Color(0xFFEAF6D8)),
                accent = Color(0xFFB45A72),
                visual = FeatureVisual.Progress
            ),
            FeatureCard(
                eyebrow = "CURATED PREP",
                title = "Pick any chapter",
                body = "Jump into subject-specific spars built for exam-style recall.",
                icon = Icons.Default.MenuBook,
                colors = listOf(Color(0xFFEAF6D8), Color(0xFFFFFFFF), Color(0xFFFFF2DA)),
                accent = Color(0xFF99703F),
                visual = FeatureVisual.Topics
            ),
            FeatureCard(
                eyebrow = "AI TEACHERS",
                title = "Choose the voice",
                body = "Study with a tutor personality that keeps you engaged.",
                icon = Icons.Default.School,
                colors = listOf(Color(0xFFF2EEFF), Color(0xFFFFFFFF), Color(0xFFFFEFF3)),
                accent = Color(0xFF705E9F),
                visual = FeatureVisual.Tutor
            )
        )
    }
    val pagerState = rememberPagerState(pageCount = { features.size })

    LaunchedEffect(pagerState, features.size) {
        while (true) {
            kotlinx.coroutines.delay(4000)
            val nextPage = (pagerState.currentPage + 1) % features.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    AnimatedHomeBlock(index = 1) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Explore Spar",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        fontFamily = BricolageGrotesqueFamily
                    )
                    Text(
                        "Swipe through what makes it feel alive",
                        fontSize = 12.sp,
                        color = ProLearnColors.Muted,
                        fontFamily = BricolageGrotesqueFamily
                    )
                }
                PagerDots(count = features.size, current = pagerState.currentPage)
            }

            Spacer(Modifier.height(12.dp))

            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 22.dp),
                pageSpacing = 14.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(292.dp)
            ) { page ->
                val pageOffset =
                    ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                        .absoluteValue
                        .coerceIn(0f, 1f)
                FeatureShowcaseCard(
                    feature = features[page],
                    pageOffset = pageOffset
                )
            }
        }
    }
}

@Composable
private fun FeatureShowcaseCard(
    feature: FeatureCard,
    pageOffset: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "featureMotion")
    val drift by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            tween(2600, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "featureDrift"
    )
    val scale = 0.94f + (1f - pageOffset) * 0.06f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = 0.72f + (1f - pageOffset) * 0.28f
                rotationY = pageOffset * 6f
            }
            .shadow(
                elevation = 22.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = feature.accent.copy(alpha = 0.14f),
                spotColor = feature.accent.copy(alpha = 0.14f)
            )
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = feature.colors,
                    start = Offset.Zero,
                    end = Offset(720f, 520f)
                )
            )
            .border(1.dp, GlassStroke, RoundedCornerShape(28.dp))
    ) {
        Box(
            Modifier
                .size(158.dp)
                .align(Alignment.TopEnd)
                .offset(x = 42.dp, y = (-48).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.42f))
        )
        Box(
            Modifier
                .size(112.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-34).dp, y = 34.dp)
                .clip(CircleShape)
                .background(feature.accent.copy(alpha = 0.08f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.72f))
                        .border(1.dp, GlassStroke, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(feature.icon, null, tint = feature.accent, modifier = Modifier.size(19.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    feature.eyebrow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = feature.accent,
                    fontFamily = BricolageGrotesqueFamily
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                feature.title,
                fontSize = 25.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
                fontFamily = BricolageGrotesqueFamily
            )
            Spacer(Modifier.height(6.dp))
            Text(
                feature.body,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = ProLearnColors.MutedDark,
                fontFamily = BricolageGrotesqueFamily,
                modifier = Modifier.fillMaxWidth(0.86f)
            )

            Spacer(Modifier.weight(1f))

            FeatureVisualArt(
                visual = feature.visual,
                accent = feature.accent,
                drift = drift,
                parallax = pageOffset
            )
        }
    }
}

@Composable
private fun FeatureVisualArt(
    visual: FeatureVisual,
    accent: Color,
    drift: Float,
    parallax: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .graphicsLayer {
                translationX = drift * (1f - parallax)
                translationY = -drift * 0.28f
            }
    ) {
        when (visual) {
            FeatureVisual.Voice -> VoiceVisual(accent)
            FeatureVisual.Debate -> DebateVisual(accent)
            FeatureVisual.Materials -> MaterialsVisual(accent)
            FeatureVisual.Progress -> ProgressVisual(accent)
            FeatureVisual.Topics -> TopicsVisual(accent)
            FeatureVisual.Tutor -> TutorVisual(accent)
        }
    }
}

@Composable
private fun BoxScope.VoiceVisual(accent: Color) {
    Row(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(88.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.58f))
            .border(1.dp, GlassStroke, RoundedCornerShape(24.dp))
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        repeat(13) { index ->
            val height = listOf(18, 28, 44, 32, 58, 36, 68, 40, 56, 30, 42, 24, 34)[index]
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(height.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (index % 3 == 0) accent else accent.copy(alpha = 0.34f))
            )
        }
    }
}

@Composable
private fun DebateVisual(accent: Color) {
    Box(Modifier.fillMaxSize()) {
        BubbleLine("Why that step?", accent, Alignment.TopStart, 0.dp)
        BubbleLine("Because velocity changes.", Ink, Alignment.CenterEnd, 16.dp)
        BubbleLine("Prove it.", accent, Alignment.BottomStart, 34.dp)
    }
}

@Composable
private fun BoxScope.BubbleLine(
    text: String,
    color: Color,
    align: Alignment,
    x: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .align(align)
            .offset(x = x)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.72f))
            .border(1.dp, color.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            fontFamily = BricolageGrotesqueFamily
        )
    }
}

@Composable
private fun MaterialsVisual(accent: Color) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        MaterialSourceTile(
            label = "Image",
            icon = Icons.Default.Image,
            accent = accent,
            modifier = Modifier
                .weight(1f)
                .height(82.dp)
        )
        MaterialSourceTile(
            label = "PDF",
            icon = Icons.Default.PictureAsPdf,
            accent = Color(0xFFB45A72),
            modifier = Modifier
                .weight(1f)
                .height(104.dp)
        )
        MaterialSourceTile(
            label = "Video",
            icon = Icons.Default.PlayCircleFilled,
            accent = Color(0xFF4C6F9F),
            modifier = Modifier
                .weight(1f)
                .height(90.dp)
        )
    }
}

@Composable
private fun MaterialSourceTile(
    label: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.64f))
            .border(1.dp, GlassStroke, RoundedCornerShape(22.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(accent.copy(alpha = 0.18f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.68f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(accent.copy(alpha = 0.68f))
            )
        }
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Ink,
            fontFamily = BricolageGrotesqueFamily,
            maxLines = 1
        )
    }
}

@Composable
private fun ProgressVisual(accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.58f))
            .border(1.dp, GlassStroke, RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listOf(32, 52, 42, 72, 60, 84, 96).forEachIndexed { index, height ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(height.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (index == 6) accent else accent.copy(alpha = 0.24f))
            )
        }
    }
}

@Composable
private fun TopicsVisual(accent: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.58f))
            .border(1.dp, GlassStroke, RoundedCornerShape(24.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("Physics", "Calculus", "Organic Chem").forEachIndexed { index, label ->
            Row(
                modifier = Modifier
                    .fillMaxWidth(if (index == 2) 0.86f else 1f)
                    .height(26.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(
                        if (index == 0) accent.copy(alpha = 0.15f) else Color.White.copy(
                            alpha = 0.72f
                        )
                    )
                    .border(
                        1.dp,
                        accent.copy(alpha = if (index == 0) 0.34f else 0.12f),
                        RoundedCornerShape(100.dp)
                    )
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.68f))
                )
                Spacer(Modifier.width(8.dp))
                Text(label, fontSize = 11.sp, color = Ink, fontFamily = BricolageGrotesqueFamily)
            }
        }
    }
}

@Composable
private fun TutorVisual(accent: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.58f))
            .border(1.dp, GlassStroke, RoundedCornerShape(24.dp))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 5.dp.toPx()
            drawLine(
                color = accent.copy(alpha = 0.32f),
                start = Offset(size.width * 0.2f, size.height * 0.72f),
                end = Offset(size.width * 0.78f, size.height * 0.28f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawLine(
                color = accent.copy(alpha = 0.18f),
                start = Offset(size.width * 0.26f, size.height * 0.32f),
                end = Offset(size.width * 0.84f, size.height * 0.72f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
        listOf(
            Alignment.TopStart to Icons.Default.School,
            Alignment.Center to Icons.Default.AutoAwesome,
            Alignment.BottomEnd to Icons.Default.Mic
        ).forEachIndexed { index, item ->
            Box(
                modifier = Modifier
                    .align(item.first)
                    .padding(16.dp)
                    .size(if (index == 1) 48.dp else 38.dp)
                    .clip(CircleShape)
                    .background(if (index == 1) accent else Color.White.copy(alpha = 0.86f))
                    .border(1.dp, GlassStroke, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.second,
                    contentDescription = null,
                    tint = if (index == 1) Color.White else accent,
                    modifier = Modifier.size(if (index == 1) 22.dp else 18.dp)
                )
            }
        }
    }
}

@Composable
private fun PagerDots(count: Int, current: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            val width by animateDpAsState(
                if (current == index) 18.dp else 6.dp,
                spring(),
                label = "dotWidth"
            )
            val color by animateColorAsState(
                if (current == index) Moss else SoftBorder,
                label = "dotColor"
            )
            Box(
                Modifier
                    .height(6.dp)
                    .width(width)
                    .clip(RoundedCornerShape(100.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun FloatingActionDock(
    modifier: Modifier = Modifier,
    onStart: () -> Unit
) {
    val alpha = remember { Animatable(0f) }
    val y = remember { Animatable(28f) }
    val infiniteTransition = rememberInfiniteTransition(label = "floatingDock")
    val breathe by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.018f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "dockBreathe"
    )

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(260)
        alpha.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
        y.animateTo(0f, spring(dampingRatio = 0.78f, stiffness = 170f))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, PageBg.copy(alpha = 0.92f), PageBg)
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    this.alpha = alpha.value
                    translationY = y.value
                    scaleX = breathe
                    scaleY = breathe
                }
                .shadow(
                    22.dp,
                    RoundedCornerShape(28.dp),
                    ambientColor = Ink.copy(alpha = 0.16f),
                    spotColor = Ink.copy(alpha = 0.16f)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White.copy(alpha = 0.82f))
                .border(1.dp, GlassStroke, RoundedCornerShape(28.dp))
                .padding(10.dp)
        ) {
            PremiumActionButton(
                text = "Start a new study session",
                icon = Icons.Default.AutoAwesome,
                onClick = onStart
            )
        }
    }
}

@Composable
private fun PremiumActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.985f else 1f,
        spring(dampingRatio = 0.72f),
        label = "buttonScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(20.dp))
            .background(Ink)
            .border(
                1.dp,
                LimeMist.copy(alpha = 0.28f),
                RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                null,
                tint = Color.White,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = BricolageGrotesqueFamily
            )
        }
        Icon(
            Icons.Outlined.ArrowForward,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun AnimatedHomeBlock(
    index: Int,
    content: @Composable () -> Unit
) {
    val alpha = remember { Animatable(0f) }
    val y = remember { Animatable(24f) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 80L)
        alpha.animateTo(1f, tween(460, easing = FastOutSlowInEasing))
        y.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 180f))
    }

    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha.value
            translationY = y.value
        }
    ) {
        content()
    }
}

@Composable
private fun BoxScope.HomeAmbientGlow() {
    Box(
        Modifier
            .size(240.dp)
            .offset(x = (-88).dp, y = 76.dp)
            .clip(CircleShape)
            .background(LimeMist.copy(alpha = 0.58f))
    )
    Box(
        Modifier
            .size(210.dp)
            .offset(x = 248.dp, y = 12.dp)
            .clip(CircleShape)
            .background(SkyMist.copy(alpha = 0.68f))
    )
    Box(
        Modifier
            .size(190.dp)
            .offset(x = 252.dp, y = 510.dp)
            .clip(CircleShape)
            .background(BlushMist.copy(alpha = 0.58f))
    )
    Box(
        Modifier
            .fillMaxWidth()
            .height(180.dp)
            .align(Alignment.BottomCenter)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        PageBg.copy(alpha = 0.74f)
                    )
                )
            )
    )
}
