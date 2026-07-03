package com.romling.diettracker.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DietColorScheme = darkColorScheme(
    background = AppColors.Background,
    surface = AppColors.Panel,
    surfaceVariant = AppColors.Panel,
    primary = AppColors.Accent,
    secondary = AppColors.Green,
    outline = AppColors.Line,
    onBackground = AppColors.TextPrimary,
    onSurface = AppColors.TextPrimary,
    onSurfaceVariant = AppColors.TextSecondary,
    onPrimary = AppColors.Background,
    error = AppColors.Remove,
)

@Composable
fun DietTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DietColorScheme,
        typography = AppTypography,
        content = content,
    )
}
