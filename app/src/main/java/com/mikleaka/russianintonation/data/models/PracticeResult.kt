package com.mikleaka.russianintonation.data.models

/**
 * Модель результата практики произношения
 *
 * @property id уникальный идентификатор результата
 * @property userId идентификатор пользователя
 * @property levelId идентификатор уровня
 * @property constructionId идентификатор интонационной конструкции
 * @property recordingUrl URL записи пользователя
 * @property score оценка соответствия интонации (0-100%)
 * @property intonationPoints точки интонационного графика пользователя
 * @property referencePoints точки эталонного интонационного графика
 * @property timestamp время выполнения упражнения
 * @property feedback текстовая обратная связь с рекомендациями
 */
data class PracticeResult(
    val id: String,
    val userId: String,
    val levelId: String,
    val constructionId: String,
    val recordingUrl: String,
    val score: Int,
    val intonationPoints: List<IntonationPoint>,
    val referencePoints: List<IntonationPoint>,
    val timestamp: Long,
    val feedback: String
)

/**
 * Точка интонационного графика
 *
 * @property timeMs временная позиция в миллисекундах
 * @property frequency частота в герцах
 * @property amplitude амплитуда
 */
data class IntonationPoint(
    val timeMs: Long,
    val frequency: Double,
    val amplitude: Double
) 