package com.mikleaka.russianintonation.data.models

/**
 * Модель пользователя приложения
 *
 * @property id уникальный идентификатор пользователя
 * @property username имя пользователя
 * @property email электронная почта пользователя
 * @property progress прогресс пользователя по интонационным конструкциям
 * @property achievements достижения пользователя
 */
data class User(
    val id: String,
    val username: String,
    val email: String,
    val progress: Map<String, ConstructionProgress> = emptyMap(),
    val achievements: List<Achievement> = emptyList()
)

/**
 * Модель прогресса пользователя по конкретной интонационной конструкции
 *
 * @property constructionId идентификатор интонационной конструкции
 * @property completedLevels список выполненных уровней
 * @property averageScore средний балл по данной интонационной конструкции
 */
data class ConstructionProgress(
    val constructionId: String,
    val completedLevels: List<String>,
    val averageScore: Float
)

/**
 * Модель достижения пользователя
 *
 * @property id уникальный идентификатор достижения
 * @property title название достижения
 * @property description описание достижения
 * @property iconUrl URL иконки достижения
 * @property isUnlocked разблокировано ли достижение
 * @property unlockedAt дата разблокировки достижения
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconUrl: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
) 