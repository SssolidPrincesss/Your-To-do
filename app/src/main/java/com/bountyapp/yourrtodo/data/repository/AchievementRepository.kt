package com.bountyapp.yourrtodo.data.repository

import android.content.Context
import android.util.Log
import com.bountyapp.yourrtodo.data.database.AppDatabase
import com.bountyapp.yourrtodo.data.entities.AchievementEntity
import com.bountyapp.yourrtodo.data.entities.AchievementType
import com.bountyapp.yourrtodo.data.entities.UserStatsEntity
import com.bountyapp.yourrtodo.model.UserStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AchievementRepository(private val context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val achievementDao = database.achievementDao()

    suspend fun initializeAchievements() {
        val achievements = achievementDao.getAllAchievements().firstOrNull()
        if (achievements.isNullOrEmpty()) {
            achievementDao.insertAllAchievements(getDefaultAchievements())
            Log.d("AchievementRepo", "Default achievements initialized")
        }
    }

    suspend fun initializeUserStats() {
        val stats = achievementDao.getUserStats().firstOrNull()
        if (stats == null) {
            val initialStats = UserStatsEntity(
                totalPoints = 0,
                currentStreak = 0,
                maxStreak = 0,
                dailyTaskCount = 0,
                totalTasksCompleted = 0,
                friendsInvited = 0,
                isPremium = false,
                currentDate = System.currentTimeMillis(),
                lastActiveDate = System.currentTimeMillis()
            )
            achievementDao.insertUserStats(initialStats)
            Log.d("AchievementRepo", "UserStats initialized with 0 points")
        }
    }

    fun getAllAchievements(): Flow<List<AchievementEntity>> = achievementDao.getAllAchievements()

    fun getUnlockedAchievements(): Flow<List<AchievementEntity>> = achievementDao.getUnlockedAchievements()

    fun getLockedAchievements(): Flow<List<AchievementEntity>> = achievementDao.getLockedAchievements()

    fun getUserStats(): Flow<UserStatsEntity?> = achievementDao.getUserStats()

    suspend fun getCurrentStatus(): UserStatus {
        val stats = achievementDao.getUserStats().firstOrNull()
        return UserStatus.getStatusByPoints(stats?.totalPoints ?: 0)
    }

    suspend fun getNextStatus(): UserStatus? {
        val currentPoints = achievementDao.getUserStats().firstOrNull()?.totalPoints ?: 0
        val currentStatus = UserStatus.getStatusByPoints(currentPoints)
        val allStatuses = UserStatus.values()
        val currentIndex = allStatuses.indexOf(currentStatus)
        return if (currentIndex < allStatuses.size - 1) allStatuses[currentIndex + 1] else null
    }

    suspend fun getPointsToNextStatus(): Int {
        val nextStatus = getNextStatus() ?: return 0
        val currentPoints = achievementDao.getUserStats().firstOrNull()?.totalPoints ?: 0
        return nextStatus.minPoints - currentPoints
    }

    suspend fun getProgressForAchievement(achievement: AchievementEntity): Int {
        val stats = achievementDao.getUserStats().firstOrNull() ?: return 0

        return when (achievement.type) {
            AchievementType.STREAK_DAYS -> stats.currentStreak
            AchievementType.DAILY_TASKS -> stats.dailyTaskCount
            AchievementType.TOTAL_TASKS -> stats.totalTasksCompleted
            AchievementType.INVITE_FRIENDS -> stats.friendsInvited
            AchievementType.PREMIUM_PURCHASE -> if (stats.isPremium) 1 else 0
        }
    }

    suspend fun onTaskCompleted(taskPoints: Int = 5): List<AchievementEntity> {
        Log.d("AchievementRepo", "========== onTaskCompleted START ==========")

        // Получаем текущую статистику
        val currentStats = achievementDao.getUserStats().firstOrNull()
            ?: run {
                Log.e("AchievementRepo", "UserStats is null!")
                initializeUserStats()
                return@onTaskCompleted emptyList()
            }

        Log.d("AchievementRepo", "Before update: points=${currentStats.totalPoints}, streak=${currentStats.currentStreak}, daily=${currentStats.dailyTaskCount}, total=${currentStats.totalTasksCompleted}")

        val currentDate = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()

        calendar.timeInMillis = currentStats.currentDate
        val lastDay = calendar.get(java.util.Calendar.DAY_OF_YEAR)
        val lastYear = calendar.get(java.util.Calendar.YEAR)

        calendar.timeInMillis = currentDate
        val today = calendar.get(java.util.Calendar.DAY_OF_YEAR)
        val currentYear = calendar.get(java.util.Calendar.YEAR)

        Log.d("AchievementRepo", "Date check: lastDay=$lastDay, lastYear=$lastYear, today=$today, currentYear=$currentYear")

        // Сохраняем старые значения для проверки достижений
        val oldStats = currentStats.copy()

        // Обновляем статистику
        val updatedStats = if (today != lastDay || currentYear != lastYear) {
            // Новый день
            val newStreak = if (today == lastDay + 1 && currentYear == lastYear) {
                currentStats.currentStreak + 1
            } else {
                1
            }

            Log.d("AchievementRepo", "New day! New streak: $newStreak")

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
            Log.d("AchievementRepo", "Same day, increasing daily count from ${currentStats.dailyTaskCount} to ${currentStats.dailyTaskCount + 1}")

            currentStats.copy(
                totalTasksCompleted = currentStats.totalTasksCompleted + 1,
                totalPoints = currentStats.totalPoints + taskPoints,
                dailyTaskCount = currentStats.dailyTaskCount + 1,
                lastActiveDate = currentDate
            )
        }

        Log.d("AchievementRepo", "After task update: points=${updatedStats.totalPoints} (+$taskPoints)")

        // Сохраняем обновленную статистику
        achievementDao.updateUserStats(updatedStats)

        // Проверяем, что сохранилось
        val savedStats = achievementDao.getUserStats().firstOrNull()
        Log.d("AchievementRepo", "Verified saved stats: points=${savedStats?.totalPoints}")

        // Проверяем достижения
        val achievements = achievementDao.getAllAchievements().firstOrNull() ?: emptyList()
        val newlyUnlocked = checkAndUpdateAchievements(achievements, oldStats, updatedStats)

        Log.d("AchievementRepo", "========== onTaskCompleted END ==========")
        return newlyUnlocked
    }

    private suspend fun checkAndUpdateAchievements(
        achievements: List<AchievementEntity>,
        oldStats: UserStatsEntity,
        newStats: UserStatsEntity
    ): List<AchievementEntity> {
        val unlockedAchievements = mutableListOf<AchievementEntity>()
        var totalPointsEarned = 0

        Log.d("AchievementRepo", "=== CHECKING FOR NEWLY UNLOCKED ACHIEVEMENTS ===")

        for (achievement in achievements) {
            if (!achievement.isUnlocked) {
                val wasCompleted = isAchievementCompleted(achievement, oldStats)
                val isNowCompleted = isAchievementCompleted(achievement, newStats)

                if (!wasCompleted && isNowCompleted) {
                    Log.d("AchievementRepo", "  ✅ NEW UNLOCK: ${achievement.name} (${achievement.requirement})")

                    // Разблокируем достижение
                    achievementDao.unlockAchievement(achievement.id, System.currentTimeMillis())
                    unlockedAchievements.add(achievement)
                    totalPointsEarned += achievement.pointsReward
                } else {
                    val progress = when (achievement.type) {
                        AchievementType.STREAK_DAYS -> newStats.currentStreak
                        AchievementType.DAILY_TASKS -> newStats.dailyTaskCount
                        AchievementType.TOTAL_TASKS -> newStats.totalTasksCompleted
                        else -> 0
                    }
                    if (progress > 0) {
                        Log.d("AchievementRepo", "  Progress for ${achievement.name}: $progress/${achievement.requirement}")
                    }
                }
            }
        }

        if (totalPointsEarned > 0) {
            Log.d("AchievementRepo", "Total points earned from new achievements: $totalPointsEarned")

            // Получаем актуальную статистику и добавляем очки за достижения
            val currentStats = achievementDao.getUserStats().firstOrNull()
            if (currentStats != null) {
                val finalStats = currentStats.copy(
                    totalPoints = currentStats.totalPoints + totalPointsEarned
                )
                achievementDao.updateUserStats(finalStats)

                Log.d("AchievementRepo", "Added $totalPointsEarned points for achievements. New total: ${finalStats.totalPoints}")

                // Проверяем финальное состояние
                val verifyStats = achievementDao.getUserStats().firstOrNull()
                Log.d("AchievementRepo", "Final verification: totalPoints=${verifyStats?.totalPoints}")
            }
        } else {
            Log.d("AchievementRepo", "No new achievements unlocked in this check")
        }

        return unlockedAchievements
    }

    private fun isAchievementCompleted(achievement: AchievementEntity, stats: UserStatsEntity): Boolean {
        return when (achievement.type) {
            AchievementType.STREAK_DAYS -> stats.currentStreak >= achievement.requirement
            AchievementType.DAILY_TASKS -> stats.dailyTaskCount >= achievement.requirement
            AchievementType.TOTAL_TASKS -> stats.totalTasksCompleted >= achievement.requirement
            AchievementType.INVITE_FRIENDS -> stats.friendsInvited >= achievement.requirement
            AchievementType.PREMIUM_PURCHASE -> stats.isPremium
        }
    }

    private fun getDefaultAchievements(): List<AchievementEntity> {
        return listOf(
            // Задач за день
            AchievementEntity(
                name = "Первые шаги",
                description = "Выполните 2 задачи за один день. Первые шаги!",
                pointsReward = 5,
                type = AchievementType.DAILY_TASKS,
                requirement = 2
            ),
            AchievementEntity(
                name = "Входим в ритм",
                description = "Выполните 5 задач за день. Входим в ритм!",
                pointsReward = 15,
                type = AchievementType.DAILY_TASKS,
                requirement = 5
            ),
            // Серии дней
            AchievementEntity(
                name = "Первый шаг",
                description = "Посещайте приложение 5 дней подряд. Путешествие начинается!",
                pointsReward = 10,
                type = AchievementType.STREAK_DAYS,
                requirement = 5
            ),
            AchievementEntity(
                name = "Привычка формируется",
                description = "Посещайте приложение 10 дней подряд. Вы строите привычку!",
                pointsReward = 25,
                type = AchievementType.STREAK_DAYS,
                requirement = 10
            ),
            AchievementEntity(
                name = "Суперсила последовательности",
                description = "Посещайте приложение 25 дней подряд. Последовательность - ваша суперсила!",
                pointsReward = 50,
                type = AchievementType.STREAK_DAYS,
                requirement = 25
            ),
            // Всего задач
            AchievementEntity(
                name = "Добро пожаловать в клуб",
                description = "Выполните всего 10 задач. Добро пожаловать в клуб!",
                pointsReward = 10,
                type = AchievementType.TOTAL_TASKS,
                requirement = 10
            )
        )
    }
}