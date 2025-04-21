package com.mikleaka.russianintonation.ui.intonation

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mikleaka.russianintonation.data.models.IntonationConstruction
import com.mikleaka.russianintonation.data.models.Level
import com.mikleaka.russianintonation.data.repository.IntonationConstructionsRepository
import com.mikleaka.russianintonation.data.repository.UserRepository
import com.mikleaka.russianintonation.data.repository.AudioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import androidx.compose.ui.platform.LocalContext
import com.mikleaka.russianintonation.RussianIntonationApp

/**
 * ViewModel для экрана деталей интонационной конструкции
 */
class IntonationDetailsViewModel(
    application: Application,
    private val constructionId: String
) : AndroidViewModel(application) {
    
    private val intonationConstructionsRepository = IntonationConstructionsRepository(application)
    private val userRepository = UserRepository()
    private var mediaPlayer: MediaPlayer? = null
    
    private val _uiState = MutableStateFlow(IntonationDetailsUiState())
    val uiState: StateFlow<IntonationDetailsUiState> = _uiState.asStateFlow()
    
    init {
        loadIntonationConstruction()
    }
    
    override fun onCleared() {
        releaseMediaPlayer()
        super.onCleared()
    }
    
    /**
     * Загружает данные об интонационной конструкции
     */
    private fun loadIntonationConstruction() {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            intonationConstructionsRepository.getIntonationConstructionById(constructionId).collect { construction ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        construction = construction
                    )
                }
            }
        }
    }
    
    /**
     * Воспроизводит эталонное аудио для уровня
     */
    fun playReferenceAudio(level: Level) {
        // Сначала останавливаем любое текущее воспроизведение
        stopPlayback()
        
        // Устанавливаем ID текущего воспроизводимого уровня
        _uiState.update { it.copy(currentlyPlayingLevelId = level.id) }
        
        viewModelScope.launch {
            try {
                // Создаем новый экземпляр AudioRepository для каждого воспроизведения
                val audioRepository = AudioRepository(getApplication())
                
                // Воспроизводим аудио
                audioRepository.playAudioFromAssets(level.referenceAudioUrl).fold(
                    onSuccess = {
                        // Наблюдаем за статусом воспроизведения
                        viewModelScope.launch {
                            // Ждем немного, чтобы начать проверку
                            kotlinx.coroutines.delay(300)
                            
                            // Проверяем каждые 500 мс, не закончилось ли воспроизведение
                            while (audioRepository.isPlaying()) {
                                kotlinx.coroutines.delay(500)
                            }
                            
                            // Воспроизведение завершилось, обновляем UI
                            _uiState.update { it.copy(currentlyPlayingLevelId = null) }
                        }
                    },
                    onFailure = { e ->
                        // В случае ошибки сбрасываем состояние
                        _uiState.update { it.copy(currentlyPlayingLevelId = null) }
                        e.printStackTrace()
                    }
                )
            } catch (e: IOException) {
                // В случае ошибки сбрасываем состояние
                _uiState.update { it.copy(currentlyPlayingLevelId = null) }
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Останавливает воспроизведение аудио
     */
    fun stopPlayback() {
        // Сначала обновляем UI, чтобы кнопка сразу изменилась
        _uiState.update { it.copy(currentlyPlayingLevelId = null) }
        
        // Затем останавливаем воспроизведение
        viewModelScope.launch {
            try {
                val audioRepository = AudioRepository(getApplication())
                audioRepository.stopAudio()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Освобождает ресурсы MediaPlayer
     */
    private fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

/**
 * Состояние UI экрана деталей интонационной конструкции
 */
data class IntonationDetailsUiState(
    val isLoading: Boolean = false,
    val construction: IntonationConstruction? = null,
    val currentlyPlayingLevelId: String? = null
)

/**
 * Factory для создания ViewModel с параметрами
 */
class IntonationDetailsViewModelFactory(
    private val constructionId: String
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IntonationDetailsViewModel::class.java)) {
            return IntonationDetailsViewModel(
                application = RussianIntonationApp.getInstance(),
                constructionId = constructionId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 