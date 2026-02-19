package com.bountyapp.yourrtodo.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "subtasks",
    foreignKeys = [ForeignKey(
        entity = TaskEntity::class,
        parentColumns = ["id"],
        childColumns = ["taskId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class SubtaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    var isCompleted: Boolean,
    val taskId: String
)