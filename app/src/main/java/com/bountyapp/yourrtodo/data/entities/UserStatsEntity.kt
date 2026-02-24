package com.bountyapp.yourrtodo.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey
    val id: Int = 1,
    var totalTasksCompleted: Int = 0,
    var totalPoints: Int = 0,
    var currentStreak: Int = 0,
    var maxStreak: Int = 0,
    var lastActiveDate: Long = System.currentTimeMillis(),
    var dailyTaskCount: Int = 0,
    var currentDate: Long = System.currentTimeMillis(),
    var friendsInvited: Int = 0,
    var isPremium: Boolean = false
)