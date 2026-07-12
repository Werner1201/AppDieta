package com.romling.diettracker

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.romling.diettracker.core.ui.theme.AppColors
import com.romling.diettracker.core.ui.theme.DietTrackerTheme
import com.romling.diettracker.feature.meal.AddFoodScreen
import com.romling.diettracker.feature.meal.AddFoodViewModel
import com.romling.diettracker.feature.chatgpt.ChatGptImportScreen
import com.romling.diettracker.feature.chatgpt.ChatGptImportViewModel
import com.romling.diettracker.feature.meal.CustomFoodsScreen
import com.romling.diettracker.data.local.entity.RecipeEntity
import com.romling.diettracker.feature.recipes.RecipeDetailScreen
import com.romling.diettracker.feature.recipes.RecipesScreen
import com.romling.diettracker.feature.recipes.RecipesViewModel
import com.romling.diettracker.feature.meal.MealDetailScreen
import com.romling.diettracker.feature.settings.SettingsScreen
import com.romling.diettracker.feature.settings.BackupImportScreen
import com.romling.diettracker.feature.today.CalendarScreen
import com.romling.diettracker.feature.today.StreakScreen
import com.romling.diettracker.feature.today.TodayMealSummary
import com.romling.diettracker.feature.today.TodayScreen
import com.romling.diettracker.feature.today.TodayViewModel
import com.romling.diettracker.feature.weight.WeightScreen

private enum class AppTab(val label: String, val icon: String) {
    DIARY("Diário", "📋"),
    RECIPES("Receitas", "🥘"),
    PROFILE("Perfil", "👤"),
}

