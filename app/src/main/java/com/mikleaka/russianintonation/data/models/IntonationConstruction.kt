package com.mikleaka.russianintonation.data.models

/**
 * Модель данных интонационной конструкции
 */
data class IntonationConstruction(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val levels: List<Level>
)

/**
 * Модель данных уровня в интонационной конструкции
 */
data class Level(
    val id: String,
    val title: String,
    val description: String,
    val phrase: String,
    val referenceAudioUrl: String,
    val difficulty: Difficulty
)

/**
 * Уровень сложности
 */
enum class Difficulty {
    EASY, MEDIUM, HARD
} 