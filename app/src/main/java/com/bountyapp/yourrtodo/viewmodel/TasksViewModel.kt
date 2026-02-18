package com.bountyapp.yourrtodo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bountyapp.yourrtodo.model.Task
import java.util.*

class TasksViewModel : ViewModel() {

    private val _tasks = MutableLiveData<MutableList<Task>>(mutableListOf())
    val tasks: LiveData<MutableList<Task>> = _tasks

    private val _allTasks = MutableLiveData<MutableList<Task>>(mutableListOf())
    val allTasks: LiveData<MutableList<Task>> = _allTasks

    init {
        loadInitialTasks()
    }

    private fun loadInitialTasks() {
        val initialTasks = mutableListOf(
            Task(
                id = "1",
                title = "Создать годовой отчет",
                dueDate = null,
                isCompleted = false,
                isOverdue = false,
                hasReminder = true,
                isRecurring = false,
                hasSubtasks = true,
                flagColor = "#FFC107",
                categoryId = "work"
            ),
            Task(
                id = "2",
                title = "Проверить почту",
                dueDate = null,
                isCompleted = false,
                isOverdue = false,
                hasReminder = false,
                isRecurring = true,
                hasSubtasks = false,
                flagColor = "#4CAF50",
                categoryId = "work"
            ),
            Task(
                id = "3",
                title = "Купить продукты",
                dueDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 1)
                }.time,
                isCompleted = false,
                isOverdue = false,
                hasReminder = true,
                isRecurring = false,
                hasSubtasks = false,
                flagColor = "#2196F3",
                categoryId = "shopping"
            ),
            Task(
                id = "4",
                title = "Сделать домашнее задание",
                dueDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 3)
                }.time,
                isCompleted = false,
                isOverdue = false,
                hasReminder = false,
                isRecurring = false,
                hasSubtasks = true,
                flagColor = "#9C27B0",
                categoryId = "study"
            ),
            Task(
                id = "5",
                title = "Позвонить родителям",
                dueDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, -1)
                }.time,
                isCompleted = true,
                isOverdue = false,
                hasReminder = false,
                isRecurring = false,
                hasSubtasks = false,
                flagColor = "#FF9800",
                categoryId = "personal"
            )
        )
        _tasks.value = initialTasks
    }

    fun getTasks(): List<Task> = _tasks.value ?: emptyList()

    fun updateTask(updatedTask: Task) {
        _tasks.value = _tasks.value?.map { task ->
            if (task.id == updatedTask.id) updatedTask else task
        }?.toMutableList()
    }

    fun toggleTaskCompletion(taskId: String): Task? {
        val currentTasks = _tasks.value ?: return null
        val taskIndex = currentTasks.indexOfFirst { it.id == taskId }
        if (taskIndex == -1) return null

        val task = currentTasks[taskIndex]
        val updatedTask = task.copy(isCompleted = !task.isCompleted)

        currentTasks[taskIndex] = updatedTask
        _tasks.value = currentTasks

        return updatedTask
    }

    fun addTask(task: Task) {
        _tasks.value = (_tasks.value ?: mutableListOf()).apply { add(task) }
    }

    fun deleteTask(taskId: String) {
        _tasks.value = _tasks.value?.filter { it.id != taskId }?.toMutableList()
    }

    fun getTasksByCategory(categoryId: String): List<Task> {
        return if (categoryId == "all") {
            getTasks()
        } else {
            getTasks().filter { it.categoryId == categoryId }
        }
    }
}