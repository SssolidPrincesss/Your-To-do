package com.bountyapp.yourrtodo.model

import java.text.SimpleDateFormat
import java.util.*

data class Task(
    val id: String,
    val title: String,
    val dueDate: Date?, // null = сегодня
    var isCompleted: Boolean,
    val isOverdue: Boolean = false,
    val hasReminder: Boolean = false,
    val isRecurring: Boolean = false,
    val hasSubtasks: Boolean = false,
    val flagColor: String = "#FFC107",
    val categoryId: String = "all", // Добавляем привязку к категории
    val isSectionHeader: Boolean = false,
    val sectionTitle: String? = null
) {
    companion object {
        fun createSectionHeader(title: String): Task {
            return Task(
                id = "section_$title",
                title = title,
                dueDate = null,
                isCompleted = false,
                isSectionHeader = true,
                sectionTitle = title
            )
        }
    }

    fun getDisplayDate(): String {
        if (isSectionHeader) return ""
        if (dueDate == null) return "" // Сегодня - без даты

        val today = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0) }
        val taskDate = Calendar.getInstance().apply {
            time = dueDate
            set(Calendar.HOUR_OF_DAY, 0)
        }

        return when {
            isSameDay(today, taskDate) -> "Сегодня"
            isTomorrow(today, taskDate) -> "Завтра"
            else -> SimpleDateFormat("dd.MM", Locale.getDefault()).format(dueDate)
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isTomorrow(cal1: Calendar, cal2: Calendar): Boolean {
        val tomorrow = cal1.clone() as Calendar
        tomorrow.add(Calendar.DAY_OF_MONTH, 1)
        return isSameDay(tomorrow, cal2)
    }

    fun calculatePoints(): Int {
        var points = 10
        if (hasReminder) points += 5
        if (isRecurring) points += 3
        if (hasSubtasks) points += 7
        if (isOverdue) points -= 3
        return points
    }

    // Метод copy должен быть аккуратен с isCompleted
    fun copy(
        isCompleted: Boolean = this.isCompleted
    ): Task {
        return Task(
            id,
            title,
            dueDate,
            isCompleted, // <-- Ключевое поле
            isOverdue,
            hasReminder,
            isRecurring,
            hasSubtasks,
            flagColor,
            categoryId,
            isSectionHeader,
            sectionTitle
        )
    }
}