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

@Composable
fun CreateFoodScreen(
    onClose: () -> Unit,
    onSave: (name: String, kcal: Double, carbs: Double, protein: Double, fat: Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("") }
    var kcal by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }

    val canSave = name.isNotBlank() && kcal.toDoubleOrNullPositive() != null

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
                        FoodTextField(label = "Calorias por 100 g", value = kcal, onValueChange = { kcal = it }, suffix = "kcal")
                        FoodTextField(label = "Carboidratos por 100 g", value = carbs, onValueChange = { carbs = it }, suffix = "g")
                        FoodTextField(label = "Proteína por 100 g", value = protein, onValueChange = { protein = it }, suffix = "g")
                        FoodTextField(label = "Gordura por 100 g", value = fat, onValueChange = { fat = it }, suffix = "g")
                    }
                }
            }

            Box(modifier = Modifier.padding(horizontal = AppSpacing.ScreenHorizontal, vertical = 12.dp)) {
                BottomPrimaryButton(
                    text = "Salvar alimento",
                    onClick = {
                        if (canSave) {
                            onSave(
                                name,
                                kcal.toDoubleOrNullPositive() ?: 0.0,
                                carbs.toDoubleOrNullPositive() ?: 0.0,
                                protein.toDoubleOrNullPositive() ?: 0.0,
                                fat.toDoubleOrNullPositive() ?: 0.0,
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

private fun String.toDoubleOrNullPositive(): Double? =
    replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0 }
