package com.romling.diettracker.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontSize = 36.sp,
        fontWeight = FontWeight.Black,
        color = AppColors.TextPrimary,
    ),
    headlineMedium = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Black,
        color = AppColors.TextPrimary,
    ),
    titleLarge = TextStyle(
        fontSize = 26.sp,
        fontWeight = FontWeight.Black,
        color = AppColors.TextPrimary,
    ),
    bodyLarge = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        color = AppColors.TextPrimary,
    ),
    bodyMedium = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        color = AppColors.TextSecondary,
    ),
    labelLarge = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Black,
        color = AppColors.TextPrimary,
    ),
)
