package com.romling.diettracker.feature.today

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import com.romling.diettracker.core.ui.components.AppCard
import com.romling.diettracker.core.ui.components.MacroProgressBar
import com.romling.diettracker.core.ui.components.SectionTitle
import com.romling.diettracker.core.ui.theme.AppColors
import com.romling.diettracker.core.ui.theme.AppShapes
import com.romling.diettracker.core.ui.theme.AppSpacing
import com.romling.diettracker.core.ui.theme.DietTrackerTheme

@Composable
fun TodayScreen(
    state: TodayUiState,
    onAddMeal: (TodayMealSummary) -> Unit = {},
    onRemoveEntry: (Long) -> Unit = {},
    onAddWater: (Int) -> Unit = {},
    onRemoveLastWater: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showRegisteredOnly by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.ScreenHorizontal, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        TodayHeader(state)
        SmartTipsButton()
        SectionTitle(title = "Resumo", actionLabel = "Detalhes")
        SummaryCard(state)
        SectionTitle(title = "Alimentação", actionLabel = "Mais")
        FoodFilterTabs(
            showRegisteredOnly = showRegisteredOnly,
            onShowAll = { showRegisteredOnly = false },
            onShowRegistered = { showRegisteredOnly = true },
        )
        if (!showRegisteredOnly) {
            MealsCard(meals = state.meals, onAddMeal = onAddMeal)
        }
        if (state.entries.isNotEmpty()) {
            SectionTitle(title = if (showRegisteredOnly) "Registrados hoje" else "Registrados")
            EntriesCard(entries = state.entries, onRemoveEntry = onRemoveEntry)
        } else if (showRegisteredOnly) {
            EmptyEntriesCard()
        }
        if (!showRegisteredOnly) {
            SectionTitle(title = "Monitor de água")
            WaterCard(water = state.water, onAddWater = onAddWater, onRemoveLastWater = onRemoveLastWater)
        }
    }
}

@Composable
private fun FoodFilterTabs(showRegisteredOnly: Boolean, onShowAll: () -> Unit, onShowRegistered: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        FilterTab(text = "Todos", selected = !showRegisteredOnly, onClick = onShowAll, modifier = Modifier.weight(1f))
        FilterTab(text = "Registrados", selected = showRegisteredOnly, onClick = onShowRegistered, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun FilterTab(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = AppShapes.Button,
        color = if (selected) AppColors.Green else AppColors.Panel,
        border = BorderStroke(2.dp, if (selected) AppColors.Accent else AppColors.Line),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun TodayHeader(state: TodayUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Hoje", style = MaterialTheme.typography.headlineLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = "💎 0", style = MaterialTheme.typography.labelLarge)
                Text(text = "🔥 0", style = MaterialTheme.typography.labelLarge)
                Text(text = "🗓️", style = MaterialTheme.typography.labelLarge)
            }
        }
        Text(
            text = "Semana ${state.week}",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal),
        )
    }
}

