package com.romling.diettracker.feature.activity

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.romling.diettracker.core.ui.components.AppCard
import com.romling.diettracker.core.ui.components.BottomPrimaryButton
import com.romling.diettracker.core.ui.theme.AppColors
import com.romling.diettracker.core.ui.theme.AppSpacing
import com.romling.diettracker.domain.service.ActivityCalorieCalculator

data class ActivityOption(val name: String, val icon: String, val met: Double)

private val activityCatalog = listOf(
    ActivityOption("Musculação", "💪", 4.5),
    ActivityOption("Caminhada", "🚶", 3.5),
    ActivityOption("Ciclismo", "🚴", 6.8),
    ActivityOption("Corrida", "🏃", 8.0),
    ActivityOption("Elíptico", "🏋️", 5.0),
    ActivityOption("Trilha", "🥾", 6.0),
    ActivityOption("Yoga", "🧘", 2.5),
    ActivityOption("Natação", "🏊", 6.0),
)

@Composable
fun ActivityScreen(
    weightKg: Double,
    onSave: (ActivityOption, Int) -> Unit,
    onClose: () -> Unit,
) {
    var selected by remember { mutableStateOf<ActivityOption?>(null) }
    var duration by remember { mutableStateOf("30") }
    BackHandler {
        if (selected != null) selected = null else onClose()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.ScreenHorizontal, vertical = 34.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Text(
                "×",
                modifier = Modifier.clickable { if (selected != null) selected = null else onClose() },
                color = AppColors.TextPrimary,
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                selected?.name ?: "Adicionar atividade",
                color = AppColors.TextPrimary,
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        val activity = selected
        if (activity == null) {
            AppCard {
                activityCatalog.forEachIndexed { index, option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = option }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(option.icon, style = MaterialTheme.typography.headlineSmall)
                        Text(option.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                        Text("→", style = MaterialTheme.typography.titleMedium)
                    }
                    if (index < activityCatalog.lastIndex) HorizontalDivider(color = AppColors.Line)
                }
            }
        } else {
            val minutes = duration.toIntOrNull() ?: 0
            val kcal = if (minutes > 0) ActivityCalorieCalculator.calculate(activity.met, weightKg, minutes).toInt() else 0
            Text(activity.icon, modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.displayMedium, textAlign = TextAlign.Center)
            Text("$kcal kcal", modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it.filter(Char::isDigit).take(3) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Duração (min)") },
                singleLine = true,
            )
            Text("Estimativa com ${weightKg.toInt()} kg", color = AppColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
            BottomPrimaryButton(
                text = "Salvar",
                onClick = { onSave(activity, minutes) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
