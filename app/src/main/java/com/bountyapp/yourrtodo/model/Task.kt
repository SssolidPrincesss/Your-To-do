package com.bountyapp.yourrtodo.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class Task(
    val id: String,
    var title: String,
    var dueDate: Date? = null,
    var isCompleted: Boolean = false,
    var hasReminder: Boolean = false,
    var isRecurring: Boolean = false,
    var hasSubtasks: Boolean = false,
    val flagColor: String = "#FFC107",
    var categoryId: String = "all",
    val isSectionHeader: Boolean = false,
    val sectionTitle: String? = null,
    var notes: String? = null,
    var reminderTime: Date? = null,
    var recurrenceRule: String? = null,
    val attachments: List<String> = emptyList(),
    var subtasks: MutableList<Subtask> = mutableListOf()
) : Parcelable {

    companion object {
        fun createSectionHeader(title: String): Task {
            return Task(
                id = "section_$title",
                title = title,
                isSectionHeader = true,
                sectionTitle = title
            )
        }
    }

    // Вычисляемое свойство для просроченности
    val isOverdue: Boolean
        get() = dueDate?.let {
            !isCompleted && it.before(
                Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0) }.time
            )
        } ?: false

    fun getDisplayDate(): String {
        if (isSectionHeader) return ""
        if (dueDate == null) return "Сегодня"

        val today = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0) }
        val taskDate = Calendar.getInstance().apply {
            time = dueDate
            set(Calendar.HOUR_OF_DAY, 0)
        }

        return when {
            isSameDay(today, taskDate) -> "Сегодня"
            isTomorrow(today, taskDate) -> "Завтра"
            else -> SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(dueDate)
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

    fun copy(
        isCompleted: Boolean = this.isCompleted,
        dueDate: Date? = this.dueDate,
        hasReminder: Boolean = this.hasReminder,
        isRecurring: Boolean = this.isRecurring,
        hasSubtasks: Boolean = this.hasSubtasks,
        notes: String? = this.notes,
        reminderTime: Date? = this.reminderTime,
        recurrenceRule: String? = this.recurrenceRule,
        subtasks: MutableList<Subtask> = this.subtasks
    ): Task {
        return Task(
            id = this.id,
            title = this.title,
            dueDate = dueDate,
            isCompleted = isCompleted,
            hasReminder = hasReminder,
            isRecurring = isRecurring,
            hasSubtasks = hasSubtasks,
            flagColor = this.flagColor,
            categoryId = this.categoryId,
            isSectionHeader = this.isSectionHeader,
            sectionTitle = this.sectionTitle,
            notes = notes,
            reminderTime = reminderTime,
            recurrenceRule = recurrenceRule,
            attachments = this.attachments,
            subtasks = subtasks
        )
    }
}