@Composable
fun DietTrackerApp(
    todayViewModel: TodayViewModel,
    addFoodViewModel: AddFoodViewModel,
    chatGptImportViewModel: ChatGptImportViewModel,
    recipesViewModel: RecipesViewModel,
) {
    val state by todayViewModel.state.collectAsState()
    val currentDate by todayViewModel.currentDate.collectAsState()
    val calendarGreenDays by todayViewModel.calendarGreenDays.collectAsState()
    val addFoodState by addFoodViewModel.state.collectAsState()
    val customFoods by addFoodViewModel.customFoods.collectAsState()
    val importState by chatGptImportViewModel.state.collectAsState()
    val recipes by recipesViewModel.recipes.collectAsState()
    val selectedIngredients by recipesViewModel.selectedIngredients.collectAsState()
    val recipeFoodResults by recipesViewModel.foodResults.collectAsState()
    var detailRecipe by remember { mutableStateOf<RecipeEntity?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var addMeal by remember { mutableStateOf<TodayMealSummary?>(null) }
    var detailMeal by remember { mutableStateOf<TodayMealSummary?>(null) }
    var showCalendar by remember { mutableStateOf(false) }
    var showStreak by remember { mutableStateOf(false) }
    var showWeight by remember { mutableStateOf(false) }
    var showCustomFoods by remember { mutableStateOf(false) }
    var showImport by remember { mutableStateOf(false) }
    var showBackupImport by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(AppTab.DIARY) }

    LaunchedEffect(importState.externalRequestId) {
        if (importState.externalRequestId > 0) showImport = true
    }

    BackHandler(
        enabled = detailRecipe != null || showImport || showBackupImport || showCustomFoods ||
            showWeight || showStreak || showCalendar || detailMeal != null || addMeal != null,
    ) {
        when {
            detailRecipe != null -> {
                detailRecipe = null
                recipesViewModel.clearRecipe()
            }
            showImport -> {
                chatGptImportViewModel.reset()
                showImport = false
            }
            showBackupImport -> showBackupImport = false
            showCustomFoods -> showCustomFoods = false
            showWeight -> showWeight = false
            showStreak -> showStreak = false
            showCalendar -> showCalendar = false
            detailMeal != null -> detailMeal = null
            addMeal != null -> addMeal = null
        }
    }

    DietTrackerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background),
            contentAlignment = Alignment.TopCenter,
        ) {
        Box(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxSize(),
        ) {
        if (detailRecipe != null && !showImport) {
            RecipeDetailScreen(
                recipe = detailRecipe!!,
                ingredients = selectedIngredients,
                foodResults = recipeFoodResults,
                onSearchFoods = recipesViewModel::searchFoods,
                onClearFoodSearch = recipesViewModel::clearFoodSearch,
                onAddIngredient = { food, grams -> recipesViewModel.addIngredientFromFood(detailRecipe!!.id, food, grams) },
                onRemoveIngredient = recipesViewModel::removeIngredient,
                onAddToDiary = { mealType ->
                    recipesViewModel.addToDiary(state.date, mealType, detailRecipe!!, selectedIngredients) {
                        detailRecipe = null
                        recipesViewModel.clearRecipe()
                        selectedTab = AppTab.DIARY
                    }
                },
                onClose = { detailRecipe = null; recipesViewModel.clearRecipe() },
            )
        } else if (showImport) {
            ChatGptImportScreen(
                state = importState,
                chatGptUrl = state.chatGptUrl,
                chatGptPrompt = state.chatGptPrompt,
                onJsonChange = chatGptImportViewModel::updateJson,
                onParse = chatGptImportViewModel::parse,
                onSaveAll = { chatGptImportViewModel.saveAll(state.date) { showImport = false } },
                onClose = { chatGptImportViewModel.reset(); showImport = false },
            )
        } else if (showBackupImport) {
            BackupImportScreen(
                onSave = { entries -> todayViewModel.importBackup(entries) { showBackupImport = false } },
                onClose = { showBackupImport = false },
            )
        } else if (showCustomFoods) {
            CustomFoodsScreen(
                foods = customFoods,
                onDelete = addFoodViewModel::deleteCustomFood,
                onClose = { showCustomFoods = false },
            )
        } else if (showWeight) {
            WeightScreen(
                weight = state.weight,
                history = state.weightHistory,
                onAddWeight = { kg -> todayViewModel.addWeight(kg); showWeight = false },
                onClose = { showWeight = false },
            )
        } else if (showStreak) {
            StreakScreen(streak = state.streak, onClose = { showStreak = false })
        } else if (showCalendar) {
            CalendarScreen(
                greenDays = calendarGreenDays,
                selectedDate = currentDate,
                onDayClick = { date ->
                    todayViewModel.goToDate(date)
                    showCalendar = false
                },
                onMonthChanged = todayViewModel::setCalendarMonth,
                onClose = { showCalendar = false },
            )
        } else if (detailMeal != null) {
            MealDetailScreen(
                meal = detailMeal!!,
                entries = state.entries,
                onRemoveEntry = todayViewModel::removeEntry,
                onEditEntry = todayViewModel::updateEntryGrams,
                onAddMore = { addMeal = detailMeal; detailMeal = null },
                onClose = { detailMeal = null },
            )
        } else if (addMeal != null) {
            AddFoodScreen(
                meal = addMeal!!,
                state = addFoodState,
                onMealChanged = addFoodViewModel::setMealType,
                onQueryChange = addFoodViewModel::updateQuery,
                onSelectFood = addFoodViewModel::selectFood,
                onOpenFoodDetails = addFoodViewModel::openFoodDetails,
                onCloseFoodDetails = addFoodViewModel::closeFoodDetails,
                onAddFood = { food, portion -> addFoodViewModel.addFood(addMeal!!.key, food.id, portion) { addMeal = null } },
                onCreateFood = { input -> addFoodViewModel.createCustomFood(input) },
                onOpenChatGptImport = {
                    chatGptImportViewModel.reset()
                    showImport = true
                },
                onClose = { addMeal = null },
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        AppTab.DIARY -> TodayScreen(
                            state = state,
                            onAddMeal = { addMeal = it },
                            onOpenMealDetail = { detailMeal = it },
                            onRemoveEntry = todayViewModel::removeEntry,
                            onAddWater = todayViewModel::addWater,
                            onRemoveLastWater = todayViewModel::removeLastWater,
                            onAddWeight = todayViewModel::addWeight,
                            onOpenWeight = { showWeight = true },
                            onOpenImport = { chatGptImportViewModel.reset(); showImport = true },
                            onPreviousDay = todayViewModel::previousDay,
                            onNextDay = todayViewModel::nextDay,
                            onOpenCalendar = { showCalendar = true },
                            onOpenStreak = { showStreak = true },
                        )
                        AppTab.PROFILE -> SettingsScreen(
                            state = state,
                            onSaveGoals = todayViewModel::saveGoals,
                            onOpenCustomFoods = { showCustomFoods = true },
                            onImportDiary = { showBackupImport = true },
                            onExportDiary = {
                                scope.launch {
                                    val json = todayViewModel.exportJson()
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "AppDieta - Diário exportado")
                                        putExtra(Intent.EXTRA_TEXT, json)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Compartilhar diário"))
                                }
                            },
                        )
                        AppTab.RECIPES -> RecipesScreen(
                            recipes = recipes,
                            onCreate = recipesViewModel::create,
                            onDelete = recipesViewModel::delete,
                            onRecipeClick = { recipe ->
                                recipesViewModel.selectRecipe(recipe.id)
                                detailRecipe = recipe
                            },
                        )
                    }
                }
                AppBottomNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                )
            }
        }
        }
        }
    }
}

@Composable
private fun AppBottomNavBar(selectedTab: AppTab, onTabSelected: (AppTab) -> Unit) {
    val largeText = LocalDensity.current.fontScale >= 1.5f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .background(AppColors.BottomBar),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            Column(
                modifier = Modifier
                    .weight(1f)
                    .selectable(
                        selected = isSelected,
                        role = Role.Tab,
                        onClick = { onTabSelected(tab) },
                    )
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (!largeText) {
                    Text(
                        text = tab.icon,
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                    )
                }
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) AppColors.Accent else AppColors.TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
