package com.bountyapp.yourrtodo.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val pointsReward: Int,
    val type: AchievementType,
    val requirement: Int, // Например: 5 дней, 10 задач и т.д.
    val isUnlocked: Boolean = false,
    val unlockedDate: Long? = null,
    val iconResId: Int? = null
)

enum class AchievementType {
    STREAK_DAYS,        // Серия дней
    DAILY_TASKS,        // Задач за день
    TOTAL_TASKS,        // Всего задач
    INVITE_FRIENDS,     // Приглашено друзей
    PREMIUM_PURCHASE    // Покупка премиум
}