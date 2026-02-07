package com.bountyapp.yourrtodo.model

data class Category(
    val id: String,
    val name: String,
    val color: String, // Цвет в формате "#RRGGBB"
    var isSelected: Boolean = false
)