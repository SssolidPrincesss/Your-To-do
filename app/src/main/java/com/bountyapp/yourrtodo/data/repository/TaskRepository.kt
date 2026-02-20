package com.bountyapp.yourrtodo.data.repository

import android.content.Context
import com.bountyapp.yourrtodo.data.database.AppDatabase
import com.bountyapp.yourrtodo.data.entities.TaskEntity
import com.bountyapp.yourrtodo.data.entities.SubtaskEntity
import com.bountyapp.yourrtodo.model.Subtask
import com.bountyapp.yourrtodo.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

class TaskRepository(context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val taskDao = database.taskDao()
    private val subtaskDao = database.subtaskDao()

    fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { taskEntities ->
            taskEntities.map { taskEntity ->
                val subtasks = subtaskDao.getSubtasksList(taskEntity.id)
                    .map { subtaskEntity ->
                        Subtask(
                            id = subtaskEntity.id,
                            title = subtaskEntity.title,
                            isCompleted = subtaskEntity.isCompleted,
                            taskId = subtaskEntity.taskId
                        )
                    }

                Task(
                    id = taskEntity.id,
                    title = taskEntity.title,
                    dueDate = taskEntity.dueDate,
                    isCompleted = taskEntity.isCompleted,
                    isOverdue = taskEntity.isOverdue,
                    hasReminder = taskEntity.hasReminder,
                    isRecurring = taskEntity.isRecurring,
                    hasSubtasks = taskEntity.hasSubtasks,
                    flagColor = taskEntity.flagColor,
                    categoryId = taskEntity.categoryId,
                    notes = taskEntity.notes,
                    reminderTime = taskEntity.reminderTime,
                    recurrenceRule = taskEntity.recurrenceRule,
                    subtasks = subtasks.toMutableList()
                )
            }
        }
    }

    suspend fun saveTask(task: Task) {
        // Сохраняем задачу
        taskDao.insertTask(
            TaskEntity(
                id = task.id,
                title = task.title,
                dueDate = task.dueDate,
                isCompleted = task.isCompleted,
                isOverdue = task.isOverdue,
                hasReminder = task.hasReminder,
                isRecurring = task.isRecurring,
                hasSubtasks = task.hasSubtasks,
                flagColor = task.flagColor,
                categoryId = task.categoryId,
                notes = task.notes,
                reminderTime = task.reminderTime,
                recurrenceRule = task.recurrenceRule
            )
        )

        // Обновляем подзадачи
        subtaskDao.deleteSubtasksForTask(task.id)
        task.subtasks.forEach { subtask ->
            subtaskDao.insertSubtask(
                SubtaskEntity(
                    id = subtask.id,
                    title = subtask.title,
                    isCompleted = subtask.isCompleted,
                    taskId = subtask.taskId
                )
            )
        }
    }

    suspend fun deleteTask(taskId: String) {
        taskDao.getTaskById(taskId)?.let { taskEntity ->
            subtaskDao.deleteSubtasksForTask(taskId)
            taskDao.deleteTask(taskEntity)
        }
    }

    suspend fun initDefaultTasks() {
        val tasks = taskDao.getTasksList()
        if (tasks.isEmpty()) {
            val calendar = Calendar.getInstance()

            // Очищаем текущее время для корректного сравнения дат
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            // Задача для категории "Работа" (work)
            val task1 = Task(
                id = "1",
                title = "Создать годовой отчет",
                dueDate = null,  // сегодня
                isCompleted = false,
                isOverdue = false,
                hasReminder = true,
                isRecurring = false,
                hasSubtasks = true,
                flagColor = "#FFC107",
                categoryId = "work",  // Важно! Привязка к категории "Работа"
                notes = "Подготовить отчет за прошлый год",
                reminderTime = null,
                recurrenceRule = null,
                subtasks = mutableListOf(
                    Subtask(id = "sub1", title = "Собрать данные", isCompleted = false, taskId = "1"),
                    Subtask(id = "sub2", title = "Подготовить презентацию", isCompleted = false, taskId = "1"),
                    Subtask(id = "sub3", title = "Написать отчет", isCompleted = false, taskId = "1")
                )
            )

            val task2 = Task(
                id = "2",
                title = "Проверить почту",
                dueDate = null,  // сегодня
                isCompleted = false,
                isOverdue = false,
                hasReminder = false,
                isRecurring = true,
                hasSubtasks = false,
                flagColor = "#4CAF50",
                categoryId = "work",  // Важно! Привязка к категории "Работа"
                notes = "Ежедневная проверка",
                reminderTime = null,
                recurrenceRule = "DAILY",
                subtasks = mutableListOf()
            )

            // Задача для категории "Покупки" (shopping)
            val task3 = Task(
                id = "3",
                title = "Купить продукты",
                dueDate = calendar.apply {
                    add(Calendar.DAY_OF_MONTH, 1)  // завтра
                }.time,
                isCompleted = false,
                isOverdue = false,
                hasReminder = true,
                isRecurring = false,
                hasSubtasks = true,
                flagColor = "#2196F3",
                categoryId = "shopping",  // Важно! Привязка к категории "Покупки"
                notes = "Список покупок",
                reminderTime = null,
                recurrenceRule = null,
                subtasks = mutableListOf(
                    Subtask(id = "sub4", title = "Молоко", isCompleted = false, taskId = "3"),
                    Subtask(id = "sub5", title = "Хлеб", isCompleted = false, taskId = "3"),
                    Subtask(id = "sub6", title = "Яйца", isCompleted = false, taskId = "3")
                )
            )

            // Задача для категории "Учеба" (study)
            val task4 = Task(
                id = "4",
                title = "Сделать домашнее задание",
                dueDate = calendar.apply {
                    add(Calendar.DAY_OF_MONTH, 2)  // через 2 дня
                }.time,
                isCompleted = false,
                isOverdue = false,
                hasReminder = false,
                isRecurring = false,
                hasSubtasks = true,
                flagColor = "#9C27B0",
                categoryId = "study",  // Важно! Привязка к категории "Учеба"
                notes = "Подготовиться к экзаменам",
                reminderTime = null,
                recurrenceRule = null,
                subtasks = mutableListOf(
                    Subtask(id = "sub7", title = "Математика", isCompleted = false, taskId = "4"),
                    Subtask(id = "sub8", title = "Физика", isCompleted = false, taskId = "4")
                )
            )

            // Задача для категории "Личное" (personal) - выполнена
            val task5 = Task(
                id = "5",
                title = "Позвонить родителям",
                dueDate = calendar.apply {
                    add(Calendar.DAY_OF_MONTH, -1)  // вчера (просрочено)
                }.time,
                isCompleted = true,  // выполнена
                isOverdue = false,
                hasReminder = false,
                isRecurring = false,
                hasSubtasks = false,
                flagColor = "#FF9800",
                categoryId = "personal",  // Важно! Привязка к категории "Личное"
                notes = "Поздравить с праздником",
                reminderTime = null,
                recurrenceRule = null,
                subtasks = mutableListOf()
            )

            listOf(task1, task2, task3, task4, task5).forEach { saveTask(it) }
        }
    }
}