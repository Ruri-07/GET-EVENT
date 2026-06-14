package com.get.events.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Admin-specific colors (GET Telco Admin brand) ─────────────────────────────
val AdminGreenDark       = Color(0xFF1B5E48)
val AdminGreenMedium     = Color(0xFF1F6B52)
val AdminGreenLight      = Color(0xFF2D7D60)
val AdminGoldPrimary     = Color(0xFFF5A623)
val AdminGoldLight       = Color(0xFFFFC85A)
val AdminBackgroundWhite = Color(0xFFF5F5F5)
val AdminSurfaceGray     = Color(0xFFEEEEEE)
val AdminInputBackground = Color(0xFFE8E8E8)
val AdminTextOnGreen     = Color(0xFFFFFFFF)
val AdminIconShieldBg    = Color(0xFF2A6E55)

// Alias for backward compat within admin screens
val GoldPrimary     get() = AdminGoldPrimary
val GoldLight       get() = AdminGoldLight
val BackgroundWhite get() = AdminBackgroundWhite
val SurfaceGray     get() = AdminSurfaceGray
val InputBackground get() = AdminInputBackground
val TextOnGreen     get() = AdminTextOnGreen
val IconShieldBg    get() = AdminIconShieldBg

// Admin typography
val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize   = 28.sp,
        lineHeight = 34.sp,
        color      = AdminTextOnGreen
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize   = 22.sp,
        lineHeight = 28.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 16.sp
    )
)

private val AdminColorScheme = lightColorScheme(
    primary         = AdminGreenDark,
    onPrimary       = AdminTextOnGreen,
    secondary       = AdminGoldPrimary,
    background      = AdminBackgroundWhite,
    surface         = AdminSurfaceGray,
    onBackground    = Color(0xFF1A1A1A),
    onSurface       = Color(0xFF1A1A1A)
)

@Composable
fun GetTelcoAdminTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AdminColorScheme,
        typography  = AppTypography,
        content     = content
    )
}
