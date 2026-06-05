package com.prolearn.spar.ui.components.spar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.domain.model.Message
import com.prolearn.spar.ui.theme.ProLearnColors

@Composable
fun MessageBubble(message: Message) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isUser) 64.dp else 8.dp,
                end = if (isUser) 8.dp else 64.dp,
                top = 4.dp,
                bottom = 4.dp
            ),
        horizontalArrangement = if (isUser) {
            androidx.compose.foundation.layout.Arrangement.End
        } else {
            androidx.compose.foundation.layout.Arrangement.Start
        }
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(ProLearnColors.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "AI",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProLearnColors.White
                )
            }
            Spacer(Modifier.width(8.dp))
        }
        val bg = if (isUser) ProLearnColors.UserBubble else ProLearnColors.AIBubble
        val textColor = if (isUser) ProLearnColors.White else ProLearnColors.Black
        val shape = if (isUser) {
            RoundedCornerShape(12.dp, 4.dp, 12.dp, 12.dp)
        } else {
            RoundedCornerShape(4.dp, 12.dp, 12.dp, 12.dp)
        }

        Box(
            modifier = Modifier
                .background(bg, shape)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Column {
                if (message.isHint) {
                    Text(
                        "HINT",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ProLearnColors.Muted,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = textColor
                )
            }
        }
    }
}
