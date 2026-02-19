package com.bountyapp.yourrtodo.data.entities  // ВАЖНО: именно data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val color: String,
    var isSelected: Boolean = false
)