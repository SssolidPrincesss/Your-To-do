package com.bountyapp.yourrtodo.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val pointsReward: Int,
    val type: AchievementType,
    val requirement: Int,
    val isUnlocked: Boolean = false,
    val unlockedDate: Long? = null,
    val iconResId: Int? = null
)

enum class AchievementType {
    STREAK_DAYS,
    DAILY_TASKS,
    TOTAL_TASKS,
    INVITE_FRIENDS,
    PREMIUM_PURCHASE
}