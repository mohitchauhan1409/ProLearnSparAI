package com.prolearn.spar.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.prolearn.spar.R

val BricolageGrotesqueFamily = FontFamily(
    Font(R.font.bricolagegrotesque_extralight, FontWeight.ExtraLight),
    Font(R.font.bricolagegrotesque_light, FontWeight.Light),
    Font(R.font.bricolagegrotesque_regular, FontWeight.Normal),
    Font(R.font.bricolagegrotesque_medium, FontWeight.Medium),
    Font(R.font.bricolagegrotesque_semibold, FontWeight.SemiBold),
    Font(R.font.bricolagegrotesque_bold, FontWeight.Bold),
    Font(R.font.bricolagegrotesque_extrabold, FontWeight.ExtraBold),
)

val ProLearnTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = BricolageGrotesqueFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-1).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = BricolageGrotesqueFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontFamily = BricolageGrotesqueFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = BricolageGrotesqueFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = BricolageGrotesqueFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = BricolageGrotesqueFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp
    )
)
