package com.romling.diettracker.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.romling.diettracker.data.repository.GoalSettings
import com.romling.diettracker.core.ui.components.AppCard
import com.romling.diettracker.core.ui.theme.AppColors
import com.romling.diettracker.core.ui.theme.AppSpacing
import com.romling.diettracker.feature.today.TodayUiState

@Composable
fun SettingsScreen(
    state: TodayUiState,
    onSaveGoals: (GoalSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    var kcal by remember { mutableStateOf(state.dailyKcal.toInt().toString()) }
    var carbs by remember { mutableStateOf(state.dailyCarbs.toInt().toString()) }
    var protein by remember { mutableStateOf(state.dailyProtein.toInt().toString()) }
    var fat by remember { mutableStateOf(state.dailyFat.toInt().toString()) }
    var water by remember { mutableStateOf(state.water.goalMl.toString()) }
    var weightGoal by remember { mutableStateOf("%.1f".format(state.weight.goalKg)) }

    LaunchedEffect(state.dailyKcal, state.dailyCarbs, state.dailyProtein, state.dailyFat, state.water.goalMl, state.weight.goalKg) {
        kcal = state.dailyKcal.toInt().toString()
        carbs = state.dailyCarbs.toInt().toString()
        protein = state.dailyProtein.toInt().toString()
        fat = state.dailyFat.toInt().toString()
        water = state.water.goalMl.toString()
        weightGoal = "%.1f".format(state.weight.goalKg)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(horizontal = AppSpacing.ScreenHorizontal, vertical = 34.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(text = "Configurações", style = MaterialTheme.typography.headlineMedium)
        AppCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GoalInput("Calorias", kcal, "kcal") { kcal = it }
                GoalInput("Carboidratos", carbs, "g") { carbs = it }
                GoalInput("Proteína", protein, "g") { protein = it }
                GoalInput("Gordura", fat, "g") { fat = it }
                GoalInput("Água", water, "ml") { water = it }
                GoalInput("Peso alvo", weightGoal, "kg") { weightGoal = it }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onSaveGoals(
                            GoalSettings(
                                dailyKcal = kcal.toGoalDouble(state.dailyKcal),
                                dailyCarbs = carbs.toGoalDouble(state.dailyCarbs),
                                dailyProtein = protein.toGoalDouble(state.dailyProtein),
                                dailyFat = fat.toGoalDouble(state.dailyFat),
                                dailyWaterMl = water.toIntOrNull() ?: state.water.goalMl,
                                defaultWeightKg = state.weight.currentKg,
                                weightGoalKg = weightGoal.toGoalDouble(state.weight.goalKg),
                            ),
                        )
                    },
                ) {
                    Text("Salvar metas")
                }
            }
        }
    }
}

@Composable
private fun GoalInput(label: String, value: String, suffix: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        suffix = { Text(suffix) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    )
}

private fun String.toGoalDouble(fallback: Double): Double =
    replace(',', '.').toDoubleOrNull() ?: fallback
