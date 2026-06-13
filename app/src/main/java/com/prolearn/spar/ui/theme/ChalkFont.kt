package com.prolearn.spar.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font as GoogleFontFace
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.prolearn.spar.R

/** Downloadable-fonts provider (Play Services) — certs already configured in res/values/font_certs.xml. */
private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val Caveat = GoogleFont("Caveat")

/**
 * Handwritten "chalk" font for the video-lesson chalkboard.
 * Falls back to the bundled Bricolage faces if the download is unavailable
 * (offline / no Play Services), so text always renders.
 */
val ChalkFontFamily = FontFamily(
    GoogleFontFace(googleFont = Caveat, fontProvider = googleFontProvider, weight = FontWeight.Bold),
    GoogleFontFace(googleFont = Caveat, fontProvider = googleFontProvider, weight = FontWeight.Medium),
    GoogleFontFace(googleFont = Caveat, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    // Bundled fallbacks (same weights) if the downloadable font can't load.
    Font(R.font.bricolagegrotesque_bold, FontWeight.Bold),
    Font(R.font.bricolagegrotesque_medium, FontWeight.Medium),
    Font(R.font.bricolagegrotesque_regular, FontWeight.Normal),
)
