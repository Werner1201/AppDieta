package com.romling.diettracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.romling.diettracker.feature.meal.AddFoodViewModel
import com.romling.diettracker.feature.meal.AddFoodViewModelFactory
import com.romling.diettracker.feature.today.TodayViewModel
import com.romling.diettracker.feature.today.TodayViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as DietTrackerApplication).container
        val todayViewModel = ViewModelProvider(
            this,
            TodayViewModelFactory(container.diaryRepository, container.waterRepository, container.weightRepository),
        )[TodayViewModel::class.java]
        val addFoodViewModel = ViewModelProvider(
            this,
            AddFoodViewModelFactory(container.foodRepository, container.diaryRepository),
        )[AddFoodViewModel::class.java]
        setContent { DietTrackerApp(todayViewModel, addFoodViewModel) }
    }
}
