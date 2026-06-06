package com.prolearn.spar.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun ProLearnTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            background = ProLearnColors.White,
            surface = ProLearnColors.Surface,
            primary = ProLearnColors.Black,
            onPrimary = ProLearnColors.White,
            onBackground = ProLearnColors.Black,
            onSurface = ProLearnColors.Black,
            outline = ProLearnColors.Border,
            error = ProLearnColors.Error
        ),
        typography = ProLearnTypography,
        shapes = Shapes(
            small = ProLearnShapes.sm,
            medium = ProLearnShapes.md,
            large = ProLearnShapes.lg
        ),
        content = {
            ProvideTextStyle(
                value = ProLearnTypography.bodyLarge,
                content = content
            )
        }
    )
}
