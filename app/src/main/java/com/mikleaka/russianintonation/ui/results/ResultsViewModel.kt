package com.mikleaka.russianintonation.ui.results

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mikleaka.russianintonation.data.models.PracticeResult
import com.mikleaka.russianintonation.data.repository.AudioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel для экрана результатов
 */
class ResultsViewModel(
    application: Application,
    private val resultId: String
) : AndroidViewModel(application) {
    
    private val audioRepository = AudioRepository(application)
    
    private val _uiState = MutableStateFlow(ResultsUiState())
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    /**
     * Загружает данные для экрана результатов
     */
    private fun loadData() {
        _uiState.update { it.copy(isLoading = true) }
        
        // В реальном приложении здесь будет запрос к API или БД
        // Для MVP просто создаем фиктивный результат
        val mockResult = createMockResult()
        
        _uiState.update { 
            it.copy(
                practiceResult = mockResult,
                isLoading = false
            ) 
        }
    }
    
    /**
     * Воспроизводит записанное аудио
     */
    fun playRecordedAudio() {
        val recordingUrl = _uiState.value.practiceResult?.recordingUrl ?: return
        
        // Если уже воспроизводится, останавливаем
        if (_uiState.value.isPlayingRecording) {
            stopPlayingRecording()
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isPlayingRecording = true) }
            
            // В реальном приложении здесь будет воспроизведение аудио
            // Для MVP просто имитируем воспроизведение
            kotlinx.coroutines.delay(3000)
            
            _uiState.update { it.copy(isPlayingRecording = false) }
        }
    }
    
    /**
     * Останавливает воспроизведение аудио
     */
    private fun stopPlayingRecording() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPlayingRecording = false) }
        }
    }
    
    /**
     * Создает фиктивный результат для демонстрации
     */
    private fun createMockResult(): PracticeResult {
        // Генерируем фиктивные точки графика
        val userPoints = generateMockIntonationPoints(20, jitter = 0.2)
        val referencePoints = generateMockIntonationPoints(20, jitter = 0.0)
        
        return PracticeResult(
            id = resultId,
            userId = "user1",
            levelId = "level1",
            constructionId = "ik1",
            recordingUrl = "mock_recording.mp3",
            score = 85,
            intonationPoints = userPoints,
            referencePoints = referencePoints,
            timestamp = System.currentTimeMillis(),
            feedback = "Хорошо! Есть небольшие отклонения в интонации, но в целом правильно. Обратите внимание на повышение тона в середине фразы."
        )
    }
    
    /**
     * Генерирует фиктивные точки интонационного графика
     */
    private fun generateMockIntonationPoints(count: Int, jitter: Double): List<com.mikleaka.russianintonation.data.models.IntonationPoint> {
        return (0 until count).map { i ->
            val timeMs = (i * 100).toLong()
            val frequency = 100.0 + Math.sin(i.toDouble() / 2) * 50 + Math.random() * 20 * jitter
            val amplitude = 0.5 + Math.cos(i.toDouble() / 3) * 0.3 + Math.random() * 0.1 * jitter
            com.mikleaka.russianintonation.data.models.IntonationPoint(timeMs, frequency, amplitude)
        }
    }
}

/**
 * Состояние UI экрана результатов
 */
data class ResultsUiState(
    val isLoading: Boolean = false,
    val practiceResult: PracticeResult? = null,
    val isPlayingRecording: Boolean = false
)

/**
 * Factory для создания ViewModel с параметрами
 */
class ResultsViewModelFactory(
    private val resultId: String
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ResultsViewModel::class.java)) {
            return ResultsViewModel(
                application = Application(),
                resultId = resultId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 