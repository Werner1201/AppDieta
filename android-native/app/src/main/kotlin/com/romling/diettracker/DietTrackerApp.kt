package com.romling.diettracker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.romling.diettracker.core.ui.theme.DietTrackerTheme
import com.romling.diettracker.feature.meal.AddFoodScreen
import com.romling.diettracker.feature.meal.AddFoodViewModel
import com.romling.diettracker.feature.today.TodayScreen
import com.romling.diettracker.feature.today.TodayMealSummary
import com.romling.diettracker.feature.today.TodayViewModel

@Composable
fun DietTrackerApp(todayViewModel: TodayViewModel, addFoodViewModel: AddFoodViewModel) {
    val state by todayViewModel.state.collectAsState()
    val addFoodState by addFoodViewModel.state.collectAsState()
    var addMeal by remember { mutableStateOf<TodayMealSummary?>(null) }
    DietTrackerTheme {
        if (addMeal == null) {
            TodayScreen(
                state = state,
                onAddMeal = { addMeal = it },
                onRemoveEntry = todayViewModel::removeEntry,
                onAddWater = todayViewModel::addWater,
                onRemoveLastWater = todayViewModel::removeLastWater,
                onAddWeight = todayViewModel::addWeight,
            )
        } else {
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
        }
    }
}
