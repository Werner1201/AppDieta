package com.romling.diettracker.feature.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.romling.diettracker.data.repository.FoodRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class AddFoodViewModel(
    private val foodRepository: FoodRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val foods = query.flatMapLatest { foodRepository.search(it) }

    val state: StateFlow<AddFoodUiState> = combine(query, foods) { query, foods ->
        AddFoodUiState(
            query = query,
            foods = foods.map {
                FoodSearchItem(
                    id = it.id,
                    name = it.name,
                    serving = it.defaultUnit,
                    kcal = it.kcal100g,
                )
            },
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AddFoodUiState())

    fun updateQuery(value: String) {
        query.value = value
    }
}

class AddFoodViewModelFactory(
    private val foodRepository: FoodRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AddFoodViewModel(foodRepository) as T
    }
}

data class AddFoodUiState(
    val query: String = "",
    val foods: List<FoodSearchItem> = emptyList(),
)

data class FoodSearchItem(
    val id: Long,
    val name: String,
    val serving: String,
    val kcal: Double,
)
