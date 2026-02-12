package com.bountyapp.yourrtodo.model

import java.util.*

data class CalendarEvent(
    val id: String,
    val title: String,
    val date: Date,
    val categoryColor: String,
    val isAllDay: Boolean = false,
    val startTime: Date? = null,
    val endTime: Date? = null
)

data class CalendarDay(
    val date: Date,
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean = false,
    val hasEvents: Boolean = false,
    val events: List<CalendarEvent> = emptyList()
)