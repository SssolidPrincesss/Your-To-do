package com.bountyapp.yourrtodo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bountyapp.yourrtodo.model.Task

class SharedEventViewModel : ViewModel() {

    // Для передачи всей задачи
    private val _taskCompletedEvent = MutableLiveData<Task?>()
    val taskCompletedEvent: LiveData<Task?> = _taskCompletedEvent

    // ДОБАВЬТЕ ЭТО - для передачи только очков
    private val _taskPointsEvent = MutableLiveData<Int?>()
    val taskPointsEvent: LiveData<Int?> = _taskPointsEvent

    private val _achievementUnlockedEvent = MutableLiveData<String?>()
    val achievementUnlockedEvent: LiveData<String?> = _achievementUnlockedEvent

    private val _taskUpdatedEvent = MutableLiveData<Task?>()
    val taskUpdatedEvent: LiveData<Task?> = _taskUpdatedEvent

    private val _taskCreatedEvent = MutableLiveData<Task?>()
    val taskCreatedEvent: LiveData<Task?> = _taskCreatedEvent

    // Существующий метод для задачи
    fun onTaskCompleted(task: Task) {
        _taskCompletedEvent.value = task
    }

    // НОВЫЙ метод для очков
    fun onTaskPointsEarned(points: Int) {
        _taskPointsEvent.value = points
    }

    fun onAchievementUnlocked(achievementName: String) {
        _achievementUnlockedEvent.value = achievementName
    }

    fun onTaskUpdated(task: Task) {
        _taskUpdatedEvent.value = task
    }

    fun onTaskCreated(task: Task) {
        _taskCreatedEvent.value = task
    }

    fun clearTaskCompletedEvent() {
        _taskCompletedEvent.value = null
    }

    fun clearTaskPointsEvent() {
        _taskPointsEvent.value = null
    }

    fun clearAchievementUnlockedEvent() {
        _achievementUnlockedEvent.value = null
    }

    fun clearTaskUpdatedEvent() {
        _taskUpdatedEvent.value = null
    }

    fun clearTaskCreatedEvent() {
        _taskCreatedEvent.value = null
    }
}