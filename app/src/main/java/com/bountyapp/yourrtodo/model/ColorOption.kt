package com.bountyapp.yourrtodo.model

data class ColorOption(
    val colorHex: String,
    val name: String = "",
    var isSelected: Boolean = false
)