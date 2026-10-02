package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = Color(0xFF14171F),
    primaryContainer = Color(0xFF332600),
    onPrimaryContainer = GoldAccentLight,
    secondary = CyanAccent,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004E57),
    onSecondaryContainer = Color(0xFF82F4FF),
    tertiary = CoralAccent,
    background = DarkStudioBg,
    onBackground = TextPrimary,
    surface = DarkStudioSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkStudioSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkStudioBorder,
    outlineVariant = Color(0xFF222633)
)

@Composable
fun LuminaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
