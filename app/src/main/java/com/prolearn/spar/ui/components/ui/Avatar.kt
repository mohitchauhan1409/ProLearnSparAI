package com.prolearn.spar.ui.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.ui.theme.BricolageGrotesqueFamily
import com.prolearn.spar.ui.theme.ProLearnColors

@Composable
fun Avatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    backgroundColor: Color = ProLearnColors.Black,
    textColor: Color = ProLearnColors.White,
    fontSize: Int = 14
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = BricolageGrotesqueFamily,
            color = textColor
        )
    }
}

// Alias for backward compatibility
@Composable
fun ProLearnAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    backgroundColor: Color = ProLearnColors.Black,
    textColor: Color = ProLearnColors.White,
    fontSize: Int = 14
) = Avatar(initials, modifier, size, backgroundColor, textColor, fontSize)
