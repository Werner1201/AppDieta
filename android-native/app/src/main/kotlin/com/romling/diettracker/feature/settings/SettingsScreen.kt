package com.romling.diettracker.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.romling.diettracker.core.ui.components.AppCard
import com.romling.diettracker.core.ui.theme.AppColors
import com.romling.diettracker.core.ui.theme.AppSpacing
import com.romling.diettracker.feature.today.TodayUiState

@Composable
fun SettingsScreen(state: TodayUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(horizontal = AppSpacing.ScreenHorizontal, vertical = 34.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(text = "Configurações", style = MaterialTheme.typography.headlineMedium)
        AppCard {
            GoalRow("Calorias", "${state.dailyKcal.toInt()} kcal")
            GoalRow("Carboidratos", "284 g")
            GoalRow("Proteína", "${state.dailyProtein.toInt()} g")
            GoalRow("Gordura", "75 g")
            GoalRow("Água", "${state.water.goalMl} ml")
            GoalRow("Peso alvo", "%.1f kg".format(state.weight.goalKg))
        }
    }
}

@Composable
private fun GoalRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.titleMedium)
    }
}
