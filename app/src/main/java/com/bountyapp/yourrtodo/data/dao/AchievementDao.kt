package com.bountyapp.yourrtodo.data.dao

import androidx.room.*
import com.bountyapp.yourrtodo.data.entities.AchievementEntity
import com.bountyapp.yourrtodo.data.entities.UserStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {

    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE isUnlocked = 1")
    fun getUnlockedAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE isUnlocked = 0")
    fun getLockedAchievements(): Flow<List<AchievementEntity>>

    @Insert
    suspend fun insertAchievement(achievement: AchievementEntity)

    @Insert
    suspend fun insertAllAchievements(achievements: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    @Query("UPDATE achievements SET isUnlocked = 1, unlockedDate = :unlockedDate WHERE id = :achievementId")
    suspend fun unlockAchievement(achievementId: Long, unlockedDate: Long)

    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStats(): Flow<UserStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStatsEntity)

    @Update
    suspend fun updateUserStats(stats: UserStatsEntity)

    @Query("SELECT SUM(pointsReward) FROM achievements WHERE isUnlocked = 1")
    suspend fun getTotalPointsFromAchievements(): Int
}