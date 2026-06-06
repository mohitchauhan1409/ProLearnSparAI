package com.prolearn.spar.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.prolearn.spar.R
import com.prolearn.spar.ui.theme.BricolageGrotesqueFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SplashBg = Color(0xFFF8FAF7)
private val SplashInk = Color(0xFF151616)
private val SplashMoss = Color(0xFF4E7D68)
private val SplashLime = Color(0xFFEAF6D8)
private val SplashSky = Color(0xFFEAF3FF)
private val SplashBlush = Color(0xFFFFEFF3)
private val SplashGlassStroke = Color(0x88FFFFFF)

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val destination by viewModel.destination.collectAsState()
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.78f) }
    val logoLift = remember { Animatable(28f) }
    val logoRotation = remember { Animatable(-9f) }
    val logoReveal = remember { Animatable(0f) }
    val detailAlpha = remember { Animatable(0f) }
    val detailLift = remember { Animatable(16f) }

    LaunchedEffect(Unit) {
        launch { logoAlpha.animateTo(1f, tween(640, easing = FastOutSlowInEasing)) }
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        launch { logoLift.animateTo(0f, tween(720, easing = FastOutSlowInEasing)) }
        launch { logoRotation.animateTo(0f, tween(760, easing = FastOutSlowInEasing)) }
        launch { logoReveal.animateTo(1f, tween(860, easing = FastOutSlowInEasing)) }
        delay(440)
        launch { detailAlpha.animateTo(1f, tween(520, easing = FastOutSlowInEasing)) }
        launch { detailLift.animateTo(0f, tween(520, easing = FastOutSlowInEasing)) }

        delay(1500)

        var resolved = destination
        while (resolved == SplashDestination.LOADING) {
            delay(50)
            resolved = viewModel.destination.value
        }

        when (resolved) {
            SplashDestination.SHOW_HOME -> onNavigateToHome()
            SplashDestination.SHOW_LOGIN -> onNavigateToLogin()
            SplashDestination.LOADING -> Unit
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splashMotion")
    val breathe by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "splashBreathe"
    )
    val shimmer by infiniteTransition.animateFloat(
        initialValue = -180f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(tween(2100, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "splashShimmer"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(SplashBg, Color(0xFFF2F7F4), Color(0xFFFFFBF5)),
                    start = Offset.Zero,
                    end = Offset(900f, 1500f)
                )
            )
            .statusBarsPadding()
    ) {
        SplashAmbient()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 42.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.72f))

            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .graphicsLayer {
                            scaleX = breathe
                            scaleY = breathe
                            alpha = logoAlpha.value * 0.72f
                        }
                        .clip(CircleShape)
                        .background(SplashLime.copy(alpha = 0.44f))
                )
                Box(
                    modifier = Modifier
                        .size(214.dp)
                        .graphicsLayer {
                            scaleX = breathe * 0.96f
                            scaleY = breathe * 0.96f
                            alpha = logoAlpha.value
                        }
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.64f))
                        .border(1.dp, SplashGlassStroke, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .width(294.dp)
                        .height(112.dp)
                        .graphicsLayer {
                            alpha = logoAlpha.value
                            scaleX = logoScale.value
                            scaleY = logoScale.value
                            translationY = logoLift.value
                            rotationY = logoRotation.value
                            cameraDistance = 18f * density
                        }
                        .shadow(
                            26.dp,
                            RoundedCornerShape(34.dp),
                            ambientColor = SplashMoss.copy(alpha = 0.14f),
                            spotColor = SplashMoss.copy(alpha = 0.14f)
                        )
                        .clip(RoundedCornerShape(34.dp))
                        .background(Color.White.copy(alpha = 0.82f))
                        .border(1.dp, SplashGlassStroke, RoundedCornerShape(34.dp))
                        .padding(horizontal = 22.dp, vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.prolearn_logo_withoutbg),
                            contentDescription = "ProLearn",
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    alpha = logoReveal.value
                                    scaleX = 0.9f + (0.1f * logoReveal.value)
                                    scaleY = 0.9f + (0.1f * logoReveal.value)
                                    translationX = (1f - logoReveal.value) * -18f
                                }
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxWidth(1f - logoReveal.value)
                            .height(80.dp)
                            .background(Color.White.copy(alpha = 0.92f))
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .width(34.dp)
                            .height(92.dp)
                            .offset(x = (logoReveal.value * 292f).dp)
                            .graphicsLayer {
                                alpha = (1f - logoReveal.value).coerceAtLeast(0f)
                            }
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        SplashMoss.copy(alpha = 0.14f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .width(70.dp)
                            .height(150.dp)
                            .offset(x = shimmer.dp)
                            .graphicsLayer { rotationZ = 16f }
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.28f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            }

            Spacer(Modifier.height(26.dp))

            Column(
                modifier = Modifier.graphicsLayer {
                    alpha = detailAlpha.value
                    translationY = detailLift.value
                },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "ProLearn AI",
                    fontSize = 36.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Italic,
                    fontFamily = BricolageGrotesqueFamily,
                    color = SplashInk,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color.White.copy(alpha = 0.72f))
                        .border(1.dp, SplashGlassStroke, RoundedCornerShape(100.dp))
                        .padding(horizontal = 13.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, null, tint = SplashMoss, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(7.dp))
                    Text(
                        "Voice-first learning, tuned for focus",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = BricolageGrotesqueFamily,
                        color = Color(0xFF555555)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            SplashLoadingDots(
                modifier = Modifier.graphicsLayer {
                    alpha = detailAlpha.value
                    translationY = detailLift.value
                }
            )
        }
    }
}

@Composable
private fun BoxScope.SplashAmbient() {
    Box(
        Modifier
            .size(240.dp)
            .offset(x = (-88).dp, y = 76.dp)
            .clip(CircleShape)
            .background(SplashLime.copy(alpha = 0.58f))
    )
    Box(
        Modifier
            .size(210.dp)
            .align(Alignment.TopEnd)
            .offset(x = 70.dp, y = 18.dp)
            .clip(CircleShape)
            .background(SplashSky.copy(alpha = 0.68f))
    )
    Box(
        Modifier
            .size(190.dp)
            .align(Alignment.BottomEnd)
            .offset(x = 56.dp, y = (-110).dp)
            .clip(CircleShape)
            .background(SplashBlush.copy(alpha = 0.58f))
    )
}

@Composable
private fun SplashLoadingDots(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "splashDots")
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val y by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(620, delayMillis = index * 130, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "splashDot$index"
            )
            Box(
                modifier = Modifier
                    .offset(y = y.dp)
                    .size(if (index == 1) 8.dp else 7.dp)
                    .clip(CircleShape)
                    .background(if (index == 1) SplashMoss else SplashMoss.copy(alpha = 0.42f))
            )
        }
    }
}
