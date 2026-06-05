package com.prolearn.spar.ui.screens.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import androidx.hilt.navigation.compose.hiltViewModel
import com.prolearn.spar.ui.components.ui.ProLearnButton
import com.prolearn.spar.ui.screens.splash.SplashViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private data class OnboardingSlide(
    val imageUrl: String,
    val title: String,
    val body: String
)

private val slides = listOf(
    OnboardingSlide(
        imageUrl = "https://iili.io/CK7lv7R.png",
        title = "Speak, don't type",
        body = "Answer questions out loud. Spar AI listens, challenges you, and adapts in real time."
    ),
    OnboardingSlide(
        imageUrl = "https://iili.io/CK70v6u.png",
        title = "The AI that argues back",
        body = "Get a wrong answer? Spar AI won't just correct you — it pushes you to understand why."
    ),
    OnboardingSlide(
        imageUrl = "https://iili.io/CK71BSf.png",
        title = "Know exactly where you break",
        body = "Every session maps your weak concepts so you know precisely what to fix before exam day."
    )
)

@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit,
    splashViewModel: SplashViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Pre-fetch all images into cache on composition so they're ready instantly
    LaunchedEffect(Unit) {
        val imageLoader = coil.ImageLoader(context)
        slides.forEach { slide ->
            val request = ImageRequest.Builder(context)
                .data(slide.imageUrl)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build()
            imageLoader.enqueue(request)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val slide = slides[page]

            Box(modifier = Modifier.fillMaxSize().padding(40.dp)) {
                // Fullscreen background image
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(slide.imageUrl)
                        .crossfade(300)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    contentDescription = slide.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient scrim at bottom for text legibility
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xCC000000),
                                    Color(0xF0000000)
                                )
                            )
                        )
                )
            }
        }

        // Overlay content pinned to the bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated text for current page
            val slide = slides[pagerState.currentPage]
            val offsetY = remember(pagerState.currentPage) { Animatable(16f) }
            val alpha = remember(pagerState.currentPage) { Animatable(0f) }

            LaunchedEffect(pagerState.currentPage) {
                offsetY.snapTo(16f)
                alpha.snapTo(0f)
                launch { alpha.animateTo(1f, tween(360, easing = FastOutSlowInEasing)) }
                launch { offsetY.animateTo(0f, tween(360, easing = FastOutSlowInEasing)) }
            }

            // Page dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(slides.size) { index ->
                    val isActive = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isActive) 8.dp else 6.dp)
                            .background(
                                if (isActive) Color.White else Color.White.copy(alpha = 0.4f),
                                CircleShape
                            )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // CTA Button with white border
            Box(modifier = Modifier.padding(horizontal = 40.dp)) {
                ProLearnButton(
                    text = if (pagerState.currentPage == slides.size - 1) "Get started" else "Next",
                    onClick = {
                        if (pagerState.currentPage < slides.size - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            splashViewModel.setHasLaunched()
                            onNavigateToLogin()
                        }
                    },
                    backgroundColor = Color.Black.copy(alpha = 0.45f),
                    textColor = Color.White,
                    borderColor = Color.White,
                    borderWidth = 1.5.dp
                )
            }
        }

        // Skip button top-right (hidden on last page)
        if (pagerState.currentPage < slides.size - 1) {
            Text(
                text = "Skip",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.75f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 8.dp, end = 20.dp)
                    .padding(8.dp)
                    .clickable {
                        splashViewModel.setHasLaunched()
                        onNavigateToLogin()
                    }
            )
        }
    }
}
