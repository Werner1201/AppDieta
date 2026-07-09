package com.romling.diettracker

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.romling.diettracker.core.ui.theme.AppColors
import com.romling.diettracker.core.ui.theme.DietTrackerTheme
import com.romling.diettracker.feature.meal.AddFoodScreen
import com.romling.diettracker.feature.meal.AddFoodViewModel
import com.romling.diettracker.feature.today.TodayMealSummary
import com.romling.diettracker.feature.today.TodayScreen
import com.romling.diettracker.feature.today.TodayViewModel

private enum class AppTab(val label: String, val icon: String) {
    DIARY("Diário", "📋"),
    FASTING("Jejum", "⏱️"),
    RECIPES("Receitas", "🥘"),
    PROFILE("Perfil", "👤"),
    PRO("Pro", "⭐"),
}

@Composable
fun DietTrackerApp(todayViewModel: TodayViewModel, addFoodViewModel: AddFoodViewModel) {
    val state by todayViewModel.state.collectAsState()
    val addFoodState by addFoodViewModel.state.collectAsState()
    var addMeal by remember { mutableStateOf<TodayMealSummary?>(null) }
    var selectedTab by remember { mutableStateOf(AppTab.DIARY) }

    DietTrackerTheme {
        if (addMeal != null) {
            AddFoodScreen(
                meal = addMeal!!,
                state = addFoodState,
                onQueryChange = addFoodViewModel::updateQuery,
                onSelectFood = addFoodViewModel::selectFood,
                onOpenFoodDetails = addFoodViewModel::openFoodDetails,
                onCloseFoodDetails = addFoodViewModel::closeFoodDetails,
                onAddFood = { food, portion -> addFoodViewModel.addFood(addMeal!!.key, food.id, portion) { addMeal = null } },
                onClose = { addMeal = null },
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        AppTab.DIARY -> TodayScreen(
                            state = state,
                            onAddMeal = { addMeal = it },
                            onRemoveEntry = todayViewModel::removeEntry,
                            onAddWater = todayViewModel::addWater,
                            onRemoveLastWater = todayViewModel::removeLastWater,
                            onAddWeight = todayViewModel::addWeight,
                        )
                        else -> TabPlaceholder(selectedTab)
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

@Composable
private fun AppBottomNavBar(selectedTab: AppTab, onTabSelected: (AppTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(AppColors.BottomBar),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = tab.icon,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                )
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

@Composable
private fun TabPlaceholder(tab: AppTab) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = tab.icon, style = MaterialTheme.typography.displayMedium)
            Text(text = tab.label, style = MaterialTheme.typography.headlineMedium)
            Text(text = "Em breve", style = MaterialTheme.typography.bodyLarge, color = AppColors.TextSecondary)
        }
    }
}
