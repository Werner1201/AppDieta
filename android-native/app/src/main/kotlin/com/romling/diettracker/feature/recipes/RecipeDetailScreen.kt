package com.romling.diettracker.feature.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.romling.diettracker.core.ui.components.AppCard
import com.romling.diettracker.core.ui.components.BottomPrimaryButton
import com.romling.diettracker.core.ui.theme.AppColors
import com.romling.diettracker.core.ui.theme.AppSpacing
import com.romling.diettracker.data.local.entity.RecipeEntity
import com.romling.diettracker.data.local.entity.RecipeIngredientEntity
import com.romling.diettracker.feature.meal.FoodSearchItem

private sealed interface AddStep {
    data object Search : AddStep
    data class Grams(val food: FoodSearchItem) : AddStep
}

@Composable
fun RecipeDetailScreen(
    recipe: RecipeEntity,
    ingredients: List<RecipeIngredientEntity>,
    foodResults: List<FoodSearchItem>,
    onSearchFoods: (String) -> Unit,
    onClearFoodSearch: () -> Unit,
    onAddIngredient: (FoodSearchItem, Double) -> Unit,
    onRemoveIngredient: (Long) -> Unit,
    onAddToDiary: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var addStep by remember { mutableStateOf<AddStep?>(null) }
    var foodQuery by remember { mutableStateOf("") }
    var gramsText by remember { mutableStateOf("") }
    var showMealPicker by remember { mutableStateOf(false) }

    val totalKcal = ingredients.sumOf { it.kcal }
    val totalCarbs = ingredients.sumOf { it.carbs }
    val totalProtein = ingredients.sumOf { it.protein }
    val totalFat = ingredients.sumOf { it.fat }

    if (showMealPicker) {
        AlertDialog(
            onDismissRequest = { showMealPicker = false },
            title = { Text("Adicionar ao diário") },
            text = {
                Column {
                    listOf(
                        "breakfast" to "Café da manhã",
                        "lunch" to "Almoço",
                        "dinner" to "Jantar",
                        "snack" to "Lanche",
                    ).forEach { (key, label) ->
                        TextButton(
                            onClick = { showMealPicker = false; onAddToDiary(key) },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(label) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMealPicker = false }) { Text("Cancelar") }
            },
        )
    }

    when (val step = addStep) {
        AddStep.Search -> AlertDialog(
            onDismissRequest = {
                addStep = null
                foodQuery = ""
                onClearFoodSearch()
            },
            title = { Text("Buscar alimento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = foodQuery,
                        onValueChange = { foodQuery = it; onSearchFoods(it) },
                        label = { Text("Nome do alimento") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Column(
                        modifier = Modifier
                            .heightIn(max = 240.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        foodResults.forEachIndexed { index, food ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        gramsText = ""
                                        addStep = AddStep.Grams(food)
                                        foodQuery = ""
                                        onClearFoodSearch()
                                    }
                                    .padding(vertical = 10.dp),
                            ) {
                                Text(food.name, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextPrimary)
                                Text(
                                    "%.0f kcal / %s".format(food.kcal, food.serving),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.TextSecondary,
                                )
                            }
                            if (index < foodResults.lastIndex) {
                                HorizontalDivider(color = AppColors.Line, thickness = 1.dp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = {
                    addStep = null
                    foodQuery = ""
                    onClearFoodSearch()
                }) { Text("Cancelar") }
            },
        )
        is AddStep.Grams -> AlertDialog(
            onDismissRequest = { addStep = null; gramsText = "" },
            title = { Text(step.food.name, maxLines = 2, overflow = TextOverflow.Ellipsis) },
            text = {
                OutlinedTextField(
                    value = gramsText,
                    onValueChange = { gramsText = it },
                    label = { Text("Gramas") },
                    suffix = { Text("g") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val g = gramsText.replace(',', '.').toDoubleOrNull()
                    if (g != null && g > 0) {
                        onAddIngredient(step.food, g)
                        addStep = null
                        gramsText = ""
                    }
                }) { Text("Adicionar") }
            },
            dismissButton = {
                TextButton(onClick = { addStep = null; gramsText = "" }) { Text("Cancelar") }
            },
        )
        null -> {}
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.Background)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onClose) {
                    Text("←", style = MaterialTheme.typography.headlineSmall, color = AppColors.TextPrimary)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = AppColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.ScreenHorizontal, vertical = AppSpacing.SectionGap),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.SectionGap),
            ) {
                if (recipe.description.isNotBlank()) {
                    Text(
                        text = recipe.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextSecondary,
                    )
                }

                if (ingredients.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Adicione ingredientes para calcular os macros.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppColors.TextSecondary,
                        )
                    }
                } else {
                    AppCard {
                        Column {
                            ingredients.forEachIndexed { index, ing ->
                                IngredientRow(ingredient = ing, onRemove = { onRemoveIngredient(ing.id) })
                                if (index < ingredients.lastIndex) {
                                    HorizontalDivider(color = AppColors.Line, thickness = 1.dp)
                                }
                            }
                        }
                    }
                    AppCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            MacroCell("Kcal", "%.0f".format(totalKcal))
                            MacroCell("Carbs", "%.1fg".format(totalCarbs))
                            MacroCell("Prot.", "%.1fg".format(totalProtein))
                            MacroCell("Gord.", "%.1fg".format(totalFat))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }

            Column(
                modifier = Modifier.padding(horizontal = AppSpacing.ScreenHorizontal, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (ingredients.isNotEmpty()) {
                    TextButton(
                        onClick = { addStep = AddStep.Search },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("+ Ingrediente") }
                    BottomPrimaryButton(text = "Adicionar ao diário", onClick = { showMealPicker = true })
                } else {
                    BottomPrimaryButton(text = "+ Ingrediente", onClick = { addStep = AddStep.Search })
                }
            }
        }
    }
}

@Composable
private fun IngredientRow(ingredient: RecipeIngredientEntity, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = ingredient.foodNameSnapshot,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "%.0fg · %.0f kcal".format(ingredient.grams, ingredient.kcal),
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary,
            )
        }
        IconButton(onClick = onRemove) {
            Text("✕", style = MaterialTheme.typography.bodyLarge, color = AppColors.Remove)
        }
    }
}

@Composable
private fun MacroCell(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = AppColors.TextSecondary)
    }
}
