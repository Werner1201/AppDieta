package com.romling.diettracker.feature.today

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import java.time.LocalDate
import com.romling.diettracker.core.ui.components.AppCard
import com.romling.diettracker.core.ui.components.MacroProgressBar
import com.romling.diettracker.core.ui.components.SectionTitle
import com.romling.diettracker.core.ui.components.ConfirmDeleteDialog
import com.romling.diettracker.core.ui.theme.AppColors
import com.romling.diettracker.core.ui.theme.AppShapes
import com.romling.diettracker.core.ui.theme.AppSpacing
import com.romling.diettracker.core.ui.theme.DietTrackerTheme
import com.romling.diettracker.core.ui.theme.LocalAppDimensions

@Composable
fun TodayScreen(
    state: TodayUiState,
    onAddMeal: (TodayMealSummary) -> Unit = {},
    onOpenMealDetail: (TodayMealSummary) -> Unit = {},
    onRemoveEntry: (Long) -> Unit = {},
    onAddWater: (Int) -> Unit = {},
    onRemoveLastWater: () -> Unit = {},
    onAddWeight: (Double) -> Unit = {},
    onOpenWeight: () -> Unit = {},
    onOpenImport: () -> Unit = {},
    onPreviousDay: () -> Unit = {},
    onNextDay: () -> Unit = {},
    onOpenCalendar: () -> Unit = {},
    onOpenStreak: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showRegisteredOnly by remember { mutableStateOf(false) }
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
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 60.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragOffset > swipeThresholdPx) onPreviousDay()
                        else if (dragOffset < -swipeThresholdPx) onNextDay()
                        dragOffset = 0f
                    },
                    onDragCancel = { dragOffset = 0f },
                ) { _, dragAmount -> dragOffset += dragAmount }
            },
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.ScreenHorizontal, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        TodayHeader(state = state, onOpenCalendar = onOpenCalendar, onOpenStreak = onOpenStreak)
        SmartTipsButton()
        ImportButton(onClick = onOpenImport)
        SectionTitle(title = "Resumo", actionLabel = "Detalhes")
        SummaryCard(state)
        SectionTitle(title = "Alimentação", actionLabel = "Mais")
        FoodFilterTabs(
            showRegisteredOnly = showRegisteredOnly,
            onShowAll = { showRegisteredOnly = false },
            onShowRegistered = { showRegisteredOnly = true },
        )
        if (!showRegisteredOnly) {
            MealsCard(meals = state.meals, onAddMeal = onAddMeal, onOpenDetail = onOpenMealDetail)
        }
        if (state.entries.isNotEmpty()) {
            SectionTitle(title = if (showRegisteredOnly) "Registrados hoje" else "Registrados")
            EntriesCard(
                entries = state.entries,
                onRemoveEntry = { entry -> pendingDelete = entry },
            )
        } else if (showRegisteredOnly) {
            EmptyEntriesCard()
        }
        if (!showRegisteredOnly) {
            SectionTitle(title = "Monitor de água")
            WaterCard(water = state.water, onAddWater = onAddWater, onRemoveLastWater = onRemoveLastWater)
            SectionTitle(title = "Valores corporais", onAction = onOpenWeight, actionLabel = "Ver histórico")
            WeightCard(weight = state.weight, onAddWeight = onAddWeight)
        }
    }
    } // Box
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
private fun TodayHeader(state: TodayUiState, onOpenCalendar: () -> Unit = {}, onOpenStreak: () -> Unit = {}) {
    val titleText = if (state.isToday) "Hoje" else {
        val d = LocalDate.parse(state.date)
        "${d.dayOfMonth}/${d.monthValue}"
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = titleText, style = MaterialTheme.typography.headlineLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = "💎 0", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = "🔥 ${state.streak.current}",
                    modifier = Modifier.clickable(onClick = onOpenStreak),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = "🗓️",
                    modifier = Modifier.clickable(onClick = onOpenCalendar),
                    style = MaterialTheme.typography.labelLarge,
                )
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
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    SummarySideMetric(value = state.totals.kcal.toInt().toString(), label = "Consumidas")
                }
                RemainingRing(state)
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    SummarySideMetric(value = "0", label = "Gastas")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                MacroMetric("Carbs", state.totals.carbs, state.dailyCarbs, Modifier.weight(1f))
                MacroMetric("Proteína", state.totals.protein, state.dailyProtein, Modifier.weight(1f))
                MacroMetric("Gordura", state.totals.fat, state.dailyFat, Modifier.weight(1f))
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
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, maxLines = 1)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RemainingRing(state: TodayUiState) {
    val dims = LocalAppDimensions.current
    val progress = if (state.dailyKcal <= 0.0) 0f else (state.totals.kcal / state.dailyKcal).toFloat()
    Box(modifier = Modifier.size(dims.summaryRingBox), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(dims.summaryRingCanvas)) {
            val stroke = Stroke(width = dims.summaryRingStroke.toPx(), cap = StrokeCap.Round)
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
            Text(text = state.remainingKcal.toString(), style = MaterialTheme.typography.titleLarge)
            Text(text = "Restantes", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun MacroMetric(label: String, value: Double, goal: Double, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        MacroProgressBar(progress = if (goal <= 0.0) 0f else (value / goal).toFloat())
        Text(
            text = "${value.toInt()}/${goal.toInt()}g",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun MealsCard(
    meals: List<TodayMealSummary>,
    onAddMeal: (TodayMealSummary) -> Unit,
    onOpenDetail: (TodayMealSummary) -> Unit,
) {
    AppCard(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)) {
        Column {
            meals.forEachIndexed { index, meal ->
                MealRow(meal = meal, onAddMeal = onAddMeal, onOpenDetail = onOpenDetail)
                if (index < meals.lastIndex) {
                    HorizontalDivider(color = AppColors.Line, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun MealRow(
    meal: TodayMealSummary,
    onAddMeal: (TodayMealSummary) -> Unit,
    onOpenDetail: (TodayMealSummary) -> Unit,
) {
    val dims = LocalAppDimensions.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dims.mealRowHeight)
            .clickable { onOpenDetail(meal) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dims.mealRowSpacing),
    ) {
        Surface(
            modifier = Modifier.size(dims.mealIconSize),
            shape = CircleShape,
            color = AppColors.Line.copy(alpha = 0.55f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = meal.icon, style = MaterialTheme.typography.titleMedium)
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = meal.label, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = "${meal.kcal.toInt()} / ${meal.goalKcal} kcal" + meal.items.takeIf { it.isNotBlank() }?.let { " - $it" }.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Surface(
            modifier = Modifier
                .size(dims.mealActionSize)
                .clickable(onClick = { onAddMeal(meal) }),
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
private fun EntriesCard(entries: List<TodayEntrySummary>, onRemoveEntry: (TodayEntrySummary) -> Unit) {
    AppCard(contentPadding = PaddingValues(horizontal = 22.dp, vertical = 0.dp)) {
        Column {
            entries.forEachIndexed { index, entry ->
                EntryRow(entry = entry, onRemoveEntry = { onRemoveEntry(entry) })
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
private fun WeightCard(weight: TodayWeightSummary, onAddWeight: (Double) -> Unit) {
    var draftKg by remember(weight.currentKg) { mutableStateOf(weight.currentKg) }

    AppCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Peso", style = MaterialTheme.typography.titleLarge)
            Text(text = "Objetivo: ${"%.1f".format(weight.goalKg)} kg", style = MaterialTheme.typography.bodyMedium)
            Text(text = "${"%.1f".format(draftKg)} kg", style = MaterialTheme.typography.headlineLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                WeightButton(text = "−", onClick = { draftKg = maxOf(1.0, draftKg - 0.1) })
                WeightButton(text = "+", onClick = { draftKg += 0.1 })
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clickable { onAddWeight("%.1f".format(draftKg).replace(',', '.').toDouble()) },
                shape = AppShapes.Button,
                color = AppColors.TextPrimary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "Registrar", color = AppColors.Background, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun WeightButton(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(52.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = AppColors.Background,
        border = BorderStroke(2.dp, AppColors.TextPrimary),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, style = MaterialTheme.typography.headlineMedium)
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

@Composable
private fun ImportButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(onClick = onClick),
        shape = AppShapes.Button,
        color = AppColors.Panel,
        border = BorderStroke(1.dp, AppColors.Line),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "📥 Importar via ChatGPT",
                style = MaterialTheme.typography.labelLarge,
                color = AppColors.TextPrimary,
            )
            Text(text = "→", style = MaterialTheme.typography.labelLarge, color = AppColors.TextSecondary)
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
