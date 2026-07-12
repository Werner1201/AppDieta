package com.romling.diettracker.feature.meal

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.romling.diettracker.core.ui.components.AppCard
import com.romling.diettracker.core.ui.components.ConfirmDeleteDialog
import com.romling.diettracker.core.ui.theme.AppColors
import com.romling.diettracker.core.ui.theme.AppSpacing

@Composable
fun CustomFoodsScreen(
    foods: List<FoodSearchItem>,
    onDelete: (Long) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDelete by remember { mutableStateOf<FoodSearchItem?>(null) }

    pendingDelete?.let { food ->
        ConfirmDeleteDialog(
            itemName = food.name,
            onConfirm = {
                onDelete(food.id)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
    ) {
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
            Text(
                text = "Meus alimentos",
                style = MaterialTheme.typography.headlineSmall,
                color = AppColors.TextPrimary,
            )
        }

        if (foods.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppSpacing.ScreenHorizontal),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Nenhum alimento criado ainda.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.TextSecondary,
                )
            }
        } else {
            AppCard(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = AppSpacing.ScreenHorizontal, vertical = AppSpacing.SectionGap),
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(foods, key = { _, food -> food.id }) { index, food ->
                        CustomFoodRow(food = food, onDelete = { pendingDelete = food })
                        if (index < foods.lastIndex) {
                            HorizontalDivider(color = AppColors.Line, thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomFoodRow(food: FoodSearchItem, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = food.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${food.kcal.toInt()} kcal / 100 g",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary,
            )
        }
        Surface(
            modifier = Modifier
                .size(48.dp)
                .clickable(onClick = onDelete),
            shape = CircleShape,
            color = AppColors.Background,
            border = BorderStroke(2.dp, AppColors.Remove),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = "−", color = AppColors.Remove, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
