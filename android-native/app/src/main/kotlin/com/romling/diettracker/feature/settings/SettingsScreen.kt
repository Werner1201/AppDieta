package com.romling.diettracker.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import java.net.URI
import com.romling.diettracker.data.repository.GoalSettings
import com.romling.diettracker.core.ui.components.AppCard
import com.romling.diettracker.core.ui.theme.AppColors
import com.romling.diettracker.core.ui.theme.AppSpacing
import com.romling.diettracker.feature.today.TodayUiState

@Composable
fun SettingsScreen(
    state: TodayUiState,
    onSaveGoals: (GoalSettings) -> Unit,
    onOpenCustomFoods: () -> Unit = {},
    onExportDiary: () -> Unit = {},
    onImportDiary: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var kcal by remember { mutableStateOf(state.dailyKcal.toInt().toString()) }
    var carbs by remember { mutableStateOf(state.dailyCarbs.toInt().toString()) }
    var protein by remember { mutableStateOf(state.dailyProtein.toInt().toString()) }
    var fat by remember { mutableStateOf(state.dailyFat.toInt().toString()) }
    var water by remember { mutableStateOf(state.water.goalMl.toString()) }
    var weightGoal by remember { mutableStateOf("%.1f".format(state.weight.goalKg)) }
    var chatGptUrl by remember { mutableStateOf(state.chatGptUrl) }
    var chatGptPrompt by remember { mutableStateOf(state.chatGptPrompt) }

    LaunchedEffect(state.dailyKcal, state.dailyCarbs, state.dailyProtein, state.dailyFat, state.water.goalMl, state.weight.goalKg, state.chatGptUrl, state.chatGptPrompt) {
        kcal = state.dailyKcal.toInt().toString()
        carbs = state.dailyCarbs.toInt().toString()
        protein = state.dailyProtein.toInt().toString()
        fat = state.dailyFat.toInt().toString()
        water = state.water.goalMl.toString()
        weightGoal = "%.1f".format(state.weight.goalKg)
        chatGptUrl = state.chatGptUrl
        chatGptPrompt = state.chatGptPrompt
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.ScreenHorizontal, vertical = 34.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(text = "Configurações", style = MaterialTheme.typography.headlineMedium)
        AppCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenCustomFoods),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Meus alimentos", style = MaterialTheme.typography.bodyLarge)
                Text(text = "→", style = MaterialTheme.typography.bodyLarge, color = AppColors.Accent)
            }
        }
        AppCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onImportDiary),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Importar diário (JSON)", style = MaterialTheme.typography.bodyLarge)
                Text(text = "↓", style = MaterialTheme.typography.bodyLarge, color = AppColors.Accent)
            }
        }
        AppCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onExportDiary),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Exportar diário (JSON)", style = MaterialTheme.typography.bodyLarge)
                Text(text = "↑", style = MaterialTheme.typography.bodyLarge, color = AppColors.Accent)
            }
        }
        AppCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GoalInput("Calorias", kcal, "kcal") { kcal = it }
                GoalInput("Carboidratos", carbs, "g") { carbs = it }
                GoalInput("Proteína", protein, "g") { protein = it }
                GoalInput("Gordura", fat, "g") { fat = it }
                GoalInput("Água", water, "ml") { water = it }
                GoalInput("Peso alvo", weightGoal, "kg") { weightGoal = it }
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = chatGptUrl,
                    onValueChange = { chatGptUrl = it },
                    label = { Text("URL do GPT") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    value = chatGptPrompt,
                    onValueChange = { chatGptPrompt = it },
                    label = { Text("Prompt do ChatGPT") },
                )
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
                                chatGptUrl = validatedChatGptUrl(chatGptUrl, state.chatGptUrl),
                                chatGptPrompt = chatGptPrompt.trim().ifBlank { state.chatGptPrompt },
                            ),
                        )
                    },
                ) {
                    Text("Salvar configurações")
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

internal fun validatedChatGptUrl(value: String, fallback: String): String {
    val candidate = value.trim()
    val uri = runCatching { URI(candidate) }.getOrNull()
    return candidate.takeIf { uri?.scheme in setOf("http", "https") && !uri?.host.isNullOrBlank() } ?: fallback
}
