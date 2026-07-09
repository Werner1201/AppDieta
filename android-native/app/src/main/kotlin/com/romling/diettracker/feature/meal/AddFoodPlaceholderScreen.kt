package com.romling.diettracker.feature.meal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.romling.diettracker.core.ui.components.AppCard
import com.romling.diettracker.core.ui.theme.AppColors
import com.romling.diettracker.core.ui.theme.AppSpacing
import com.romling.diettracker.feature.today.TodayMealSummary

@Composable
fun AddFoodScreen(
    meal: TodayMealSummary,
    state: AddFoodUiState,
    onQueryChange: (String) -> Unit,
    onAddFood: (FoodSearchItem) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.ScreenHorizontal, vertical = 34.dp),
        verticalArrangement = Arrangement.spacedBy(26.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Text(
                text = "×",
                modifier = Modifier.clickable(onClick = onClose),
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = meal.label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        AppCard {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("O que você comeu?") },
                textStyle = MaterialTheme.typography.bodyLarge,
            )
            FoodsCard(foods = state.foods, onAddFood = onAddFood)
        }
    }
}

@Composable
private fun FoodsCard(foods: List<FoodSearchItem>, onAddFood: (FoodSearchItem) -> Unit) {
    Column {
        foods.take(20).forEachIndexed { index, food ->
            FoodRow(food = food, onAddFood = onAddFood)
            if (index < foods.take(20).lastIndex) {
                HorizontalDivider(color = AppColors.Line, thickness = 1.dp)
            }
        }
    }
}

@Composable
private fun FoodRow(food: FoodSearchItem, onAddFood: (FoodSearchItem) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = food.name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = food.serving, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(text = "${food.kcal.toInt()} kcal", style = MaterialTheme.typography.bodyLarge)
        Surface(
            modifier = Modifier.clickable { onAddFood(food) },
            shape = androidx.compose.foundation.shape.CircleShape,
            color = AppColors.Background,
            border = androidx.compose.foundation.BorderStroke(2.dp, AppColors.Accent),
        ) {
            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
                Text(text = "+", color = AppColors.Accent, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
