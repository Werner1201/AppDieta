package com.romling.diettracker.feature.meal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.romling.diettracker.core.ui.components.AppCard
import com.romling.diettracker.core.ui.components.BottomPrimaryButton
import com.romling.diettracker.core.ui.theme.AppColors
import com.romling.diettracker.core.ui.theme.AppSpacing

data class CustomFoodInput(
    val name: String,
    val category: String,
    val aliases: String,
    val kcal100g: Double,
    val carbs100g: Double,
    val protein100g: Double,
    val fat100g: Double,
    val fiber100g: Double,
    val sugar100g: Double,
    val sodiumMg100g: Double,
    val defaultUnit: String,
    val gramsPerDefaultUnit: Double,
    val source: String,
)

@Composable
fun CreateFoodScreen(
    onClose: () -> Unit,
    onSave: (CustomFoodInput) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var aliases by remember { mutableStateOf("") }
    var kcal by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var fiber by remember { mutableStateOf("") }
    var sugar by remember { mutableStateOf("") }
    var sodium by remember { mutableStateOf("") }
    var defaultUnit by remember { mutableStateOf("100 g") }
    var gramsPerUnit by remember { mutableStateOf("100") }
    var source by remember { mutableStateOf("Cadastro manual") }

    val optionalNumbers = listOf(carbs, protein, fat, fiber, sugar, sodium)
    val canSave = name.isNotBlank() &&
        category.isNotBlank() &&
        defaultUnit.isNotBlank() &&
        kcal.toNonNegativeDouble() != null &&
        gramsPerUnit.toPositiveDouble() != null &&
        optionalNumbers.all { it.isBlank() || it.toNonNegativeDouble() != null }

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
                    Text(text = "×", style = MaterialTheme.typography.headlineSmall, color = AppColors.TextPrimary)
                }
                Text(
                    text = "Criar alimento",
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
                AppCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FoodTextField(label = "Nome", value = name, onValueChange = { name = it }, keyboardType = KeyboardType.Text)
                        FoodTextField(label = "Categoria", value = category, onValueChange = { category = it }, keyboardType = KeyboardType.Text)
                        FoodTextField(label = "Aliases", value = aliases, onValueChange = { aliases = it }, keyboardType = KeyboardType.Text)
                    }
                }
                AppCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FoodTextField(label = "Calorias por 100 g", value = kcal, onValueChange = { kcal = it }, suffix = "kcal")
                        FoodTextField(label = "Carboidratos por 100 g", value = carbs, onValueChange = { carbs = it }, suffix = "g")
                        FoodTextField(label = "Proteína por 100 g", value = protein, onValueChange = { protein = it }, suffix = "g")
                        FoodTextField(label = "Gordura por 100 g", value = fat, onValueChange = { fat = it }, suffix = "g")
                        FoodTextField(label = "Fibras por 100 g", value = fiber, onValueChange = { fiber = it }, suffix = "g")
                        FoodTextField(label = "Açúcar por 100 g", value = sugar, onValueChange = { sugar = it }, suffix = "g")
                        FoodTextField(label = "Sódio por 100 g", value = sodium, onValueChange = { sodium = it }, suffix = "mg")
                    }
                }
                AppCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FoodTextField(label = "Unidade padrão", value = defaultUnit, onValueChange = { defaultUnit = it }, keyboardType = KeyboardType.Text)
                        FoodTextField(label = "Gramas por unidade", value = gramsPerUnit, onValueChange = { gramsPerUnit = it }, suffix = "g")
                        FoodTextField(label = "Fonte ou observação", value = source, onValueChange = { source = it }, keyboardType = KeyboardType.Text)
                    }
                }
            }

            Box(modifier = Modifier.padding(horizontal = AppSpacing.ScreenHorizontal, vertical = 12.dp)) {
                BottomPrimaryButton(
                    text = "Salvar alimento",
                    onClick = {
                        if (canSave) {
                            onSave(
                                CustomFoodInput(
                                    name = name.trim(),
                                    category = category.trim(),
                                    aliases = aliases.trim(),
                                    kcal100g = kcal.toNonNegativeDouble() ?: 0.0,
                                    carbs100g = carbs.toNonNegativeDouble() ?: 0.0,
                                    protein100g = protein.toNonNegativeDouble() ?: 0.0,
                                    fat100g = fat.toNonNegativeDouble() ?: 0.0,
                                    fiber100g = fiber.toNonNegativeDouble() ?: 0.0,
                                    sugar100g = sugar.toNonNegativeDouble() ?: 0.0,
                                    sodiumMg100g = sodium.toNonNegativeDouble() ?: 0.0,
                                    defaultUnit = defaultUnit.trim(),
                                    gramsPerDefaultUnit = gramsPerUnit.toPositiveDouble() ?: 100.0,
                                    source = source.trim().ifBlank { "Cadastro manual" },
                                ),
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun FoodTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    suffix: String = "",
    keyboardType: KeyboardType = KeyboardType.Decimal,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        suffix = if (suffix.isNotEmpty()) ({ Text(suffix) }) else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    )
}

private fun String.toNonNegativeDouble(): Double? =
    replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0 }

private fun String.toPositiveDouble(): Double? =
    replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() && it > 0 }
