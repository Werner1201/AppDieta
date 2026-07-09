package com.romling.diettracker

import androidx.compose.runtime.Composable
import com.romling.diettracker.core.ui.theme.DietTrackerTheme
import com.romling.diettracker.feature.today.TodayScreen
import com.romling.diettracker.feature.today.TodayUiState

@Composable
fun DietTrackerApp() {
    DietTrackerTheme {
        TodayScreen(
            state = TodayUiState(
                date = "2026-07-01",
                week = 27,
                dailyKcal = 2333.0,
                dailyProtein = 114.0,
                remainingKcal = 2333,
            ),
        )
    }
}
