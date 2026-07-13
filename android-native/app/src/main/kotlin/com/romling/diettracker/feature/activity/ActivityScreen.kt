package com.romling.diettracker.feature.activity

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import com.romling.diettracker.core.ui.components.AppCard
import com.romling.diettracker.core.ui.components.BottomPrimaryButton
import com.romling.diettracker.core.ui.theme.AppColors
import com.romling.diettracker.core.ui.theme.AppSpacing
import com.romling.diettracker.domain.service.ActivityCalorieCalculator
import com.romling.diettracker.R
import com.romling.diettracker.feature.today.TodayActivitySummary

data class ActivityOption(
    val name: String,
    val icon: String,
    val lightMet: Double,
    val moderateMet: Double,
    val vigorousMet: Double,
    val tracksDistance: Boolean = false,
    val tracksSteps: Boolean = false,
    val custom: Boolean = false,
)

private val activityCatalog = listOf(
    ActivityOption("Passos (manual)", "👟", 2.0, 3.0, 4.0, tracksSteps = true),
    ActivityOption("Musculação", "💪", 3.5, 4.5, 6.0),
    ActivityOption("Caminhada", "🚶", 2.5, 3.5, 5.0, true),
    ActivityOption("Ciclismo", "🚴", 4.0, 6.8, 10.0, true),
    ActivityOption("Corrida", "🏃", 6.0, 8.0, 11.0, true),
    ActivityOption("Elíptico", "🏋️", 4.0, 5.0, 8.0),
    ActivityOption("Trilha", "🥾", 4.0, 6.0, 8.0, true),
    ActivityOption("Yoga", "🧘", 2.0, 2.5, 4.0),
    ActivityOption("Natação", "🏊", 4.0, 6.0, 9.0, true),
)

