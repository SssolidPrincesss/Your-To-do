package com.bountyapp.yourrtodo.utils

import com.bountyapp.yourrtodo.data.entities.AchievementEntity
import com.bountyapp.yourrtodo.data.entities.AchievementType
import com.bountyapp.yourrtodo.data.entities.UserStatsEntity
import com.bountyapp.yourrtodo.model.UserStatus

object GamificationUtils {

    // ============== РАСЧЕТ УРОВНЕЙ И СТАТУСОВ ==============

    /**
     * Расчет уровня пользователя на основе рейтинга
     */
    fun calculateLevel(rating: Int): Int {
        return when {
            rating < 100 -> 1
            rating < 300 -> 2
            rating < 600 -> 3
            rating < 1000 -> 4
            rating < 1500 -> 5
            rating < 2100 -> 6
            rating < 2800 -> 7
            rating < 3600 -> 8
            rating < 4500 -> 9
            else -> 10
        }
    }

    /**
     * Расчет очков до следующего уровня
     */
    fun pointsToNextLevel(currentRating: Int): Int {
        val currentLevel = calculateLevel(currentRating)
        val nextLevelThreshold = when (currentLevel) {
            1 -> 100
            2 -> 300
            3 -> 600
            4 -> 1000
            5 -> 1500
            6 -> 2100
            7 -> 2800
            8 -> 3600
            9 -> 4500
            else -> Int.MAX_VALUE
        }
        return nextLevelThreshold - currentRating
    }

    /**
     * Получение названия статуса по уровню
     */
    fun getStatusTitle(level: Int): String {
        return when (level) {
            1 -> "Новичок"
            2 -> "Стажер"
            3 -> "Опытный"
            4 -> "Профессионал"
            5 -> "Эксперт"
            6 -> "Мастер"
            7 -> "Гуру"
            8 -> "Легенда"
            9 -> "Миф"
            else -> "Бессмертный"
        }
    }

    /**
     * Получение статуса по очкам (использует UserStatus enum)
     */
    fun getUserStatusByPoints(points: Int): UserStatus {
        return UserStatus.getStatusByPoints(points)
    }

    // ============== ПРОВЕРКА ДОСТИЖЕНИЙ ==============

    /**
     * Проверяет, выполнено ли достижение
     */
    fun isAchievementCompleted(
        achievement: AchievementEntity,
        stats: UserStatsEntity
    ): Boolean {
        return when (achievement.type) {
            AchievementType.STREAK_DAYS -> stats.currentStreak >= achievement.requirement
            AchievementType.DAILY_TASKS -> stats.dailyTaskCount >= achievement.requirement
            AchievementType.TOTAL_TASKS -> stats.totalTasksCompleted >= achievement.requirement
            AchievementType.INVITE_FRIENDS -> stats.friendsInvited >= achievement.requirement
            AchievementType.PREMIUM_PURCHASE -> stats.isPremium
        }
    }

    /**
     * Получает текущий прогресс для достижения
     */
    fun getAchievementProgress(
        achievement: AchievementEntity,
        stats: UserStatsEntity
    ): Int {
        return when (achievement.type) {
            AchievementType.STREAK_DAYS -> stats.currentStreak
            AchievementType.DAILY_TASKS -> stats.dailyTaskCount
            AchievementType.TOTAL_TASKS -> stats.totalTasksCompleted
            AchievementType.INVITE_FRIENDS -> stats.friendsInvited
            AchievementType.PREMIUM_PURCHASE -> if (stats.isPremium) 1 else 0
        }.coerceAtMost(achievement.requirement)
    }

    /**
     * Получает процент выполнения достижения
     */
    fun getAchievementProgressPercentage(
        achievement: AchievementEntity,
        stats: UserStatsEntity
    ): Int {
        val progress = getAchievementProgress(achievement, stats)
        return (progress * 100 / achievement.requirement).coerceIn(0, 100)
    }

    // ============== ОБНОВЛЕНИЕ СТАТИСТИКИ ==============

