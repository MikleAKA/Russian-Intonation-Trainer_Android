package com.mikleaka.russianintonation.data.repository

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.mikleaka.russianintonation.data.models.IntonationPoint
import com.mikleaka.russianintonation.data.models.PracticeResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin

/**
 * Репозиторий для работы с аудио (запись, воспроизведение, анализ)
 */
class AudioRepository(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var recordingFile: File? = null
    private var isRecording = false
    private var isPlaying = false

    /**
     * Начинает запись аудио
     * @return File объект файла записи
     */
    suspend fun startRecording(): Result<File> = withContext(Dispatchers.IO) {
        if (isRecording) {
            return@withContext Result.failure(IOException("Запись уже идет"))
        }

        try {
            val outputDir = context.cacheDir
            val outputFile = File(outputDir, "recording_${UUID.randomUUID()}.mp3")
            recordingFile = outputFile

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(outputFile.absolutePath)
                
                try {
                    prepare()
                    start()
                    isRecording = true
                    Result.success(outputFile)
                } catch (e: IOException) {
                    Result.failure(e)
                }
            }
            
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Останавливает запись аудио
     * @return путь к записанному файлу
     */
    suspend fun stopRecording(): Result<File> = withContext(Dispatchers.IO) {
        if (!isRecording) {
            return@withContext Result.failure(IOException("Запись не была начата"))
        }

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            
            val file = recordingFile ?: return@withContext Result.failure(IOException("Файл записи не найден"))
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Воспроизводит аудиофайл
     * @param file файл для воспроизведения
     */
    suspend fun playAudio(file: File): Result<Unit> = withContext(Dispatchers.IO) {
        if (isPlaying) {
            stopAudio()
        }

        try {
            mediaPlayer = createNewMediaPlayer()
            
            mediaPlayer?.apply {
                setDataSource(file.absolutePath)
                prepare()
                setOnCompletionListener {
                    this@AudioRepository.isPlaying = false
                    Log.d("AudioRepository", "Воспроизведение аудио завершено по событию onCompletion")
                    release()
                    this@AudioRepository.mediaPlayer = null
                }
                start()
                this@AudioRepository.isPlaying = true
                Log.d("AudioRepository", "Воспроизведение аудио начато: ${file.absolutePath}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AudioRepository", "Ошибка при воспроизведении аудио", e)
            this@AudioRepository.isPlaying = false
            this@AudioRepository.mediaPlayer = null
            Result.failure(e)
        }
    }
    
    /**
     * Воспроизводит аудиофайл из assets
     * @param assetPath путь к файлу в assets
     */
    suspend fun playAudioFromAssets(assetPath: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (isPlaying) {
            stopAudio()
        }
        
        try {
            var afd: AssetFileDescriptor? = null
            try {
                Log.d("AudioRepository", "Попытка воспроизведения аудио из assets: $assetPath")
                afd = context.assets.openFd(assetPath)
                
                mediaPlayer = createNewMediaPlayer()
                
                mediaPlayer?.apply {
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    prepare()
                    setOnCompletionListener {
                        this@AudioRepository.isPlaying = false
                        Log.d("AudioRepository", "Воспроизведение аудио из assets завершено по событию onCompletion: $assetPath")
                        release()
                        this@AudioRepository.mediaPlayer = null
                    }
                    start()
                    this@AudioRepository.isPlaying = true
                    Log.d("AudioRepository", "Воспроизведение аудио из assets начато: $assetPath")
                }
                afd.close()
                Result.success(Unit)
            } catch (e: IOException) {
                Log.e("AudioRepository", "Ошибка при воспроизведении аудио из assets: $assetPath", e)
                afd?.close()
                this@AudioRepository.isPlaying = false
                this@AudioRepository.mediaPlayer = null
                Result.failure(e)
            }
        } catch (e: Exception) {
            Log.e("AudioRepository", "Ошибка при воспроизведении аудио из assets", e)
            this@AudioRepository.isPlaying = false
            this@AudioRepository.mediaPlayer = null
            Result.failure(e)
        }
    }

    /**
     * Останавливает воспроизведение аудио
     */
    suspend fun stopAudio(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val player = mediaPlayer
            
            mediaPlayer = null
            isPlaying = false
            
            player?.apply {
                try {
                    if (isPlaying) {
                        stop()
                    }
                } catch (e: Exception) {
                    Log.e("AudioRepository", "Ошибка при остановке воспроизведения", e)
                }
                
                try {
                    release()
                } catch (e: Exception) {
                    Log.e("AudioRepository", "Ошибка при освобождении ресурсов MediaPlayer", e)
                }
            }
            
            Log.d("AudioRepository", "Воспроизведение аудио остановлено")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AudioRepository", "Критическая ошибка при остановке воспроизведения аудио", e)
            mediaPlayer = null
            isPlaying = false
            Result.failure(e)
        }
    }
    
    /**
     * Проверяет, воспроизводится ли аудио в данный момент
     */
    fun isPlaying(): Boolean {
        return isPlaying
    }

    /**
     * Анализирует записанную интонацию и сравнивает с эталонной
     * (В реальном приложении здесь будет запрос к серверу с ML моделью)
     * @param recordingFile файл записи
     * @param levelId идентификатор уровня
     * @param constructionId идентификатор интонационной конструкции
     * @param userId идентификатор пользователя
     * @return результат анализа
     */
    fun analyzeIntonation(
        recordingFile: File,
        levelId: String,
        constructionId: String,
        userId: String
    ): Flow<Result<PracticeResult>> = flow {
        // Имитация запроса к серверу и анализа записи
        kotlinx.coroutines.delay(2000)
        
        // Генерируем фиктивные точки графика для демонстрации
        val userPoints = generateMockIntonationPoints(20)
        val referencePoints = generateMockIntonationPoints(20)
        
        // Рассчитываем фиктивную оценку (в реальном приложении будет результат ML)
        val score = (70..95).random()
        
        val result = PracticeResult(
            id = UUID.randomUUID().toString(),
            userId = userId,
            levelId = levelId,
            constructionId = constructionId,
            recordingUrl = recordingFile.absolutePath,
            score = score,
            intonationPoints = userPoints,
            referencePoints = referencePoints,
            timestamp = System.currentTimeMillis(),
            feedback = generateFeedback(score)
        )
        
        emit(Result.success(result))
    }
    
    /**
     * Генерирует мок-данные для точек интонационного графика
     */
    private fun generateMockIntonationPoints(count: Int): List<IntonationPoint> {
        return (0 until count).map { i ->
            val timeMs = (i * 100).toLong()
            val frequency = 100.0 + sin(i.toDouble() / 2) * 50 + Math.random() * 20
            val amplitude = 0.5 + cos(i.toDouble() / 3) * 0.3 + Math.random() * 0.1
            IntonationPoint(timeMs, frequency, amplitude)
        }
    }
    
    /**
     * Генерирует текстовую обратную связь на основе оценки
     */
    private fun generateFeedback(score: Int): String {
        return when {
            score >= 90 -> "Отлично! Ваша интонация очень близка к эталонной."
            score >= 75 -> "Хорошо! Есть небольшие отклонения в интонации, но в целом правильно."
            score >= 60 -> "Неплохо, но требуется дополнительная практика. Обратите внимание на повышение тона."
            else -> "Нужна дополнительная практика. Постарайтесь четче следовать образцу интонации."
        }
    }

    /**
     * Создает новый экземпляр MediaPlayer
     */
    private fun createNewMediaPlayer(): MediaPlayer {
        return MediaPlayer().apply {
            setOnErrorListener { _, what, extra ->
                Log.e("AudioRepository", "MediaPlayer ошибка: what=$what, extra=$extra")
                this@AudioRepository.isPlaying = false
                this@AudioRepository.mediaPlayer = null
                true
            }
        }
    }
} 