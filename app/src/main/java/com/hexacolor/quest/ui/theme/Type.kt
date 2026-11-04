package com.hexacolor.quest.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val DisplayFontFamily = FontFamily.SansSerif
val MonoFontFamily = FontFamily.Monospace

val HexaTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = 30.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.3).sp
    ),
    titleLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),
    titleSmall = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    bodySmall = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    ),
    labelMedium = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.4.sp
    )
)
