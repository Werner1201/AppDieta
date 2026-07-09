package com.romling.diettracker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.romling.diettracker.core.ui.theme.DietTrackerTheme
import com.romling.diettracker.feature.today.TodayScreen
import com.romling.diettracker.feature.today.TodayViewModel

@Composable
fun DietTrackerApp(todayViewModel: TodayViewModel) {
    val state by todayViewModel.state.collectAsState()
    DietTrackerTheme {
        TodayScreen(state = state)
    }
}
