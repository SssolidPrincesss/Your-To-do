package com.bountyapp.yourrtodo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bountyapp.yourrtodo.data.repository.TaskRepository
import com.bountyapp.yourrtodo.model.Task
import com.bountyapp.yourrtodo.utils.ReminderScheduler
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.UUID

class TasksViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TaskRepository(application.applicationContext)

    private val _tasks = MutableLiveData<List<Task>>(emptyList())
    val tasks: LiveData<List<Task>> = _tasks

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    // Ссылки на другие ViewModel
    private var achievementsViewModel: AchievementsViewModel? = null
    private var sharedEventViewModel: SharedEventViewModel? = null

    init {
        viewModelScope.launch {
            repository.initDefaultTasks()
            loadTasks()
        }
    }

    fun setAchievementsViewModel(viewModel: AchievementsViewModel) {
        this.achievementsViewModel = viewModel
    }

    fun setSharedEventViewModel(viewModel: SharedEventViewModel) {
        this.sharedEventViewModel = viewModel
    }

    private fun loadTasks() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getAllTasks().collect { taskList ->
                    _tasks.postValue(taskList)
                    _isLoading.postValue(false)
                }
            } catch (e: Exception) {
                _error.postValue("Ошибка загрузки задач: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }

    fun getTasks(): List<Task> = _tasks.value ?: emptyList()

    fun updateTask(updatedTask: Task) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Сначала отменяем старое напоминание (если было)
                getTaskById(updatedTask.id)?.reminderDateTime?.let {
                    ReminderScheduler.cancelReminder(getApplication(), updatedTask.id)
                }

                repository.saveTask(updatedTask)
                _isLoading.value = false

                // Планируем новое напоминание
                updatedTask.reminderDateTime?.let {
                    ReminderScheduler.scheduleReminder(getApplication(), updatedTask.id, updatedTask.title, it)
                }
            } catch (e: Exception) {
                _error.value = "Ошибка обновления задачи: ${e.message}"
                _isLoading.value = false
            }
        }
    }


    fun toggleTaskCompletion(taskId: String): Task? {
        val currentTasks = _tasks.value ?: return null
        val task = currentTasks.find { it.id == taskId } ?: return null

        val wasCompleted = task.isCompleted
        val updatedTask = task.copy(isCompleted = !task.isCompleted)

        viewModelScope.launch {
            try {
                repository.saveTask(updatedTask)

                // ЕСЛИ ЗАДАЧА ТОЛЬКО ЧТО СТАЛА ВЫПОЛНЕННОЙ
                if (!wasCompleted && updatedTask.isCompleted) {
                    val taskPoints = 5 // Базовые очки за задачу

                    achievementsViewModel?.addPoints(taskPoints, taskId)
                    sharedEventViewModel?.showTaskCompleted(task.title, taskPoints)

                    // Если задача повторяющаяся и имеет правило повторения
                    if (updatedTask.isRecurring && !updatedTask.recurrenceRule.isNullOrEmpty()) {
                        // Проверяем, существует ли уже будущая задача в этой серии
                        val existingFutureTask = _tasks.value?.find { otherTask ->
                            otherTask.seriesId == updatedTask.seriesId
                                    && otherTask.id != updatedTask.id
                                    && !otherTask.isCompleted
                        }
                        if (existingFutureTask == null) {
                            // Создаём новую задачу на будущее
                            createRecurringTask(updatedTask)
                        } // иначе – уже есть будущая задача, ничего не делаем
                    }
                }

                // Если задача была возвращена (снято выполнение)
                if (wasCompleted && !updatedTask.isCompleted) {
                    sharedEventViewModel?.showTaskUncompleted(task.title)
                }

            } catch (e: Exception) {
                _error.value = "Ошибка изменения статуса: ${e.message}"
            }
        }

        return updatedTask
    }

    private fun calculateNextDueDate(currentDate: Date?, rule: String?): Date? {
        if (currentDate == null) return null
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        when (rule) {
            "DAILY" -> calendar.add(Calendar.DAY_OF_MONTH, 1)
            "WEEKLY" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            "MONTHLY" -> calendar.add(Calendar.MONTH, 1)
            "YEARLY" -> calendar.add(Calendar.YEAR, 1)
            else -> return null
        }
        return calendar.time
    }

    private fun createRecurringTask(originalTask: Task) {
        val newDueDate = calculateNextDueDate(originalTask.dueDate, originalTask.recurrenceRule)

        // Определяем seriesId: если у исходной задачи его нет, генерируем новый
        val seriesId = originalTask.seriesId ?: UUID.randomUUID().toString()

        // Создаём новую задачу на основе исходной
        val newTask = originalTask.copy(
            id = UUID.randomUUID().toString(),
            dueDate = newDueDate,
            isCompleted = false,
            seriesId = seriesId
        )

        // Если у исходной задачи не было seriesId, обновляем её
        if (originalTask.seriesId == null) {
            originalTask.seriesId = seriesId
            viewModelScope.launch {
                repository.saveTask(originalTask)
            }
        }

        viewModelScope.launch {
            repository.saveTask(newTask)
            sharedEventViewModel?.showTaskCreated(newTask.title)
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.saveTask(task)
                _isLoading.value = false
                sharedEventViewModel?.showTaskCreated(task.title)

                // Планируем напоминание, если задано
                task.reminderDateTime?.let {
                    ReminderScheduler.scheduleReminder(getApplication(), task.id, task.title, it)
                }
            } catch (e: Exception) {
                _error.value = "Ошибка добавления задачи: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Отменяем напоминание
                getTaskById(taskId)?.reminderDateTime?.let {
                    ReminderScheduler.cancelReminder(getApplication(), taskId)
                }

                repository.deleteTask(taskId)
                _isLoading.value = false
                getTaskById(taskId)?.let {
                    sharedEventViewModel?.showTaskDeleted(it.title)
                }
            } catch (e: Exception) {
                _error.value = "Ошибка удаления задачи: ${e.message}"
                _isLoading.value = false
            }
            loadTasks()
        }
    }

    fun getTasksByCategory(categoryId: String): List<Task> {
        return if (categoryId == "all") {
            getTasks()
        } else {
            getTasks().filter { it.categoryId == categoryId }
        }
    }

    fun getTaskById(taskId: String): Task? {
        return _tasks.value?.find { it.id == taskId }
    }

    fun clearError() {
        _error.value = null
    }
}