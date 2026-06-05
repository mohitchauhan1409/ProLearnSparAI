package com.prolearn.spar.ui.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.prolearn.spar.ui.theme.ProLearnColors
import com.prolearn.spar.ui.theme.ProLearnShapes

@Composable
fun ProLearnCard(
    modifier: Modifier = Modifier,
    borderColor: androidx.compose.ui.graphics.Color = ProLearnColors.Border,
    backgroundColor: androidx.compose.ui.graphics.Color = ProLearnColors.White,
    padding: Int = 16,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(ProLearnShapes.md)
            .background(backgroundColor)
            .border(1.dp, borderColor, ProLearnShapes.md)
            .padding(padding.dp)
    ) {
        content()
    }
}
