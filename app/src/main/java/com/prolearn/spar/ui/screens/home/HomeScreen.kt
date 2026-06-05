package com.prolearn.spar.ui.screens.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.R
import com.prolearn.spar.ui.components.ui.Avatar
import com.prolearn.spar.ui.theme.BricolageGrotesqueFamily
import com.prolearn.spar.ui.theme.ProLearnColors
import kotlinx.coroutines.delay

data class StatCard(val label: String, val value: Int, val suffix: String = "")

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

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "pulseScale"
    )

    val greeting = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    val featureImages = remember {
        listOf(
            R.drawable.fea_1, R.drawable.fea_2, R.drawable.fea_3, R.drawable.fea_4, R.drawable.fea_5
        )
    }
    val infiniteImages =
        remember(featureImages) { List(500) { featureImages[it % featureImages.size] } }
    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
    LaunchedEffect(lazyListState) {
        lazyListState.scrollToItem(250)
        while (true) {
            lazyListState.animateScrollBy(
                value = 600f, animationSpec = tween(durationMillis = 2500, easing = LinearEasing)
            )
            delay(50)
        }
    }

    // Warm off-white background matching the reference
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ProLearnColors.White)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // Left section - Avatar + Greeting
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.clickable { onNavigateToProfile() }) {
                        Avatar(initials = currentUser?.avatarInitials ?: "?")
                    }

                    Spacer(Modifier.width(10.dp))

                    Column {
                        Text(
                            text = greeting,
                            fontSize = 11.sp,
                            color = ProLearnColors.Muted,
                            fontFamily = BricolageGrotesqueFamily
                        )

                        Text(
                            text = currentUser?.firstName ?: "",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = BricolageGrotesqueFamily,
                            color = ProLearnColors.Black
                        )
                    }
                }

                IconButton(
                    onClick = { /* Navigate to notifications */ }) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = ProLearnColors.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Carousel ────────────────────────────────────────────────────
            LazyRow(
                state = lazyListState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                userScrollEnabled = true
            ) {
                items(infiniteImages.size) { index ->
                    Image(
                        painter = painterResource(id = infiniteImages[index]),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .wrapContentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .border(0.6.dp, ProLearnColors.Muted, RoundedCornerShape(12.dp))
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            // ── Welcome copy ────────────────────────────────────────────────
            Text(
                text = "Welcome to",
                fontSize = 18.sp,
                fontFamily = BricolageGrotesqueFamily,
                fontWeight = FontWeight.Normal,
                color = ProLearnColors.MutedDark,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Prolearn Spar AI!",
                fontSize = 42.sp,
                fontFamily = BricolageGrotesqueFamily,
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Italic,
                color = ProLearnColors.Black,
                textAlign = TextAlign.Center,
                letterSpacing = (-1).sp
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Your voice-first JEE study partner.\nSpeak, answer, get challenged.\nThe AI that argues back.",
                fontSize = 15.sp,
                fontFamily = BricolageGrotesqueFamily,
                fontWeight = FontWeight.Normal,
                color = ProLearnColors.MutedDark,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(Modifier.height(32.dp))

            // ── Primary CTA — Get Started ────────────────────────────────
            Box(
                modifier = Modifier
                    .scale(pulseScale)
                    .padding(horizontal = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(ProLearnColors.Black, RoundedCornerShape(14.dp))
                        .clickable { onNavigateToSparSetup(null) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Get Started",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = BricolageGrotesqueFamily,
                            color = ProLearnColors.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Outlined.ArrowForward,
                            contentDescription = null,
                            tint = ProLearnColors.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Secondary CTA — Continue previous ───────────────────────
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(ProLearnColors.White, RoundedCornerShape(14.dp))
                    .border(1.dp, ProLearnColors.Border, RoundedCornerShape(14.dp))
                    .clickable { onNavigateToProgress() }, contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // "G"-like dot mark
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(ProLearnColors.Surface, CircleShape)
                            .border(1.5.dp, ProLearnColors.Border, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "↺",
                            fontSize = 11.sp,
                            color = ProLearnColors.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Continue previous learning session",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = BricolageGrotesqueFamily,
                        color = ProLearnColors.Black
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Divider row ─────────────────────────────────────────────
            Row(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(ProLearnColors.Border)
                )
                Text(
                    "  Or start a quick spar  ",
                    fontSize = 12.sp,
                    color = ProLearnColors.Muted,
                    fontFamily = BricolageGrotesqueFamily
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(ProLearnColors.Border)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Quick subject chips ──────────────────────────────────────
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.subjects.size) { index ->
                    val subject = viewModel.subjects[index]
                    Box(
                        modifier = Modifier
                            .background(
                                ProLearnColors.White, RoundedCornerShape(20.dp)
                            )
                            .border(1.dp, ProLearnColors.Border, RoundedCornerShape(20.dp))
                            .clickable { onNavigateToSparSetup(subject) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(
                            subject,
                            fontSize = 13.sp,
                            fontFamily = BricolageGrotesqueFamily,
                            fontWeight = FontWeight.Medium,
                            color = ProLearnColors.Black
                        )
                    }
                }
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(ProLearnColors.White, RoundedCornerShape(12.dp))
            .border(1.dp, ProLearnColors.Border, RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = BricolageGrotesqueFamily,
            color = ProLearnColors.Black
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            fontSize = 10.sp,
            color = ProLearnColors.Muted,
            fontFamily = BricolageGrotesqueFamily,
            textAlign = TextAlign.Center
        )
    }
}
