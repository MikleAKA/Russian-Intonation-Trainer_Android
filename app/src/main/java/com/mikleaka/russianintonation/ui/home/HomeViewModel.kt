package com.mikleaka.russianintonation.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mikleaka.russianintonation.data.models.ConstructionProgress
import com.mikleaka.russianintonation.data.models.IntonationConstruction
import com.mikleaka.russianintonation.data.repository.IntonationConstructionsRepository
import com.mikleaka.russianintonation.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.mikleaka.russianintonation.RussianIntonationApp

/**
 * ViewModel для главного экрана
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val intonationConstructionsRepository = IntonationConstructionsRepository(application)
    private val userRepository = UserRepository()
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    /**
     * Загружает данные для главного экрана
     */
    private fun loadData() {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            // Загружаем интонационные конструкции
            intonationConstructionsRepository.getIntonationConstructions().collect { constructions ->
                _uiState.update { it.copy(intonationConstructions = constructions) }
                
                // Загружаем прогресс пользователя
                userRepository.getCurrentUser().collect { user ->
                    _uiState.update { 
                        it.copy(
                            userProgress = user?.progress ?: emptyMap(),
                            isLoading = false
                        ) 
                    }
                }
            }
        }
    }
}

/**
 * Состояние UI главного экрана
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val intonationConstructions: List<IntonationConstruction> = emptyList(),
    val userProgress: Map<String, ConstructionProgress> = emptyMap()
)

/**
 * Factory для создания ViewModel
 */
class HomeViewModelFactory : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(
                application = RussianIntonationApp.getInstance()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 