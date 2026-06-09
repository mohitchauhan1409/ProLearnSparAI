package com.prolearn.spar.ui.screens.arena

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.ui.theme.BricolageGrotesqueFamily
import com.prolearn.spar.ui.theme.ProLearnColors
import kotlinx.coroutines.launch

@Composable
fun ArenaList(content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 118.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

@Composable
fun ArenaHeader(
    title: String,
    subtitle: String,
    action: String?,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 34.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Ink,
                fontFamily = BricolageGrotesqueFamily
            )
            Text(
                subtitle,
                fontSize = 14.sp,
                color = ProLearnColors.MutedDark,
                fontFamily = BricolageGrotesqueFamily
            )
        }
        if (action != null && onAction != null) {
            MiniButton(action, onAction)
        }
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String) {
    Column(Modifier
        .fillMaxWidth()
        .padding(top = 16.dp)) {
        Text(
            title,
            fontSize = 21.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Ink,
            fontFamily = BricolageGrotesqueFamily
        )
        Text(
            subtitle,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = ProLearnColors.MutedDark,
            fontFamily = BricolageGrotesqueFamily
        )
    }
}

@Composable
fun ModeCard(
    title: String,
    body: String,
    icon: ImageVector,
    accent: Color,
    button: String,
    onClick: () -> Unit
) {
    PremiumPanel(accent = accent) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon, accent)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text(body, fontSize = 13.sp, color = ProLearnColors.MutedDark, lineHeight = 18.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        PremiumButton(button, Icons.Default.Bolt, onClick)
    }
}

@Composable
fun PremiumPanel(accent: Color = Moss, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                18.dp,
                RoundedCornerShape(28.dp),
                ambientColor = accent.copy(alpha = 0.10f),
                spotColor = accent.copy(alpha = 0.10f)
            )
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.82f))
            .border(1.dp, GlassStroke, RoundedCornerShape(28.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
fun PremiumButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Ink)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            fontFamily = BricolageGrotesqueFamily
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun MiniButton(text: String, onClick: () -> Unit) {
    Text(
        text,
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Color.White.copy(alpha = 0.82f))
            .border(1.dp, SoftBorder, RoundedCornerShape(100.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 9.dp),
        color = Ink,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun StatPill(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(62.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.82f))
            .border(1.dp, GlassStroke, RoundedCornerShape(22.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(icon, accent, 34)
        Spacer(Modifier.width(9.dp))
        Column {
            Text(label, fontSize = 11.sp, color = ProLearnColors.MutedDark)
            Text(
                value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun IconBadge(icon: ImageVector, accent: Color, size: Int = 44) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = accent, modifier = Modifier.size((size * 0.48f).dp))
    }
}

@Composable
fun ComingSoonCard(
    title: String,
    body: String,
    icon: ImageVector,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    ComingSoonStatic(title, body, icon) {
        scope.launch { snackbarHostState.showSnackbar("$title notifications enabled") }
    }
}

@Composable
fun ComingSoonStatic(
    title: String,
    body: String,
    icon: ImageVector,
    onNotify: (() -> Unit)? = null
) {
    PremiumPanel(accent = Gold) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon, Gold)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
                Text(body, fontSize = 13.sp, lineHeight = 18.sp, color = ProLearnColors.MutedDark)
            }
        }
        Spacer(Modifier.height(12.dp))
        MiniButton(if (onNotify == null) "Coming Soon" else "Notify Me", onNotify ?: {})
    }
}

@Composable
fun Confetti() {
    val phase by rememberInfiniteTransition(label = "confetti").animateFloat(
        0f,
        1f,
        infiniteRepeatable(tween(1300), RepeatMode.Restart),
        label = "phase"
    )
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(42.dp)
    ) {
        repeat(18) { i ->
            val x = size.width * ((i * 37 % 100) / 100f)
            val y = size.height * (((phase + i * 0.13f) % 1f))
            drawCircle(listOf(Gold, Coral, Blue, Moss)[i % 4], 4f, Offset(x, y))
        }
    }
}

@Composable
fun BoxScope.ArenaAmbientBackdrop() {
    val drift by rememberInfiniteTransition(label = "arenaAmbient").animateFloat(
        -9f,
        9f,
        infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "drift"
    )
    Box(
        Modifier
            .size(240.dp)
            .offset(x = (-82).dp, y = (78 + drift).dp)
            .clip(CircleShape)
            .background(Mint.copy(alpha = 0.58f))
    )
    Box(
        Modifier
            .size(220.dp)
            .align(Alignment.TopEnd)
            .offset(x = 74.dp, y = (18 - drift).dp)
            .clip(CircleShape)
            .background(Sky.copy(alpha = 0.62f))
    )
    Box(
        Modifier
            .size(190.dp)
            .align(Alignment.BottomEnd)
            .offset(x = 54.dp, y = (-120).dp)
            .clip(CircleShape)
            .background(Blush.copy(alpha = 0.58f))
    )
}
