package com.romling.diettracker.feature.meal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.romling.diettracker.core.ui.components.AppCard
import com.romling.diettracker.core.ui.components.BottomPrimaryButton
import com.romling.diettracker.core.ui.components.ConfirmDeleteDialog
import com.romling.diettracker.core.ui.theme.AppColors
import com.romling.diettracker.core.ui.theme.AppSpacing
import com.romling.diettracker.feature.today.TodayEntrySummary
import com.romling.diettracker.feature.today.TodayMealSummary

@Composable
fun MealDetailScreen(
    meal: TodayMealSummary,
    entries: List<TodayEntrySummary>,
    onRemoveEntry: (Long) -> Unit,
    onEditEntry: (Long, Double) -> Unit = { _, _ -> },
    onAddMore: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mealEntries = entries.filter { it.mealType == meal.key }
    val totalKcal = mealEntries.sumOf { it.kcal }
    val totalCarbs = mealEntries.sumOf { it.carbs }
    val totalProtein = mealEntries.sumOf { it.protein }
    val totalFat = mealEntries.sumOf { it.fat }
    var editingEntry by remember { mutableStateOf<TodayEntrySummary?>(null) }
    var editGrams by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<TodayEntrySummary?>(null) }

    pendingDelete?.let { entry ->
        ConfirmDeleteDialog(
            itemName = entry.name,
            onConfirm = {
                onRemoveEntry(entry.id)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }

    editingEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { editingEntry = null },
            title = { Text(entry.name, maxLines = 2, overflow = TextOverflow.Ellipsis) },
            text = {
                OutlinedTextField(
                    value = editGrams,
                    onValueChange = { editGrams = it },
                    label = { Text("Gramas") },
                    suffix = { Text("g") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val g = editGrams.replace(',', '.').toDoubleOrNull()
                    if (g != null && g > 0) {
                        onEditEntry(entry.id, g)
                        editingEntry = null
                    }
                }) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { editingEntry = null }) { Text("Cancelar") }
            },
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            MealDetailHeader(meal = meal, onClose = onClose)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.ScreenHorizontal, vertical = AppSpacing.SectionGap),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.SectionGap),
            ) {
                MealHeroCard(meal = meal, totalKcal = totalKcal, totalCarbs = totalCarbs, totalProtein = totalProtein, totalFat = totalFat)

                if (mealEntries.isNotEmpty()) {
                    AppCard {
                        Column {
                            mealEntries.forEachIndexed { index, entry ->
                                MealEntryRow(
                                    entry = entry,
                                    onRemove = { pendingDelete = entry },
                                    onEdit = {
                                        editGrams = entry.gramsTotal.toInt().toString()
                                        editingEntry = entry
                                    },
                                )
                                if (index < mealEntries.lastIndex) {
                                    HorizontalDivider(color = AppColors.Line, thickness = 1.dp)
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Nenhum alimento registrado",
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppColors.TextSecondary,
                        )
                    }
                }
            }

            Box(modifier = Modifier.padding(horizontal = AppSpacing.ScreenHorizontal, vertical = 12.dp)) {
                BottomPrimaryButton(text = "Adicionar mais", onClick = onAddMore)
            }
        }
    }
}

@Composable
private fun MealDetailHeader(meal: TodayMealSummary, onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Background)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose) {
            Text(text = "←", style = MaterialTheme.typography.headlineSmall, color = AppColors.TextPrimary)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = meal.label,
            style = MaterialTheme.typography.headlineSmall,
            color = AppColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MealHeroCard(
    meal: TodayMealSummary,
    totalKcal: Double,
    totalCarbs: Double,
    totalProtein: Double,
    totalFat: Double,
) {
    AppCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = meal.icon, style = MaterialTheme.typography.displayMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                MacroStatCell(label = "Kcal", value = "%.0f".format(totalKcal), goal = meal.goalKcal.toString())
                MacroStatCell(label = "Carbs", value = "%.1fg".format(totalCarbs))
                MacroStatCell(label = "Proteína", value = "%.1fg".format(totalProtein))
                MacroStatCell(label = "Gordura", value = "%.1fg".format(totalFat))
            }
        }
    }
}

@Composable
private fun MacroStatCell(label: String, value: String, goal: String? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        if (goal != null) {
            Text(
                text = "/ $goal",
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.Accent,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MealEntryRow(entry: TodayEntrySummary, onRemove: () -> Unit, onEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = entry.name,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "%.0fg · %.0f kcal".format(entry.gramsTotal, entry.kcal),
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary,
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        IconButton(onClick = onEdit) {
            Text(text = "✎", style = MaterialTheme.typography.bodyLarge, color = AppColors.Accent)
        }
        IconButton(onClick = onRemove) {
            Text(text = "✕", style = MaterialTheme.typography.bodyLarge, color = AppColors.Remove)
        }
    }
}