@Composable
private fun SmartTipsButton() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp),
        shape = AppShapes.Button,
        color = AppColors.TipBackground,
        border = BorderStroke(3.dp, AppColors.TipBorder),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "✦ Ver minhas Dicas Inteligentes",
                color = AppColors.TipText,
                style = MaterialTheme.typography.labelLarge,
            )
            Text(text = "→", color = AppColors.TipText, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun SummaryCard(state: TodayUiState) {
    AppCard(contentPadding = PaddingValues(0.dp)) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SummarySideMetric(value = state.totals.kcal.toInt().toString(), label = "Consumidas")
                RemainingRing(state)
                SummarySideMetric(value = "0", label = "Gastas")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                MacroMetric("Carboidratos", state.totals.carbs, 284.0, Modifier.weight(1f))
                MacroMetric("Proteína", state.totals.protein, 114.0, Modifier.weight(1f))
                MacroMetric("Gordura", state.totals.fat, 75.0, Modifier.weight(1f))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .background(AppColors.Green),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🍽️ Agora: Comer", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun SummarySideMetric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineMedium)
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun RemainingRing(state: TodayUiState) {
    val progress = if (state.dailyKcal <= 0.0) 0f else (state.totals.kcal / state.dailyKcal).toFloat()
    Box(modifier = Modifier.size(132.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(116.dp)) {
            val stroke = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
            val inset = stroke.width / 2
            val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
            drawArc(
                color = AppColors.Line,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke,
            )
            drawArc(
                color = AppColors.Accent,
                startAngle = 135f,
                sweepAngle = 270f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = state.remainingKcal.toString(), style = MaterialTheme.typography.headlineMedium)
            Text(text = "Restantes", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun MacroMetric(label: String, value: Double, goal: Double, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        MacroProgressBar(progress = (value / goal).toFloat())
        Text(
            text = "${value.toInt()} / ${goal.toInt()} g",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MealsCard(meals: List<TodayMealSummary>, onAddMeal: (TodayMealSummary) -> Unit) {
    AppCard(contentPadding = PaddingValues(horizontal = 28.dp, vertical = 0.dp)) {
        Column {
            meals.forEachIndexed { index, meal ->
                MealRow(meal = meal, onAddMeal = onAddMeal)
                if (index < meals.lastIndex) {
                    HorizontalDivider(color = AppColors.Line, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun MealRow(meal: TodayMealSummary, onAddMeal: (TodayMealSummary) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Surface(
            modifier = Modifier.size(AppSpacing.MealIconSize),
            shape = CircleShape,
            color = AppColors.Line.copy(alpha = 0.55f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = meal.icon, style = MaterialTheme.typography.titleLarge)
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = "${meal.label} →", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "${meal.kcal.toInt()} / ${meal.goalKcal} kcal" + meal.items.takeIf { it.isNotBlank() }?.let { " - $it" }.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Surface(
            modifier = Modifier
                .size(AppSpacing.MealActionSize)
                .clickable { onAddMeal(meal) },
            shape = CircleShape,
            color = AppColors.TextPrimary,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = "+", color = AppColors.Background, style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}

@Composable
private fun EntriesCard(entries: List<TodayEntrySummary>, onRemoveEntry: (Long) -> Unit) {
    AppCard(contentPadding = PaddingValues(horizontal = 22.dp, vertical = 0.dp)) {
        Column {
            entries.forEachIndexed { index, entry ->
                EntryRow(entry = entry, onRemoveEntry = onRemoveEntry)
                if (index < entries.lastIndex) {
                    HorizontalDivider(color = AppColors.Line, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun EmptyEntriesCard() {
    AppCard {
        Text(text = "Nenhum alimento registrado hoje.", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun WaterCard(water: TodayWaterSummary, onAddWater: (Int) -> Unit, onRemoveLastWater: () -> Unit) {
    AppCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Água", style = MaterialTheme.typography.titleLarge)
            Text(text = "Objetivo: ${"%.1f".format(water.goalLiters)} litros", style = MaterialTheme.typography.bodyMedium)
            Text(text = "%.2f L".format(water.consumedLiters), style = MaterialTheme.typography.headlineLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(100, 200, 250, 500).forEach { amount ->
                    WaterButton(text = "+${amount}", onClick = { onAddWater(amount) }, modifier = Modifier.weight(1f))
                }
            }
            if (water.consumedMl > 0) {
                Text(
                    text = "Desfazer último copo",
                    modifier = Modifier.clickable(onClick = onRemoveLastWater),
                    color = AppColors.Accent,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun WaterButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .height(46.dp)
            .clickable(onClick = onClick),
        shape = AppShapes.Button,
        color = AppColors.Green,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun EntryRow(entry: TodayEntrySummary, onRemoveEntry: (Long) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = entry.name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = "${entry.kcal.toInt()} kcal", style = MaterialTheme.typography.bodyMedium)
        }
        Surface(
            modifier = Modifier.clickable { onRemoveEntry(entry.id) },
            shape = CircleShape,
            color = AppColors.Background,
            border = BorderStroke(2.dp, AppColors.Remove),
        ) {
            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
                Text(text = "−", color = AppColors.Remove, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Preview
@Composable
private fun TodayScreenPreview() {
    DietTrackerTheme {
        TodayScreen(
            TodayUiState(
                date = "2026-07-01",
                week = 27,
                dailyKcal = 2333.0,
                dailyProtein = 114.0,
                totals = TodayNutritionTotals(kcal = 996.0, carbs = 81.0, protein = 83.0, fat = 35.0),
                remainingKcal = 1337,
            ),
        )
    }
}
