package com.bountyapp.yourrtodo.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
        private val dayEventsIndicator: ImageView = itemView.findViewById(R.id.day_events_indicator)
        private val dayStar: ImageView = itemView.findViewById(R.id.day_star) // Теперь ImageView

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
            dayNumber.paintFlags = 0
            dayNumber.alpha = 1f

            // Дни не из текущего месяца
            if (!day.isCurrentMonth) {
                dayNumber.setTextColor(Color.parseColor("#999999"))
                dayNumber.alpha = 0.5f
                dayStar.visibility = View.GONE
                dayEventsIndicator.visibility = View.GONE
                return
            }

            // Сбрасываем цвет для дней текущего месяца
            dayNumber.setTextColor(Color.BLACK)
            dayNumber.alpha = 1f

            // Обработка звездочек с использованием векторных изображений
            when {
                // Сегодня и выбрано - заполненная звезда
                day.isToday && isSelected -> {
                    dayStar.visibility = View.VISIBLE
                    dayStar.setImageResource(R.drawable.ic_star_filled) // Замените на вашу иконку
                    dayStar.setColorFilter(Color.parseColor("#FFC107"))
                    dayNumber.setTextColor(Color.BLACK)
                }
                // Сегодня (не выбрано) - пустая звезда
                day.isToday -> {
                    dayStar.visibility = View.VISIBLE
                    dayStar.setImageResource(R.drawable.ic_star_outline) // Замените на вашу иконку
                    dayStar.setColorFilter(Color.parseColor("#FFC107"))
                    dayNumber.setTextColor(Color.BLACK)
                }
                // Выбрано (не сегодня) - заполненная звезда
                isSelected -> {
                    dayStar.visibility = View.VISIBLE
                    dayStar.setImageResource(R.drawable.ic_star_filled) // Замените на вашу иконку
                    dayStar.setColorFilter(Color.parseColor("#FFC107"))
                    dayNumber.setTextColor(Color.BLACK)
                }
                // Обычный день
                else -> {
                    dayStar.visibility = View.GONE
                    dayNumber.setTextColor(Color.BLACK)
                }
            }

            // Волнистая линия для дней с событиями
            if (day.hasEvents) {
                dayEventsIndicator.visibility = View.VISIBLE
                dayEventsIndicator.setColorFilter(Color.RED)
            } else {
                dayEventsIndicator.visibility = View.GONE
            }
        }
    }
}