package com.romling.diettracker.feature.chatgpt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.romling.diettracker.data.repository.DiaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatGptImportViewModel(
    private val diaryRepository: DiaryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatGptImportUiState())
    val state: StateFlow<ChatGptImportUiState> = _state.asStateFlow()

    fun updateJson(text: String) {
        _state.update { it.copy(json = text, parseError = null, savedCount = 0) }
    }

    fun parse() {
        val raw = _state.value.json.trim()
        try {
            _state.update { it.copy(preview = ChatGptImportParser.parse(raw), parseError = null) }
        } catch (e: Exception) {
            _state.update { it.copy(parseError = e.message?.take(160) ?: "Importação inválida.", preview = emptyList()) }
        }
    }

    fun loadExternalContent(text: String) {
        _state.update {
            it.copy(
                json = text,
                parseError = null,
                savedCount = 0,
                externalRequestId = it.externalRequestId + 1,
            )
        }
        parse()
    }

    fun saveAll(date: String, onDone: () -> Unit = {}) {
        val items = _state.value.preview
        if (items.isEmpty()) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            items.forEach { item ->
                diaryRepository.addImportedFood(
                    date = date,
                    mealType = item.mealType,
                    name = item.name,
                    kcal = item.kcal,
                    carbs = item.carbs,
                    protein = item.protein,
                    fat = item.fat,
                    gramsTotal = item.gramsTotal,
                )
            }
            _state.update { it.copy(isSaving = false, savedCount = items.size, preview = emptyList(), json = "") }
            onDone()
        }
    }

    fun reset() {
        _state.value = ChatGptImportUiState()
    }
}

class ChatGptImportViewModelFactory(
    private val diaryRepository: DiaryRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ChatGptImportViewModel(diaryRepository) as T
}

data class ChatGptImportUiState(
    val json: String = "",
    val preview: List<ImportItem> = emptyList(),
    val parseError: String? = null,
    val isSaving: Boolean = false,
    val savedCount: Int = 0,
    val externalRequestId: Int = 0,
)
