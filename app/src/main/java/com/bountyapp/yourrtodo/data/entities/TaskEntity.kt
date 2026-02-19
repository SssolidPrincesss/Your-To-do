package com.bountyapp.yourrtodo.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val dueDate: Date?,
    var isCompleted: Boolean,
    val isOverdue: Boolean,
    val hasReminder: Boolean,
    val isRecurring: Boolean,
    val hasSubtasks: Boolean,
    val flagColor: String,
    val categoryId: String,
    val notes: String?,
    val reminderTime: Date?,
    val recurrenceRule: String?
)