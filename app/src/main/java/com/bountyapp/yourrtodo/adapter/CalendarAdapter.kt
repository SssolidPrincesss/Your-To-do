package com.bountyapp.yourrtodo.adapter

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.model.CalendarDay
import java.text.SimpleDateFormat
import java.util.*

class CalendarAdapter(
    private val days: List<CalendarDay>,
    private val onDayClick: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarDayViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarDayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return CalendarDayViewHolder(view) { position ->
            // Обработчик клика из ViewHolder
            handleDayClick(position)
        }
    }

    override fun onBindViewHolder(holder: CalendarDayViewHolder, position: Int) {
        val day = days[position]
        holder.bind(day, position == selectedPosition)
    }

    override fun getItemCount() = days.size

    private fun handleDayClick(position: Int) {
        if (position < 0 || position >= days.size) return

        val previousSelected = selectedPosition
        selectedPosition = position

        if (previousSelected != -1) {
            notifyItemChanged(previousSelected)
        }
        notifyItemChanged(position)

        onDayClick(days[position])
    }

    fun updateSelection(day: CalendarDay) {
        val position = days.indexOfFirst {
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(it.date) ==
                    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(day.date)
        }
        if (position != -1) {
            val previousSelected = selectedPosition
            selectedPosition = position
            if (previousSelected != -1) notifyItemChanged(previousSelected)
            notifyItemChanged(position)
        }
    }

    fun getSelectedDay(): CalendarDay? {
        return if (selectedPosition in 0 until days.size) {
            days[selectedPosition]
        } else {
            null
        }
    }

    class CalendarDayViewHolder(
        itemView: View,
        private val onClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val dayNumber: TextView = itemView.findViewById(R.id.day_number)
        private val dayEventsIndicator: View = itemView.findViewById(R.id.day_events_indicator)
        private val dayStar: TextView = itemView.findViewById(R.id.day_star)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onClick(position)
                }
            }
        }

        fun bind(day: CalendarDay, isSelected: Boolean) {
            // Устанавливаем номер дня
            dayNumber.text = day.dayOfMonth.toString()

            // Сбрасываем все стили
            dayNumber.paintFlags = 0
            dayNumber.alpha = 1f

            // Визуальное оформление
            when {
                !day.isCurrentMonth -> {
                    // Дни другого месяца - серые
                    dayNumber.setTextColor(Color.parseColor("#999999"))
                    dayNumber.alpha = 0.5f
                    dayStar.visibility = View.GONE
                    dayEventsIndicator.visibility = View.GONE
                }
                day.isToday -> {
                    // Сегодня - пустая звезда
                    dayNumber.setTextColor(Color.BLACK)
                    dayStar.visibility = View.VISIBLE
                    dayStar.text = "☆"
                    dayStar.setTextColor(Color.parseColor("#FFC107"))

                    // Подчеркивание если есть события
                    if (day.hasEvents) {
                        dayNumber.paintFlags = dayNumber.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                        dayNumber.setTextColor(Color.RED)
                        dayEventsIndicator.visibility = View.VISIBLE
                    } else {
                        dayEventsIndicator.visibility = View.GONE
                    }
                }
                isSelected -> {
                    // Выбранный день - заполненная звезда
                    dayNumber.setTextColor(Color.BLACK)
                    dayStar.visibility = View.VISIBLE
                    dayStar.text = "★"
                    dayStar.setTextColor(Color.parseColor("#FFC107"))

                    // Подчеркивание если есть события
                    if (day.hasEvents) {
                        dayNumber.paintFlags = dayNumber.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                        dayNumber.setTextColor(Color.RED)
                        dayEventsIndicator.visibility = View.VISIBLE
                    } else {
                        dayEventsIndicator.visibility = View.GONE
                    }
                }
                else -> {
                    // Обычный день
                    dayNumber.setTextColor(Color.BLACK)
                    dayStar.visibility = View.GONE

                    // Подчеркивание если есть события
                    if (day.hasEvents) {
                        dayNumber.paintFlags = dayNumber.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                        dayNumber.setTextColor(Color.RED)
                        dayEventsIndicator.visibility = View.VISIBLE
                    } else {
                        dayEventsIndicator.visibility = View.GONE
                    }
                }
            }
        }
    }
}