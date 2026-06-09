package com.prolearn.spar.ui.screens.arena

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AnswerOption(option: String, selected: String?, answer: String, onClick: () -> Unit) {
    val isSelected = selected == option
    val isCorrect = selected != null && option == answer
    val color = when {
        isCorrect -> Moss
        isSelected -> Coral
        else -> Color.White
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected == null) Color.White.copy(alpha = 0.78f) else color.copy(alpha = 0.14f))
            .border(1.dp, if (isCorrect || isSelected) color else SoftBorder, RoundedCornerShape(18.dp))
            .clickable(enabled = selected == null, onClick = onClick)
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (selected == null) Paper else color.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text("", color = Ink, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.width(10.dp))
        Text(option, Modifier.weight(1f), color = Ink, fontWeight = FontWeight.SemiBold, lineHeight = 19.sp)
        AnimatedVisibility(selected != null && (isCorrect || isSelected)) {
            Icon(if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close, null, tint = color, modifier = Modifier.size(19.dp))
        }
    }
}
