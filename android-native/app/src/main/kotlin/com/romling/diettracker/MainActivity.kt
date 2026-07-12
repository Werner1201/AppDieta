package com.romling.diettracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.romling.diettracker.feature.chatgpt.ChatGptImportViewModel
import com.romling.diettracker.feature.chatgpt.ChatGptImportViewModelFactory
import com.romling.diettracker.feature.meal.AddFoodViewModel
import com.romling.diettracker.feature.meal.AddFoodViewModelFactory
import com.romling.diettracker.feature.recipes.RecipesViewModel
import com.romling.diettracker.feature.recipes.RecipesViewModelFactory
import com.romling.diettracker.feature.today.TodayViewModel
import com.romling.diettracker.feature.today.TodayViewModelFactory

class MainActivity : ComponentActivity() {
    private lateinit var chatGptImportViewModel: ChatGptImportViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as DietTrackerApplication).container
        val todayViewModel = ViewModelProvider(
            this,
            TodayViewModelFactory(
                container.diaryRepository,
                container.waterRepository,
                container.weightRepository,
                container.settingsRepository,
                container.activityRepository,
            ),
        )[TodayViewModel::class.java]
        val addFoodViewModel = ViewModelProvider(
            this,
            AddFoodViewModelFactory(container.foodRepository, container.diaryRepository),
        )[AddFoodViewModel::class.java]
        chatGptImportViewModel = ViewModelProvider(
            this,
            ChatGptImportViewModelFactory(container.diaryRepository),
        )[ChatGptImportViewModel::class.java]
        val recipesViewModel = ViewModelProvider(
            this,
            RecipesViewModelFactory(container.recipeRepository, container.foodRepository, container.diaryRepository),
        )[RecipesViewModel::class.java]
        handleImportIntent(intent)
        setContent { DietTrackerApp(todayViewModel, addFoodViewModel, chatGptImportViewModel, recipesViewModel) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleImportIntent(intent)
    }

    private fun handleImportIntent(intent: Intent) {
        val uri = intent.data ?: return
        if (intent.action == Intent.ACTION_VIEW && uri.scheme == "romlingdiet") {
            chatGptImportViewModel.loadExternalContent(uri.toString())
        }
    }
}
