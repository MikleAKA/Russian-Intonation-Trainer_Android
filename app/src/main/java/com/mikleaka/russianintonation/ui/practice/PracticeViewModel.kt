package com.mikleaka.russianintonation.ui.practice

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mikleaka.russianintonation.data.models.Level
import com.mikleaka.russianintonation.data.models.PracticeResult
import com.mikleaka.russianintonation.data.repository.AudioRepository
import com.mikleaka.russianintonation.data.repository.IntonationConstructionsRepository
import com.mikleaka.russianintonation.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import com.mikleaka.russianintonation.RussianIntonationApp

/**
 * ViewModel для экрана практики
 */
class PracticeViewModel(
    application: Application,
    private val constructionId: String,
    private val levelId: String
) : AndroidViewModel(application) {
    
    private val intonationConstructionsRepository = IntonationConstructionsRepository(application)
    private val userRepository = UserRepository()
    private val audioRepository = AudioRepository(application)
    
    private val _uiState = MutableStateFlow(PracticeUiState())
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    override fun onCleared() {
        viewModelScope.launch {
            audioRepository.stopAudio()
        }
        super.onCleared()
    }
    
    /**
     * Загружает данные для экрана практики
     */
    private fun loadData() {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            intonationConstructionsRepository.getIntonationConstructionById(constructionId).collect { construction ->
                val level = construction?.levels?.find { it.id == levelId }
                _uiState.update { 
                    it.copy(
                        level = level,
                        isLoading = false
                    ) 
                }
            }
        }
    }
    
    /**
     * Воспроизводит эталонное аудио
     */
    fun playReferenceAudio() {
        val level = _uiState.value.level ?: return
        
        // Сначала останавливаем любое текущее воспроизведение
        stopPlayingReference()
        
        // Обновляем UI состояние
        _uiState.update { it.copy(isPlayingReference = true) }
        
        viewModelScope.launch {
            try {
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
                            _uiState.update { it.copy(isPlayingReference = false) }
                        }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isPlayingReference = false) }
                        e.printStackTrace()
                    }
                )
            } catch (e: IOException) {
                _uiState.update { it.copy(isPlayingReference = false) }
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Начинает запись аудио
     */
    fun startRecording() {
        viewModelScope.launch {
            audioRepository.startRecording().fold(
                onSuccess = { file ->
                    _uiState.update { it.copy(isRecording = true) }
                },
                onFailure = { error ->
                    // Обработка ошибки
                }
            )
        }
    }
    
    /**
     * Останавливает запись аудио
     */
    fun stopRecording() {
        viewModelScope.launch {
            audioRepository.stopRecording().fold(
                onSuccess = { file ->
                    _uiState.update { 
                        it.copy(
                            isRecording = false,
                            recordingFile = file
                        ) 
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isRecording = false) }
                    error.printStackTrace()
                }
            )
        }
    }
    
    /**
     * Воспроизводит записанное аудио
     */
    fun playRecordedAudio() {
        val file = _uiState.value.recordingFile ?: return
        
        // Сначала останавливаем любое текущее воспроизведение
        stopPlayingRecording()
        
        // Обновляем UI состояние
        _uiState.update { it.copy(isPlayingRecording = true) }
        
        viewModelScope.launch {
            audioRepository.playAudio(file).fold(
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
                        _uiState.update { it.copy(isPlayingRecording = false) }
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isPlayingRecording = false) }
                    error.printStackTrace()
                }
            )
        }
    }
    
    /**
     * Анализирует записанную интонацию
     */
    fun analyzeIntonation() {
        val file = _uiState.value.recordingFile ?: return
        
        _uiState.update { it.copy(isAnalyzing = true) }
        
        viewModelScope.launch {
            userRepository.getCurrentUser().collect { user ->
                if (user != null) {
                    audioRepository.analyzeIntonation(
                        recordingFile = file,
                        levelId = levelId,
                        constructionId = constructionId,
                        userId = user.id
                    ).collect { result ->
                        result.fold(
                            onSuccess = { practiceResult ->
                                // Обновляем прогресс пользователя
                                userRepository.updateProgress(
                                    constructionId = constructionId,
                                    levelId = levelId,
                                    score = practiceResult.score
                                ).collect { updatedUser ->
                                    _uiState.update { 
                                        it.copy(
                                            isAnalyzing = false,
                                            practiceResult = practiceResult
                                        ) 
                                    }
                                }
                            },
                            onFailure = { error ->
                                _uiState.update { it.copy(isAnalyzing = false) }
                                // Обработка ошибки
                            }
                        )
                    }
                } else {
                    // Если пользователь не авторизован, просто анализируем без сохранения прогресса
                    audioRepository.analyzeIntonation(
                        recordingFile = file,
                        levelId = levelId,
                        constructionId = constructionId,
                        userId = "guest"
                    ).collect { result ->
                        result.fold(
                            onSuccess = { practiceResult ->
                                _uiState.update { 
                                    it.copy(
                                        isAnalyzing = false,
                                        practiceResult = practiceResult
                                    ) 
                                }
                            },
                            onFailure = { error ->
                                _uiState.update { it.copy(isAnalyzing = false) }
                                // Обработка ошибки
                            }
                        )
                    }
                }
            }
        }
    }

    /**
     * Останавливает воспроизведение эталонного аудио
     */
    fun stopPlayingReference() {
        // Сначала обновляем UI, чтобы кнопка сразу изменилась
        _uiState.update { it.copy(isPlayingReference = false) }
        
        // Затем останавливаем воспроизведение
        viewModelScope.launch {
            audioRepository.stopAudio().fold(
                onSuccess = {},
                onFailure = { error ->
                    error.printStackTrace()
                }
            )
        }
    }

    /**
     * Останавливает воспроизведение записанного аудио
     */
    fun stopPlayingRecording() {
        // Сначала обновляем UI, чтобы кнопка сразу изменилась
        _uiState.update { it.copy(isPlayingRecording = false) }
        
        // Затем останавливаем воспроизведение
        viewModelScope.launch {
            audioRepository.stopAudio().fold(
                onSuccess = {},
                onFailure = { error ->
                    error.printStackTrace()
                }
            )
        }
    }
    
    /**
     * Сбрасывает результат практики
     * Необходимо вызывать после перехода на экран результатов
     */
    fun clearPracticeResult() {
        _uiState.update { it.copy(practiceResult = null) }
    }
}

/**
 * Состояние UI экрана практики
 */
data class PracticeUiState(
    val isLoading: Boolean = false,
    val level: Level? = null,
    val isPlayingReference: Boolean = false,
    val isRecording: Boolean = false,
    val recordingFile: File? = null,
    val isPlayingRecording: Boolean = false,
    val isAnalyzing: Boolean = false,
    val practiceResult: PracticeResult? = null
)

/**
 * Factory для создания ViewModel с параметрами
 */
class PracticeViewModelFactory(
    private val constructionId: String,
    private val levelId: String
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PracticeViewModel::class.java)) {
            return PracticeViewModel(
                application = RussianIntonationApp.getInstance(),
                constructionId = constructionId,
                levelId = levelId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 