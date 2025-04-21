package com.mikleaka.russianintonation.data.repository

import android.content.Context
import com.mikleaka.russianintonation.data.models.Difficulty
import com.mikleaka.russianintonation.data.models.IntonationConstruction
import com.mikleaka.russianintonation.data.models.Level
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Репозиторий для работы с интонационными конструкциями
 */
class IntonationConstructionsRepository(private val context: Context) {
    
    /**
     * Получает список всех интонационных конструкций
     */
    fun getIntonationConstructions(): Flow<List<IntonationConstruction>> = flow {
        // В реальном приложении здесь будет загрузка данных из API или базы данных
        // Пока используем моки для демонстрации
        val constructions = listOf(
            IntonationConstruction(
                id = "ik1",
                title = "ИК-1",
                description = "Интонационная конструкция завершенности. Используется в повествовательных предложениях и специальных вопросах.",
                imageUrl = null,
                levels = listOf(
                    Level(
                        id = "ik1_level1",
                        title = "Это мой дом",
                        description = "Простое предложение с ИК-1",
                        phrase = "Это мой дом",
                        referenceAudioUrl = "ik1/Its_my_house/Its_my_house1.wav",
                        difficulty = Difficulty.EASY
                    ),
                    Level(
                        id = "ik1_level2",
                        title = "Скоро наступит зима",
                        description = "Предложение с ИК-1 в конце",
                        phrase = "Скоро наступит зима",
                        referenceAudioUrl = "ik1/Winter_is_coming_soon/Winter_is_coming_soon1.wav",
                        difficulty = Difficulty.MEDIUM
                    ),
                    Level(
                        id = "ik1_level3",
                        title = "Москва - столица России",
                        description = "Повествовательное предложение с ИК-1",
                        phrase = "Москва - столица России",
                        referenceAudioUrl = "ik1/Moscow_is_capital/Moscow_is_capital1.wav",
                        difficulty = Difficulty.HARD
                    )
                )
            ),
            IntonationConstruction(
                id = "ik2",
                title = "ИК-2",
                description = "Интонационная конструкция для волеизъявления. Используется в императивах и общих вопросах с вопросительным словом.",
                imageUrl = null,
                levels = listOf(
                    Level(
                        id = "ik2_level1",
                        title = "Кто пришёл?",
                        description = "Вопросительное предложение с ИК-2",
                        phrase = "Кто пришёл?",
                        referenceAudioUrl = "ik2/who_came/who_came1.wav",
                        difficulty = Difficulty.EASY
                    ),
                    Level(
                        id = "ik2_level2",
                        title = "Где ты был?",
                        description = "Вопрос с вопросительным словом",
                        phrase = "Где ты был?",
                        referenceAudioUrl = "ik2/where_were_you/where_were_you1.wav",
                        difficulty = Difficulty.MEDIUM
                    ),
                    Level(
                        id = "ik2_level3",
                        title = "Как тебя зовут?",
                        description = "Вопрос с вопросительным словом",
                        phrase = "Как тебя зовут?",
                        referenceAudioUrl = "ik2/what_is_your_name/what_is_your_name1.wav",
                        difficulty = Difficulty.HARD
                    )
                )
            ),
            IntonationConstruction(
                id = "ik3",
                title = "ИК-3",
                description = "Интонационная конструкция вопроса без вопросительного слова. Используется в общих вопросах.",
                imageUrl = null,
                levels = listOf(
                    Level(
                        id = "ik3_level1",
                        title = "Это твоя книга?",
                        description = "Общий вопрос с ИК-3",
                        phrase = "Это твоя книга?",
                        referenceAudioUrl = "ik3/is_it_your_book/is_it_your_book1.wav",
                        difficulty = Difficulty.EASY
                    ),
                    Level(
                        id = "ik3_level2",
                        title = "Ты студент?",
                        description = "Простой общий вопрос с ИК-3",
                        phrase = "Ты студент?",
                        referenceAudioUrl = "ik3/are_you_student/are_you_student1.wav",
                        difficulty = Difficulty.MEDIUM
                    ),
                    Level(
                        id = "ik3_level3",
                        title = "Вы говорите по-русски?",
                        description = "Общий вопрос о знании языка",
                        phrase = "Вы говорите по-русски?",
                        referenceAudioUrl = "ik3/do_you_speak_russian/do_you_speak_russian1.wav",
                        difficulty = Difficulty.HARD
                    )
                )
            )
        )
        
        emit(constructions)
    }
    
    /**
     * Получает конкретную интонационную конструкцию по идентификатору
     */
    fun getIntonationConstructionById(id: String): Flow<IntonationConstruction?> = flow {
        val constructions = getIntonationConstructions().collect { constructions ->
            val construction = constructions.find { it.id == id }
            emit(construction)
        }
    }
    
    /**
     * Получает полный путь к аудио-файлу
     */
    fun getAudioFilePath(relativePath: String): String {
        return "file:///android_asset/$relativePath"
    }
} 