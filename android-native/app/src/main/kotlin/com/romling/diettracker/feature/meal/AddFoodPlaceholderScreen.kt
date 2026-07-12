package com.romling.diettracker.feature.meal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
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
    onMealChanged: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    onSelectFood: (Long) -> Unit,
    onOpenFoodDetails: (Long) -> Unit,
    onCloseFoodDetails: () -> Unit,
    onAddFood: (FoodSearchItem, FoodPortionItem?) -> Unit,
    onCreateFood: (CustomFoodInput) -> Unit,
    onOpenChatGptImport: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCreateFood by remember { mutableStateOf(false) }
    var showFrequent by remember { mutableStateOf(true) }

    LaunchedEffect(meal.key) { onMealChanged(meal.key) }

    BackHandler {
        when {
            showCreateFood -> showCreateFood = false
            state.detailFood != null -> onCloseFoodDetails()
            else -> onClose()
        }
    }

    if (showCreateFood) {
        CreateFoodScreen(
            onClose = { showCreateFood = false },
            onSave = { input ->
                onCreateFood(input)
                showCreateFood = false
            },
        )
        return
    }

    if (state.detailFood != null) {
        FoodDetailScreen(
            food = state.detailFood,
            portions = state.portions,
            onClose = onCloseFoodDetails,
            onAddFood = { food, portion ->
                onAddFood(food, portion)
                onCloseFoodDetails()
                onClose()
            },
        )
        return
    }

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
                style = MaterialTheme.typography.headlineSmall,
                color = AppColors.TextPrimary,
            )
            Text(
                text = meal.label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineSmall,
                color = AppColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "+ Criar",
                modifier = Modifier.clickable { showCreateFood = true },
                style = MaterialTheme.typography.labelLarge,
                color = AppColors.Accent,
            )
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clickable(role = Role.Button, onClick = onOpenChatGptImport),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            color = AppColors.Panel,
            border = BorderStroke(2.dp, AppColors.Line),
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 18.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = "📷 Câmera / ChatGPT",
                    style = MaterialTheme.typography.labelLarge,
                    color = AppColors.Accent,
                )
            }
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
            FoodsCard(
                state = state,
                showFrequent = showFrequent && state.query.isBlank(),
                onShowFrequentChange = { showFrequent = it },
                onSelectFood = onSelectFood,
                onOpenFoodDetails = onOpenFoodDetails,
                onAddFood = onAddFood,
            )
        }
    }
}

@Composable
private fun FoodsCard(
    state: AddFoodUiState,
    showFrequent: Boolean,
    onShowFrequentChange: (Boolean) -> Unit,
    onSelectFood: (Long) -> Unit,
    onOpenFoodDetails: (Long) -> Unit,
    onAddFood: (FoodSearchItem, FoodPortionItem?) -> Unit,
) {
    val foods = (if (showFrequent) state.frequentFoods else state.foods).take(20)
    Column {
        if (state.query.isBlank()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = showFrequent,
                    onClick = { onShowFrequentChange(true) },
                    label = { Text("Frequentes") },
                )
                FilterChip(
                    selected = !showFrequent,
                    onClick = { onShowFrequentChange(false) },
                    label = { Text("Todos") },
                )
            }
        }
        if (showFrequent && foods.isEmpty()) {
            Text(
                text = "Seus alimentos mais registrados nesta refeição aparecerão aqui.",
                modifier = Modifier.padding(vertical = 18.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextSecondary,
            )
        }
        foods.forEachIndexed { index, food ->
            FoodRow(
                food = food,
                onSelectFood = onSelectFood,
                onOpenFoodDetails = onOpenFoodDetails,
                onAddFood = onAddFood,
            )
            if (state.selectedFoodId == food.id) {
                state.portions.forEach { portion ->
                    PortionRow(portion = portion, onAddFood = { onAddFood(food, portion) })
                }
            }
            if (index < foods.lastIndex) {
                HorizontalDivider(color = AppColors.Line, thickness = 1.dp)
            }
        }
    }
}

@Composable
private fun FoodRow(
    food: FoodSearchItem,
    onSelectFood: (Long) -> Unit,
    onOpenFoodDetails: (Long) -> Unit,
    onAddFood: (FoodSearchItem, FoodPortionItem?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectFood(food.id) }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onOpenFoodDetails(food.id) },
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(text = food.name, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(text = food.serving, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(
            text = "${food.kcal.toInt()} kcal",
            modifier = Modifier.width(52.dp),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.End,
            maxLines = 1,
        )
        Surface(
            modifier = Modifier.clickable { onAddFood(food, null) },
            shape = androidx.compose.foundation.shape.CircleShape,
            color = AppColors.Background,
            border = androidx.compose.foundation.BorderStroke(2.dp, AppColors.Accent),
        ) {
            Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), contentAlignment = Alignment.Center) {
                Text(text = "+", color = AppColors.Accent, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun PortionRow(portion: FoodPortionItem, onAddFood: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(onClick = onAddFood)
            .padding(start = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = portion.label, style = MaterialTheme.typography.bodyLarge)
        Text(text = "${portion.grams.toInt()} g", style = MaterialTheme.typography.bodyMedium)
    }
}