@Composable
fun ActivityScreen(
    weightKg: Double,
    frequentNames: List<String> = emptyList(),
    initialActivity: TodayActivitySummary? = null,
    onSave: (ActivityOption, Double, Int, Double?, String, Int?) -> Unit,
    onClose: () -> Unit,
) {
    val initialOption = initialActivity?.let { entry ->
        activityCatalog.firstOrNull { it.name == entry.name }
            ?: ActivityOption(entry.name, entry.icon, entry.met, entry.met, entry.met, custom = true)
    }
    var selected by remember(initialActivity?.id) { mutableStateOf(initialOption) }
    var duration by remember(initialActivity?.id) { mutableStateOf(initialActivity?.durationMinutes?.toString() ?: "30") }
    var intensity by remember(initialActivity?.id) {
        val mets = initialOption?.let { listOf(it.lightMet, it.moderateMet, it.vigorousMet) }
        mutableStateOf(mets?.indexOf(initialActivity?.met)?.takeIf { it >= 0 } ?: 1)
    }
    var distance by remember(initialActivity?.id) { mutableStateOf(initialActivity?.distanceKm?.toString() ?: "") }
    var note by remember(initialActivity?.id) { mutableStateOf(initialActivity?.note ?: "") }
    var steps by remember(initialActivity?.id) { mutableStateOf(initialActivity?.steps?.toString() ?: "") }
    var customName by remember(initialActivity?.id) { mutableStateOf(initialActivity?.name ?: "") }
    var customMet by remember(initialActivity?.id) { mutableStateOf(initialActivity?.met?.toString() ?: "") }
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
            IconButton(onClick = { if (selected != null) selected = null else onClose() }) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_back), tint = AppColors.TextPrimary)
            }
            Text(
                selected?.name ?: stringResource(R.string.activity_add_title),
                modifier = Modifier.weight(1f),
                color = AppColors.TextPrimary,
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        val activity = selected
        if (activity == null) {
            val frequent = frequentNames.mapNotNull { name -> activityCatalog.firstOrNull { it.name == name } }
            if (frequent.isNotEmpty()) {
                Text(stringResource(R.string.activity_frequent), style = MaterialTheme.typography.titleLarge)
                ActivityOptionsCard(frequent, onSelect = { selected = it })
            }
            Text(stringResource(R.string.activity_all), style = MaterialTheme.typography.titleLarge)
            ActivityOptionsCard(activityCatalog.filterNot { it in frequent }, onSelect = { selected = it })
            Text(
                stringResource(R.string.activity_custom),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        customName = ""
                        customMet = ""
                        selected = ActivityOption("Atividade personalizada", "➕", 1.0, 1.0, 1.0, custom = true)
                    }
                    .padding(vertical = 12.dp),
                color = AppColors.Accent,
                style = MaterialTheme.typography.titleMedium,
            )
        } else {
            val stepCount = steps.toIntOrNull()
            val minutes = if (activity.tracksSteps && stepCount != null) {
                ceil(stepCount / 100.0).toInt()
            } else duration.toIntOrNull() ?: 0
            val met = if (activity.custom) {
                customMet.replace(',', '.').toDoubleOrNull() ?: 0.0
            } else {
                listOf(activity.lightMet, activity.moderateMet, activity.vigorousMet)[intensity]
            }
            val kcal = if (minutes > 0 && met > 0) {
                ActivityCalorieCalculator.calculate(met, weightKg, minutes).toInt()
            } else 0
            val animatedKcal by animateIntAsState(
                targetValue = kcal,
                animationSpec = tween(240, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)),
                label = "activity calories",
            )
            Text(activity.icon, modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.displayMedium, textAlign = TextAlign.Center)
            Text("$animatedKcal kcal", modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            if (activity.tracksSteps) {
                OutlinedTextField(
                    value = steps,
                    onValueChange = { steps = it.filter(Char::isDigit).take(6) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.activity_steps)) },
                    singleLine = true,
                )
                Text(stringResource(R.string.activity_steps_estimate), color = AppColors.TextSecondary)
            } else {
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it.filter(Char::isDigit).take(3) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.activity_duration_minutes)) },
                    singleLine = true,
                )
            }
            if (activity.custom) {
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it.take(60) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nome") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = customMet,
                    onValueChange = { customMet = it.filter { char -> char.isDigit() || char == ',' || char == '.' }.take(4) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("MET") },
                    singleLine = true,
                )
            } else Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    stringResource(R.string.activity_intensity_light),
                    stringResource(R.string.activity_intensity_moderate),
                    stringResource(R.string.activity_intensity_vigorous),
                ).forEachIndexed { index, label ->
                    FilterChip(
                        selected = intensity == index,
                        onClick = { intensity = index },
                        label = { Text(label) },
                    )
                }
            }
            if (activity.tracksDistance) {
                OutlinedTextField(
                    value = distance,
                    onValueChange = { distance = it.filter { char -> char.isDigit() || char == ',' || char == '.' }.take(6) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.activity_distance_optional)) },
                    singleLine = true,
                )
            }
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(500) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.activity_note_optional)) },
                minLines = 2,
            )
            Text(
                stringResource(R.string.activity_weight_estimate, weightKg.toInt()),
                color = AppColors.TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
            )
            BottomPrimaryButton(
                text = stringResource(R.string.activity_save),
                onClick = {
                    if (minutes > 0 && met > 0 && (!activity.custom || customName.isNotBlank())) {
                        onSave(
                            if (activity.custom) activity.copy(name = customName.trim()) else activity,
                            met,
                            minutes,
                            distance.replace(',', '.').toDoubleOrNull(),
                            note.trim(),
                            stepCount,
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ActivityOptionsCard(options: List<ActivityOption>, onSelect: (ActivityOption) -> Unit) {
    AppCard {
        options.forEachIndexed { index, option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(option) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(option.icon, style = MaterialTheme.typography.headlineSmall)
                Text(option.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.action_open_item, option.name),
                    tint = AppColors.TextSecondary,
                )
            }
            if (index < options.lastIndex) HorizontalDivider(color = AppColors.Line)
        }
    }
}
