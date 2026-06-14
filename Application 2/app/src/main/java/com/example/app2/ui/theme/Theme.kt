package com.getticket.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary              = GreenDark,
    onPrimary            = Color.White,
    primaryContainer     = GreenSurface,
    onPrimaryContainer   = GreenDark,
    secondary            = GreenMedium,
    onSecondary          = Color.White,
    secondaryContainer   = GreenMint,
    onSecondaryContainer = GreenDark,
    background           = BackgroundLight,
    onBackground         = TextPrimary,
    surface              = SurfaceWhite,
    onSurface            = TextPrimary,
    surfaceVariant       = BackgroundLight,
    onSurfaceVariant     = TextSecondary,
    outline              = DividerColor,
    outlineVariant       = DividerColor,
    error                = BadgeRed,
)

private val DarkColorScheme = darkColorScheme(
    primary              = GreenLight,
    onPrimary            = GreenDark,
    primaryContainer     = GreenMedium,
    onPrimaryContainer   = GreenLight,
    secondary            = GreenMint,
    onSecondary          = GreenDark,
    background           = Color(0xFF121212),
    onBackground         = Color(0xFFE4E1E9),
    surface              = Color(0xFF1E1E1E),
    onSurface            = Color(0xFFE4E1E9),
    surfaceVariant       = Color(0xFF2A2A2A),
    onSurfaceVariant     = Color(0xFFB0B0B0),
    outline              = Color(0xFF444444),
    error                = BadgeRed,
)

@Composable
fun GetTicketTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography  = Typography,
        content     = content
    )
}
