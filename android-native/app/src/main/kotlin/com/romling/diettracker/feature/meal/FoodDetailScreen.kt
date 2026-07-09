package com.romling.diettracker.feature.meal

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.romling.diettracker.core.ui.components.AppCard
import com.romling.diettracker.core.ui.components.BottomPrimaryButton
import com.romling.diettracker.core.ui.theme.AppColors
import com.romling.diettracker.core.ui.theme.AppSpacing

@Composable
fun FoodDetailScreen(
    food: FoodSearchItem,
    portions: List<FoodPortionItem>,
    onClose: () -> Unit,
    onAddFood: (FoodSearchItem, FoodPortionItem?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FoodDetailHeader(foodName = food.name, onClose = onClose)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.ScreenHorizontal, vertical = AppSpacing.SectionGap),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.SectionGap),
            ) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary,
                )
                Text(
                    text = food.serving,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextSecondary,
                )

                NutritionCard(food = food)

                if (portions.isNotEmpty()) {
                    PortionsCard(portions = portions, onAddFood = { onAddFood(food, it) })
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            Box(modifier = Modifier.padding(horizontal = AppSpacing.ScreenHorizontal, vertical = 12.dp)) {
                BottomPrimaryButton(
                    text = "Adicionar porção padrão",
                    onClick = { onAddFood(food, null) },
                )
            }
        }
    }
}

@Composable
private fun FoodDetailHeader(foodName: String, onClose: () -> Unit) {
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
            text = foodName,
            style = MaterialTheme.typography.headlineSmall,
            color = AppColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun NutritionCard(food: FoodSearchItem) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Text(
                text = "Informação nutricional por 100 g",
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            NutritionRow(label = "Calorias", value = "${food.kcal.toInt()} kcal")
            HorizontalDivider(color = AppColors.Line, thickness = 1.dp)
            NutritionRow(label = "Carboidratos", value = "%.1f g".format(food.carbs))
            HorizontalDivider(color = AppColors.Line, thickness = 1.dp)
            NutritionRow(label = "Proteína", value = "%.1f g".format(food.protein))
            HorizontalDivider(color = AppColors.Line, thickness = 1.dp)
            NutritionRow(label = "Gordura", value = "%.1f g".format(food.fat))
            HorizontalDivider(color = AppColors.Line, thickness = 1.dp)
            NutritionRow(label = "Fibra", value = "%.1f g".format(food.fiber))
            HorizontalDivider(color = AppColors.Line, thickness = 1.dp)
            NutritionRow(label = "Açúcares", value = "%.1f g".format(food.sugar))
            HorizontalDivider(color = AppColors.Line, thickness = 1.dp)
            NutritionRow(label = "Sódio", value = "${food.sodiumMg.toInt()} mg")
        }
    }
}

@Composable
private fun NutritionRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextPrimary)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.Accent,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun PortionsCard(portions: List<FoodPortionItem>, onAddFood: (FoodPortionItem) -> Unit) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Text(
                text = "Porções disponíveis",
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            portions.forEachIndexed { index, portion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable { onAddFood(portion) }
                        .padding(end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = portion.label, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextPrimary)
                    Text(
                        text = "${portion.grams.toInt()} g",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Accent,
                    )
                }
                if (index < portions.lastIndex) {
                    HorizontalDivider(color = AppColors.Line, thickness = 1.dp)
                }
            }
        }
    }
}
