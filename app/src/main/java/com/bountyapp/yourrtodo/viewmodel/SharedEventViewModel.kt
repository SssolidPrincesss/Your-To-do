package com.bountyapp.yourrtodo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SharedEventViewModel : ViewModel() {

    // Только UI-события для отображения тостов и уведомлений

    // Тосты
    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    // События выполнения задач (только для логирования, не для бизнес-логики)
    private val _taskCompletedEvent = MutableLiveData<Pair<String, Int>?>() // (title, points)
    val taskCompletedEvent: LiveData<Pair<String, Int>?> = _taskCompletedEvent

    private val _taskUncompletedEvent = MutableLiveData<String?>() // title
    val taskUncompletedEvent: LiveData<String?> = _taskUncompletedEvent

    private val _taskCreatedEvent = MutableLiveData<String?>() // title
    val taskCreatedEvent: LiveData<String?> = _taskCreatedEvent

    private val _taskDeletedEvent = MutableLiveData<String?>() // title
    val taskDeletedEvent: LiveData<String?> = _taskDeletedEvent

    private val _taskUpdatedEvent = MutableLiveData<String?>() // title
    val taskUpdatedEvent: LiveData<String?> = _taskUpdatedEvent

    // События достижений
    private val _achievementUnlockedEvent = MutableLiveData<String?>() // achievement name
    val achievementUnlockedEvent: LiveData<String?> = _achievementUnlockedEvent

    // Методы для отправки UI-событий с автоочисткой

    fun showTaskCompleted(taskTitle: String, points: Int) {
        val message = "Задача выполнена: $taskTitle (+$points ★)"
        _toastMessage.postValue(message)
        _taskCompletedEvent.postValue(Pair(taskTitle, points))

        // Автоочистка через 1 секунду
        viewModelScope.launch {
            delay(1000)
            _taskCompletedEvent.postValue(null)
            _toastMessage.postValue(null)
        }
    }

    fun showTaskUncompleted(taskTitle: String) {
        val message = "Задача возвращена: $taskTitle"
        _toastMessage.postValue(message)
        _taskUncompletedEvent.postValue(taskTitle)

        viewModelScope.launch {
            delay(1000)
            _taskUncompletedEvent.postValue(null)
            _toastMessage.postValue(null)
        }
    }

    fun showTaskCreated(taskTitle: String) {
        val message = "Задача создана: $taskTitle"
        _toastMessage.postValue(message)
        _taskCreatedEvent.postValue(taskTitle)

        viewModelScope.launch {
            delay(1000)
            _taskCreatedEvent.postValue(null)
            _toastMessage.postValue(null)
        }
    }

    fun showTaskDeleted(taskTitle: String) {
        val message = "Задача удалена: $taskTitle"
        _toastMessage.postValue(message)
        _taskDeletedEvent.postValue(taskTitle)

        viewModelScope.launch {
            delay(1000)
            _taskDeletedEvent.postValue(null)
            _toastMessage.postValue(null)
        }
    }

    fun showTaskUpdated(taskTitle: String) {
        val message = "Задача обновлена: $taskTitle"
        _toastMessage.postValue(message)
        _taskUpdatedEvent.postValue(taskTitle)

        viewModelScope.launch {
            delay(1000)
            _taskUpdatedEvent.postValue(null)
            _toastMessage.postValue(null)
        }
    }

    fun showAchievementUnlocked(achievementName: String, points: Int) {
        val message = "🏆 Достижение получено: $achievementName (+$points ★)"
        _toastMessage.postValue(message)
        _achievementUnlockedEvent.postValue(achievementName)

        viewModelScope.launch {
            delay(2000)
            _achievementUnlockedEvent.postValue(null)
            _toastMessage.postValue(null)
        }
    }

    fun showPointsEarned(points: Int, reason: String) {
        val message = "+$points ★ за $reason"
        _toastMessage.postValue(message)

        viewModelScope.launch {
            delay(1000)
            _toastMessage.postValue(null)
        }
    }
}