    /**
     * Обновляет статистику после выполнения задачи
     */
    fun updateStatsAfterTaskCompletion(
        currentStats: UserStatsEntity,
        taskPoints: Int = 10
    ): UserStatsEntity {
        val currentDate = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()

        calendar.timeInMillis = currentStats.currentDate
        val lastDay = calendar.get(java.util.Calendar.DAY_OF_YEAR)
        val lastYear = calendar.get(java.util.Calendar.YEAR)

        calendar.timeInMillis = currentDate
        val today = calendar.get(java.util.Calendar.DAY_OF_YEAR)
        val currentYear = calendar.get(java.util.Calendar.YEAR)

        return if (today != lastDay || currentYear != lastYear) {
            // Новый день
            val newStreak = if (today == lastDay + 1 && currentYear == lastYear) {
                currentStats.currentStreak + 1
            } else {
                1
            }

            currentStats.copy(
                totalTasksCompleted = currentStats.totalTasksCompleted + 1,
                totalPoints = currentStats.totalPoints + taskPoints,
                currentStreak = newStreak,
                maxStreak = maxOf(currentStats.maxStreak, newStreak),
                dailyTaskCount = 1,
                currentDate = currentDate,
                lastActiveDate = currentDate
            )
        } else {
            // Тот же день
            currentStats.copy(
                totalTasksCompleted = currentStats.totalTasksCompleted + 1,
                totalPoints = currentStats.totalPoints + taskPoints,
                dailyTaskCount = currentStats.dailyTaskCount + 1,
                lastActiveDate = currentDate
            )
        }
    }

    /**
     * Обновляет статистику после приглашения друга
     */
    fun updateStatsAfterFriendInvited(currentStats: UserStatsEntity): UserStatsEntity {
        return currentStats.copy(
            friendsInvited = currentStats.friendsInvited + 1
        )
    }

    /**
     * Обновляет статистику после покупки премиум
     */
    fun updateStatsAfterPremiumPurchase(currentStats: UserStatsEntity): UserStatsEntity {
        return currentStats.copy(
            isPremium = true
        )
    }

    // ============== ПРОВЕРКА НОВЫХ ДОСТИЖЕНИЙ ==============

    /**
     * Проверяет, какие достижения были выполнены и возвращает их
     */
    fun checkNewlyCompletedAchievements(
        achievements: List<AchievementEntity>,
        oldStats: UserStatsEntity,
        newStats: UserStatsEntity
    ): List<AchievementEntity> {
        return achievements.filter { achievement ->
            !achievement.isUnlocked &&
                    !isAchievementCompleted(achievement, oldStats) &&
                    isAchievementCompleted(achievement, newStats)
        }
    }

    /**
     * Рассчитывает общее количество очков за выполненные достижения
     */
    fun calculateTotalPointsFromAchievements(achievements: List<AchievementEntity>): Int {
        return achievements.filter { it.isUnlocked }.sumOf { it.pointsReward }
    }

    // ============== РАСЧЕТ НАГРАД ==============

    /**
     * Рассчитывает бонус за серию дней
     */
    fun calculateStreakBonus(streakDays: Int): Int {
        return when {
            streakDays >= 100 -> 50
            streakDays >= 50 -> 25
            streakDays >= 25 -> 10
            streakDays >= 10 -> 5
            streakDays >= 5 -> 2
            else -> 0
        }
    }

    /**
     * Рассчитывает бонус за выполнение задач за день
     */
    fun calculateDailyTaskBonus(dailyTasks: Int): Int {
        return when {
            dailyTasks >= 50 -> 30
            dailyTasks >= 20 -> 15
            dailyTasks >= 10 -> 7
            dailyTasks >= 5 -> 3
            else -> 0
        }
    }

    /**
     * Получает следующее достижение по типу
     */
    fun getNextAchievement(
        achievements: List<AchievementEntity>,
        type: AchievementType,
        currentValue: Int
    ): AchievementEntity? {
        return achievements
            .filter { it.type == type && !it.isUnlocked && it.requirement > currentValue }
            .minByOrNull { it.requirement }
    }

    /**
     * Получает прогресс до следующего достижения
     */
    fun getProgressToNextAchievement(
        achievements: List<AchievementEntity>,
        type: AchievementType,
        currentValue: Int
    ): Pair<AchievementEntity?, Int> {
        val next = getNextAchievement(achievements, type, currentValue)
        return if (next != null) {
            Pair(next, (currentValue * 100 / next.requirement).coerceIn(0, 100))
        } else {
            Pair(null, 100)
        }
    }
}