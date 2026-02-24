package com.bountyapp.yourrtodo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bountyapp.yourrtodo.data.repository.TaskRepository
import com.bountyapp.yourrtodo.model.Task
import kotlinx.coroutines.launch

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
                repository.saveTask(updatedTask)
                _isLoading.value = false
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

                    // 1. ПРЯМОЙ ВЫЗОВ AchievementsViewModel
                    achievementsViewModel?.addPoints(taskPoints, taskId)

                    // 2. ТОЛЬКО UI-СОБЫТИЯ через SharedEventViewModel
                    sharedEventViewModel?.showTaskCompleted(task.title, taskPoints)
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

    fun addTask(task: Task) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.saveTask(task)
                _isLoading.value = false

                // UI-событие о создании задачи
                sharedEventViewModel?.showTaskCreated(task.title)

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
                // Получаем задачу для отображения в тосте
                val task = getTaskById(taskId)

                repository.deleteTask(taskId)
                _isLoading.value = false

                // UI-событие об удалении
                task?.let {
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