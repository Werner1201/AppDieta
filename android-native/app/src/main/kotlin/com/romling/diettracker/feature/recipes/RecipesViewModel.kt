package com.romling.diettracker.feature.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.romling.diettracker.data.local.entity.RecipeEntity
import com.romling.diettracker.data.repository.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecipesViewModel(private val recipeRepository: RecipeRepository) : ViewModel() {
    val recipes: StateFlow<List<RecipeEntity>> = recipeRepository.allRecipes()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun create(name: String, description: String) {
        if (name.isBlank()) return
        viewModelScope.launch { recipeRepository.create(name.trim(), description.trim()) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { recipeRepository.deleteById(id) }
    }
}

class RecipesViewModelFactory(private val recipeRepository: RecipeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return RecipesViewModel(recipeRepository) as T
    }
}
