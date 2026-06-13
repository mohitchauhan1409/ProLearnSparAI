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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PictureAsPdf
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.R
import com.prolearn.spar.ui.components.navigation.MainTab
import com.prolearn.spar.ui.components.navigation.ProLearnBottomNav
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
private const val FeatureArtworkAspectRatio = 2.6013987f

private data class FeatureCard(
    val eyebrow: String,
    val title: String,
    val body: String,
    val icon: ImageVector,
    val colors: List<Color>,
    val accent: Color,
    val imageRes: Int
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToSparSetup: (String?) -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToArena: () -> Unit,
    onNavigateToVideoLessons: () -> Unit
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
                VideoLessonsBanner(onClick = onNavigateToVideoLessons)
            }

            item {
                FeatureCarousel()
            }
        }

        FloatingActionDock(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 78.dp),
            onStart = { onNavigateToSparSetup(null) }
        )

        ProLearnBottomNav(
            selected = MainTab.Home,
            onHome = {},
            onArena = onNavigateToArena,
            onProfile = onNavigateToProfile,
            modifier = Modifier.align(Alignment.BottomCenter)
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
                "ProLearn AI",
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
                PremiumStat("Sessions", totalSessions.toString(), "done", Modifier.weight(1f))
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
                eyebrow = "LIVE SESSION",
                title = "Argue with the AI",
                body = "Answer aloud and get pushed with follow-ups like a real tutor.",
                icon = Icons.Default.Mic,
                colors = listOf(Color(0xFFEAF6D8), Color(0xFFFFFFFF), Color(0xFFFFF7E8)),
                accent = Moss,
                imageRes = R.drawable.home_feature_voice
            ),
            FeatureCard(
                eyebrow = "THINKING DRILLS",
                title = "Reason under pressure",
                body = "The tutor probes weak steps until the concept actually clicks.",
                icon = Icons.Default.Psychology,
                colors = listOf(Color(0xFFEAF3FF), Color(0xFFFFFFFF), Color(0xFFF2EEFF)),
                accent = Color(0xFF4C6F9F),
                imageRes = R.drawable.home_feature_debate
            ),
            FeatureCard(
                eyebrow = "YOUR MATERIALS",
                title = "Study from anything",
                body = "Bring images, PDFs, and YouTube videos into the session.",
                icon = Icons.Default.PictureAsPdf,
                colors = listOf(Color(0xFFFFF2DA), Color(0xFFFFFFFF), Color(0xFFEAF3FF)),
                accent = Color(0xFF8C673B),
                imageRes = R.drawable.home_feature_materials
            ),
            FeatureCard(
                eyebrow = "PROGRESS",
                title = "See mastery form",
                body = "Track sessions, questions, and momentum without visual noise.",
                icon = Icons.Default.GraphicEq,
                colors = listOf(Color(0xFFFFEFF3), Color(0xFFFFFFFF), Color(0xFFEAF6D8)),
                accent = Color(0xFFB45A72),
                imageRes = R.drawable.home_feature_progress
            ),
            FeatureCard(
                eyebrow = "CURATED PREP",
                title = "Pick any chapter",
                body = "Jump into subject-specific sessions built for exam-style recall.",
                icon = Icons.Default.MenuBook,
                colors = listOf(Color(0xFFEAF6D8), Color(0xFFFFFFFF), Color(0xFFFFF2DA)),
                accent = Color(0xFF99703F),
                imageRes = R.drawable.home_feature_topics
            ),
            FeatureCard(
                eyebrow = "AI TEACHERS",
                title = "Choose the voice",
                body = "Study with a tutor personality that keeps you engaged.",
                icon = Icons.Default.School,
                colors = listOf(Color(0xFFF2EEFF), Color(0xFFFFFFFF), Color(0xFFFFEFF3)),
                accent = Color(0xFF705E9F),
                imageRes = R.drawable.home_feature_tutor
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
                        "Explore sessions",
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

            Spacer(Modifier.height(10.dp))

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

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(FeatureArtworkAspectRatio)
                    .graphicsLayer {
                        translationX = drift * (1f - pageOffset)
                        translationY = -drift * 0.18f
                    }
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.54f))
                    .border(1.dp, GlassStroke, RoundedCornerShape(24.dp))
            ) {
                Image(
                    painter = painterResource(id = feature.imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.White.copy(alpha = 0.1f))
                            )
                        )
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
    val shineTransition = rememberInfiniteTransition(label = "premiumButtonShine")
    val shineOffset by shineTransition.animateFloat(
        initialValue = -180f,
        targetValue = 420f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "premiumButtonShineOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
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
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(96.dp)
                .offset(x = shineOffset.dp)
                .graphicsLayer { rotationZ = 18f }
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.28f),
                            Color.White.copy(alpha = 0.55f),
                            Color.White.copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    )
                )
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = BricolageGrotesqueFamily
            )
            Spacer(Modifier.width(12.dp))
            Icon(
                Icons.Outlined.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun VideoLessonsBanner(onClick: () -> Unit) {
    AnimatedHomeBlock(index = 1) {
        val interaction = remember { MutableInteractionSource() }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(listOf(Ink, Moss))
                )
                .border(1.dp, GlassStroke, RoundedCornerShape(24.dp))
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    onClick = onClick
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "AI Video Lessons",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = BricolageGrotesqueFamily
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(100))
                            .background(Color.White.copy(alpha = 0.18f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "NEW",
                            fontSize = 9.sp,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = BricolageGrotesqueFamily
                        )
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    "Type any topic, get a narrated lesson",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.82f),
                    fontFamily = BricolageGrotesqueFamily
                )
            }
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = Ink,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
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
