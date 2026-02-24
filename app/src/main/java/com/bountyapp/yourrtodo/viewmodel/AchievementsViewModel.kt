package com.bountyapp.yourrtodo.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bountyapp.yourrtodo.data.entities.AchievementEntity
import com.bountyapp.yourrtodo.data.entities.UserStatsEntity
import com.bountyapp.yourrtodo.data.repository.AchievementRepository
import com.bountyapp.yourrtodo.model.UserStatus
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class AchievementsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AchievementRepository(application.applicationContext)

    private val _achievements = MutableLiveData<List<AchievementEntity>>()
    val achievements: LiveData<List<AchievementEntity>> = _achievements

    private val _unlockedAchievements = MutableLiveData<List<AchievementEntity>>()
    val unlockedAchievements: LiveData<List<AchievementEntity>> = _unlockedAchievements

    private val _lockedAchievements = MutableLiveData<List<AchievementEntity>>()
    val lockedAchievements: LiveData<List<AchievementEntity>> = _lockedAchievements

    private val _userStats = MutableLiveData<UserStatsEntity?>()
    val userStats: LiveData<UserStatsEntity?> = _userStats

    private val _currentStatus = MutableLiveData<UserStatus>()
    val currentStatus: LiveData<UserStatus> = _currentStatus

    private val _nextStatus = MutableLiveData<UserStatus?>()
    val nextStatus: LiveData<UserStatus?> = _nextStatus

    private val _pointsToNextStatus = MutableLiveData<Int>()
    val pointsToNextStatus: LiveData<Int> = _pointsToNextStatus

    private val _achievementProgress = MutableLiveData<Map<Long, Int>>()
    val achievementProgress: LiveData<Map<Long, Int>> = _achievementProgress

    private val _unlockedAchievementMessage = MutableLiveData<AchievementEntity?>()
    val unlockedAchievementMessage: LiveData<AchievementEntity?> = _unlockedAchievementMessage

    init {
        viewModelScope.launch {
            repository.initializeAchievements()
            repository.initializeUserStats()
            loadData()
        }
    }

    fun loadData() {
        viewModelScope.launch {
            loadDataInternal()
        }
    }

    private suspend fun loadDataInternal() {
        try {
            repository.getAllAchievements().collect { list ->
                _achievements.postValue(list)
                _unlockedAchievements.postValue(list.filter { it.isUnlocked })
                _lockedAchievements.postValue(list.filter { !it.isUnlocked })

                val progressMap = mutableMapOf<Long, Int>()
                list.forEach { achievement ->
                    progressMap[achievement.id] = repository.getProgressForAchievement(achievement)
                }
                _achievementProgress.postValue(progressMap)

                Log.d("AchievementsVM", "Loaded ${list.size} achievements")
            }

            repository.getUserStats().collect { stats ->
                _userStats.postValue(stats)
                stats?.let {
                    Log.d("AchievementsVM", "Loaded stats: totalPoints=${it.totalPoints}")
                    updateStatusInfo(it.totalPoints)
                }
            }
        } catch (e: Exception) {
            Log.e("AchievementsVM", "Error loading data", e)
        }
    }

    private suspend fun updateStatusInfo(points: Int) {
        _currentStatus.postValue(repository.getCurrentStatus())
        _nextStatus.postValue(repository.getNextStatus())
        _pointsToNextStatus.postValue(repository.getPointsToNextStatus())
    }

    fun onTaskCompleted() {
        viewModelScope.launch {
            try {
                Log.d("AchievementsVM", "=== TASK COMPLETED EVENT RECEIVED ===")

                val oldStats = _userStats.value
                Log.d("AchievementsVM", "Old stats points: ${oldStats?.totalPoints}")

                // Вызываем репозиторий и получаем список разблокированных достижений
                val newlyUnlocked = repository.onTaskCompleted()

                // Принудительно обновляем данные
                forceRefresh()

                // Показываем сообщения о новых достижениях
                if (newlyUnlocked.isNotEmpty()) {
                    Log.d("AchievementsVM", "New achievements unlocked: ${newlyUnlocked.size}")
                    newlyUnlocked.forEach { achievement ->
                        Log.d("AchievementsVM", "  - ${achievement.name} (+${achievement.pointsReward})")
                        _unlockedAchievementMessage.postValue(achievement)
                    }
                }

                val finalStats = _userStats.value
                Log.d("AchievementsVM", "Final stats points: ${finalStats?.totalPoints}")

            } catch (e: Exception) {
                Log.e("AchievementsVM", "Error in onTaskCompleted", e)
            }
        }
    }


    fun clearUnlockedMessage() {
        _unlockedAchievementMessage.postValue(null)
    }

    fun getProgressToNextStatus(): Float {
        val points = _userStats.value?.totalPoints ?: 0
        val next = _nextStatus.value
        val current = _currentStatus.value ?: UserStatus.BEGINNER

        return if (next != null) {
            val range = next.minPoints - current.minPoints
            val progress = points - current.minPoints
            if (range > 0) progress.toFloat() / range else 0f
        } else {
            1f
        }
    }

    fun forceRefresh() {
        viewModelScope.launch {
            Log.d("AchievementsVM", "Force refresh started")

            val freshStats = repository.getUserStats().firstOrNull()
            Log.d("AchievementsVM", "Fresh stats from DB: ${freshStats?.totalPoints}")

            _userStats.postValue(freshStats)

            if (freshStats != null) {
                updateStatusInfo(freshStats.totalPoints)
            }

            val freshAchievements = repository.getAllAchievements().firstOrNull() ?: emptyList()
            _achievements.postValue(freshAchievements)
            _unlockedAchievements.postValue(freshAchievements.filter { it.isUnlocked })
            _lockedAchievements.postValue(freshAchievements.filter { !it.isUnlocked })

            val progressMap = mutableMapOf<Long, Int>()
            freshAchievements.forEach { achievement ->
                progressMap[achievement.id] = repository.getProgressForAchievement(achievement)
            }
            _achievementProgress.postValue(progressMap)

            Log.d("AchievementsVM", "Force refresh completed")
        }
    }
}