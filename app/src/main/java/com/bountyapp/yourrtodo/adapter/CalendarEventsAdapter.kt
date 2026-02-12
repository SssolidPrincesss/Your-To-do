package com.bountyapp.yourrtodo.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.model.CalendarEvent
import java.text.SimpleDateFormat
import java.util.*

class CalendarEventsAdapter(
    private val events: List<CalendarEvent>
) : RecyclerView.Adapter<CalendarEventsAdapter.CalendarEventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarEventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_event, parent, false)
        return CalendarEventViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarEventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount() = events.size

    class CalendarEventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eventTitle: TextView = itemView.findViewById(R.id.event_title)
        private val eventTime: TextView = itemView.findViewById(R.id.event_time)
        private val colorIndicator: View = itemView.findViewById(R.id.color_indicator)

        fun bind(event: CalendarEvent) {
            eventTitle.text = event.title

            // Устанавливаем цвет категории
            colorIndicator.setBackgroundColor(Color.parseColor(event.categoryColor))

            // Форматируем время
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())

            eventTime.text = if (event.isAllDay) {
                "Весь день"
            } else {
                val start = event.startTime?.let { timeFormat.format(it) } ?: ""
                val end = event.endTime?.let { timeFormat.format(it) } ?: ""
                "$start - $end"
            }
        }
    }
}