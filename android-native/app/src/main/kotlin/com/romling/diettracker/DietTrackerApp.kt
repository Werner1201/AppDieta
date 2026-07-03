package com.romling.diettracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.romling.diettracker.core.ui.theme.AppColors
import com.romling.diettracker.core.ui.theme.AppSpacing
import com.romling.diettracker.core.ui.theme.AppTypography
import com.romling.diettracker.core.ui.theme.DietTrackerTheme

@Composable
fun DietTrackerApp() {
    DietTrackerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(AppSpacing.ScreenHorizontal),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Dieta Local",
                style = AppTypography.headlineLarge,
            )
        }
    }
}
