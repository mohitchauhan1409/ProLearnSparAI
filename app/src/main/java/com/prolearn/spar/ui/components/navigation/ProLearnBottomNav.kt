package com.prolearn.spar.ui.components.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.ui.theme.BricolageGrotesqueFamily
import com.prolearn.spar.ui.theme.ProLearnColors

enum class MainTab { Home, Arena, Profile }

@Composable
fun ProLearnBottomNav(
    selected: MainTab,
    onHome: () -> Unit,
    onArena: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        Color(0xEEF8FAF7),
                        Color(0xFFF8FAF7)
                    )
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 430.dp)
                .height(72.dp)
                .shadow(
                    28.dp,
                    RoundedCornerShape(32.dp),
                    ambientColor = Color.Black.copy(alpha = 0.14f),
                    spotColor = Color.Black.copy(alpha = 0.12f)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.98f),
                            Color(0xFFF2F5EF).copy(alpha = 0.96f)
                        )
                    )
                )
                .border(
                    1.dp,
                    Brush.linearGradient(
                        listOf(Color.White, Color(0xFFE1E8DE), Color.White.copy(alpha = 0.68f))
                    ),
                    RoundedCornerShape(32.dp)
                )
                .padding(7.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem("Home", Icons.Default.Home, selected == MainTab.Home, onHome, Modifier.weight(1f))
            NavItem("Arena", Icons.Default.EmojiEvents, selected == MainTab.Arena, onArena, Modifier.weight(1f))
            NavItem("Profile", Icons.Default.Person, selected == MainTab.Profile, onProfile, Modifier.weight(1f))
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconFill by animateColorAsState(
        if (selected) Color.White.copy(alpha = 0.18f) else Color(0xFFF0F4EE),
        label = "navIconFill"
    )
    val content by animateColorAsState(
        if (selected) Color.White else ProLearnColors.MutedDark,
        label = "navContent"
    )
    val scale by animateFloatAsState(
        if (selected) 1.03f else 1f,
        spring(dampingRatio = 0.74f),
        label = "navScale"
    )
    val shape = RoundedCornerShape(26.dp)

    Row(
        modifier = modifier
            .height(58.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                if (selected) 14.dp else 0.dp,
                shape,
                ambientColor = Color(0xFF151616).copy(alpha = 0.16f),
                spotColor = Color(0xFF151616).copy(alpha = 0.12f)
            )
            .clip(shape)
            .background(
                if (selected) {
                    Brush.linearGradient(
                        listOf(Color(0xFF151616), Color(0xFF29322D))
                    )
                } else {
                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                }
            )
            .border(
                1.dp,
                if (selected) Color.White.copy(alpha = 0.12f) else Color.Transparent,
                shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(iconFill)
                .border(
                    1.dp,
                    if (selected) Color.White.copy(alpha = 0.16f) else Color.White,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = content, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(7.dp))
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = content,
            fontFamily = BricolageGrotesqueFamily,
            maxLines = 1
        )
    }
}
