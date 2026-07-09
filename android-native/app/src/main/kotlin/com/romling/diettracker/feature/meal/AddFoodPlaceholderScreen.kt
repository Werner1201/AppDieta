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
import androidx.compose.material3.MaterialTheme
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
fun AddFoodPlaceholderScreen(
    meal: TodayMealSummary,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
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
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                AddModeTile(icon = "🔍", label = "Pesquisar", selected = true, modifier = Modifier.weight(1f))
                AddModeTile(icon = "📷", label = "Câmera", modifier = Modifier.weight(1f))
                AddModeTile(icon = "▥", label = "Código", modifier = Modifier.weight(1f))
            }
            Text(
                text = "Adicionar alimento",
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
private fun AddModeTile(
    icon: String,
    label: String,
    selected: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(104.dp),
        shape = com.romling.diettracker.core.ui.theme.AppShapes.Card,
        color = if (selected) AppColors.Green else AppColors.Panel,
        border = androidx.compose.foundation.BorderStroke(3.dp, if (selected) AppColors.Accent else AppColors.Line),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = icon, style = MaterialTheme.typography.headlineMedium)
                Text(text = label, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
