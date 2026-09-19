package com.example.flydigicooler.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Immutable
data class CoolerColors(
    val background: Color,
    val glowColor1: Color,
    val glowColor2: Color,
    val glassBackground: Color,
    val glassBorderBrush: Brush,
    val textPrimary: Color,
    val textSecondary: Color,
    val accentPrimary: Color,
    val lowTempColor: Color,
    val hubBackground: Color,
    val hubBorderBrush: Brush,
    val dialTrackColor: Color,
    val dialTickMajor: Color,
    val dialTickMinor: Color,
    val dialButtonBg: Color,
    val dialButtonBorder: Color,
    val switchTrackUnchecked: Color
)

val DarkCoolerColors = CoolerColors(
    background = Color(0xFF07090E),
    glowColor1 = Color(0xFF00E5FF).copy(alpha = 0.35f),
    glowColor2 = Color(0xFF7000FF).copy(alpha = 0.28f),
    glassBackground = Color.White.copy(alpha = 0.06f),
    glassBorderBrush = Brush.verticalGradient(
        listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.04f))
    ),
    textPrimary = Color.White,
    textSecondary = Color(0xFF8E99A8),
    accentPrimary = Color(0xFF00E5FF),
    lowTempColor = Color(0xFF00E5FF),
    hubBackground = Color(0xFF10141E).copy(alpha = 0.92f),
    hubBorderBrush = Brush.verticalGradient(
        listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.06f))
    ),
    dialTrackColor = Color.White.copy(alpha = 0.08f),
    dialTickMajor = Color(0xFF00E5FF).copy(alpha = 0.85f),
    dialTickMinor = Color.White.copy(alpha = 0.20f),
    dialButtonBg = Color(0xFF161B26).copy(alpha = 0.85f),
    dialButtonBorder = Color.White.copy(alpha = 0.15f),
    switchTrackUnchecked = Color.White.copy(alpha = 0.15f)
)

val LightCoolerColors = CoolerColors(
    background = Color(0xFFF3F5FA),
    glowColor1 = Color(0xFFBCE8FF).copy(alpha = 0.65f),
    glowColor2 = Color(0xFFE4DDFE).copy(alpha = 0.60f),
    glassBackground = Color.White.copy(alpha = 0.78f),
    glassBorderBrush = Brush.verticalGradient(
        listOf(Color.White, Color.White.copy(alpha = 0.45f))
    ),
    textPrimary = Color(0xFF181B24),
    textSecondary = Color(0xFF7B8698),
    accentPrimary = Color(0xFF007AFF),
    lowTempColor = Color(0xFF007AFF),
    hubBackground = Color.White,
    hubBorderBrush = Brush.verticalGradient(
        listOf(Color.White, Color(0xFFE6ECF5))
    ),
    dialTrackColor = Color.Black.copy(alpha = 0.05f),
    dialTickMajor = Color(0xFF007AFF),
    dialTickMinor = Color(0xFF9EAAB9).copy(alpha = 0.40f),
    dialButtonBg = Color.White.copy(alpha = 0.95f),
    dialButtonBorder = Color.White,
    switchTrackUnchecked = Color(0xFFE2E6EE)
)

val LocalCoolerColors = staticCompositionLocalOf { DarkCoolerColors }

object CoolerTheme {
    val colors: CoolerColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCoolerColors.current
}

@Composable
fun CoolerAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkCoolerColors else LightCoolerColors
    CompositionLocalProvider(LocalCoolerColors provides colors) {
        content()
    }
}