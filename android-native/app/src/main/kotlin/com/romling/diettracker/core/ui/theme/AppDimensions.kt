package com.romling.diettracker.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AppDimensions(
    val summaryRingBox: Dp = 80.dp,
    val summaryRingCanvas: Dp = 64.dp,
    val summaryRingStroke: Dp = 8.dp,
    val mealIconSize: Dp = 44.dp,
    val mealActionSize: Dp = 40.dp,
    val mealRowHeight: Dp = 80.dp,
    val mealRowSpacing: Dp = 12.dp,
)

@Composable
fun rememberAppDimensions(): AppDimensions {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    return remember(screenWidthDp) {
        when {
            screenWidthDp >= 480 -> AppDimensions(
                summaryRingBox = 120.dp,
                summaryRingCanvas = 104.dp,
                summaryRingStroke = 12.dp,
                mealIconSize = 70.dp,
                mealActionSize = 58.dp,
                mealRowHeight = 112.dp,
                mealRowSpacing = 18.dp,
            )
            screenWidthDp >= 360 -> AppDimensions(
                summaryRingBox = 100.dp,
                summaryRingCanvas = 84.dp,
                summaryRingStroke = 10.dp,
                mealIconSize = 56.dp,
                mealActionSize = 48.dp,
                mealRowHeight = 96.dp,
                mealRowSpacing = 14.dp,
            )
            else -> AppDimensions()
        }
    }
}

val LocalAppDimensions = staticCompositionLocalOf { AppDimensions() }
