package com.bountyapp.yourrtodo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.adapter.CalendarAdapter
import com.bountyapp.yourrtodo.adapter.CalendarEventsAdapter
import com.bountyapp.yourrtodo.model.CalendarDay
import com.bountyapp.yourrtodo.model.CalendarEvent
import java.text.SimpleDateFormat
import java.util.*

class FragmentCalendar : Fragment() {

    private lateinit var monthYearText: TextView
    private lateinit var daysRecyclerView: RecyclerView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var eventsAdapter: CalendarEventsAdapter

    private val calendar = Calendar.getInstance()
    private var currentMonth = calendar.get(Calendar.MONTH)
    private var currentYear = calendar.get(Calendar.YEAR)

    private val days = mutableListOf<CalendarDay>()
    private val allEvents = mutableListOf<CalendarEvent>() // Все события
    private var selectedDay: CalendarDay? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        // Инициализация view
        monthYearText = view.findViewById(R.id.month_year_text)
        daysRecyclerView = view.findViewById(R.id.days_recycler_view)
        eventsRecyclerView = view.findViewById(R.id.events_recycler_view)

        // Настройка макетов
        daysRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
        eventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Генерация тестовых событий (один раз)
        generateTestEvents()

        // Генерация календаря для текущего месяца
        generateCalendarData()

        // Настройка адаптеров
        setupAdapters()

        // Обновление отображения месяца
        updateMonthYearText()

        // Настройка обработчиков кнопок
        setupNavigationButtons(view)

        return view
    }

    private fun generateCalendarData() {
        days.clear()

        val tempCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        // Получаем первый день месяца
        val firstDayOfMonth = tempCalendar.get(Calendar.DAY_OF_WEEK)

        // Определяем смещение для первого дня недели (понедельник = 1)
        val offset = when (firstDayOfMonth) {
            Calendar.SUNDAY -> 6
            else -> firstDayOfMonth - 2
        }

        // Добавляем дни предыдущего месяца
        tempCalendar.add(Calendar.DAY_OF_MONTH, -offset)
        for (i in 0 until offset) {
            val date = tempCalendar.time
            days.add(CalendarDay(
                date = date,
                dayOfMonth = tempCalendar.get(Calendar.DAY_OF_MONTH),
                isCurrentMonth = false,
                isToday = isToday(date),
                hasEvents = hasEventsOnDate(date)
            ))
            tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Добавляем дни текущего месяца
        tempCalendar.set(Calendar.YEAR, currentYear)
        tempCalendar.set(Calendar.MONTH, currentMonth)
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1)

        val daysInMonth = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..daysInMonth) {
            val date = tempCalendar.time
            days.add(CalendarDay(
                date = date,
                dayOfMonth = i,
                isCurrentMonth = true,
                isToday = isToday(date),
                hasEvents = hasEventsOnDate(date)
            ))
            tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Добавляем дни следующего месяца чтобы заполнить сетку
        val totalCells = 42 // 6 недель * 7 дней
        val remainingCells = totalCells - days.size
        for (i in 0 until remainingCells) {
            val date = tempCalendar.time
            days.add(CalendarDay(
                date = date,
                dayOfMonth = tempCalendar.get(Calendar.DAY_OF_MONTH),
                isCurrentMonth = false,
                isToday = isToday(date),
                hasEvents = hasEventsOnDate(date)
            ))
            tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun isToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val checkDate = Calendar.getInstance().apply { time = date }
        return today.get(Calendar.YEAR) == checkDate.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == checkDate.get(Calendar.MONTH) &&
                today.get(Calendar.DAY_OF_MONTH) == checkDate.get(Calendar.DAY_OF_MONTH)
    }

    private fun hasEventsOnDate(date: Date): Boolean {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val checkDateStr = dateFormat.format(date)
        return allEvents.any { dateFormat.format(it.date) == checkDateStr }
    }

    private fun generateTestEvents() {
        allEvents.clear()

        // Тестовые события на разные даты
        val calendar = Calendar.getInstance()

        // Сегодня
        allEvents.add(CalendarEvent(
            id = "1",
            title = "Составить годовой отчет",
            date = calendar.time,
            categoryColor = "#4CAF50"
        ))

        // Завтра
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        allEvents.add(CalendarEvent(
            id = "2",
            title = "Купить продукты",
            date = calendar.time,
            categoryColor = "#FF9800"
        ))

        // Послезавтра
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        allEvents.add(CalendarEvent(
            id = "3",
            title = "Реферат",
            date = calendar.time,
            categoryColor = "#9C27B0"
        ))

        // События в следующем месяце
        calendar.add(Calendar.MONTH, 1)
        calendar.set(Calendar.DAY_OF_MONTH, 5)
        allEvents.add(CalendarEvent(
            id = "4",
            title = "Встреча с командой",
            date = calendar.time,
            categoryColor = "#2196F3"
        ))

        calendar.set(Calendar.DAY_OF_MONTH, 15)
        allEvents.add(CalendarEvent(
            id = "5",
            title = "Оплатить счета",
            date = calendar.time,
            categoryColor = "#FF5722"
        ))
    }

    private fun setupAdapters() {
        calendarAdapter = CalendarAdapter(days) { selectedDay ->
            this.selectedDay = selectedDay

            // Обновляем выбранный день в адаптере
            calendarAdapter.updateSelection(selectedDay)

            // Фильтруем события для выбранного дня
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val selectedDateStr = dateFormat.format(selectedDay.date)
            val dayEvents = allEvents.filter {
                dateFormat.format(it.date) == selectedDateStr
            }

            // Обновляем список событий
            eventsAdapter = CalendarEventsAdapter(dayEvents)
            eventsRecyclerView.adapter = eventsAdapter

            // Прокручиваем к началу списка событий
            eventsRecyclerView.scrollToPosition(0)
        }

        daysRecyclerView.adapter = calendarAdapter

        // Выбираем сегодня по умолчанию, если есть в этом месяце
        val todayInMonth = days.firstOrNull { it.isToday && it.isCurrentMonth }
        if (todayInMonth != null) {
            selectedDay = todayInMonth
            calendarAdapter.updateSelection(todayInMonth)

            // Показываем события на сегодня
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val todayEvents = allEvents.filter {
                dateFormat.format(it.date) == dateFormat.format(todayInMonth.date)
            }
            eventsAdapter = CalendarEventsAdapter(todayEvents)
        } else {
            // Иначе показываем пустой список
            eventsAdapter = CalendarEventsAdapter(emptyList())
        }

        eventsRecyclerView.adapter = eventsAdapter
    }

    private fun updateMonthYearText() {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        calendar.set(Calendar.YEAR, currentYear)
        calendar.set(Calendar.MONTH, currentMonth)
        monthYearText.text = dateFormat.format(calendar.time)
    }

    private fun setupNavigationButtons(view: View) {
        view.findViewById<TextView>(R.id.prev_month).setOnClickListener {
            changeMonth(-1)
        }

        view.findViewById<TextView>(R.id.next_month).setOnClickListener {
            changeMonth(1)
        }
    }

    private fun changeMonth(delta: Int) {
        // Обновляем текущий месяц и год
        calendar.add(Calendar.MONTH, delta)
        currentMonth = calendar.get(Calendar.MONTH)
        currentYear = calendar.get(Calendar.YEAR)

        // Генерируем данные для нового месяца
        generateCalendarData()

        // Обновляем адаптер
        calendarAdapter = CalendarAdapter(days) { selectedDay ->
            this.selectedDay = selectedDay
            calendarAdapter.updateSelection(selectedDay)

            // Фильтруем события для выбранного дня
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val selectedDateStr = dateFormat.format(selectedDay.date)
            val dayEvents = allEvents.filter {
                dateFormat.format(it.date) == selectedDateStr
            }

            eventsAdapter = CalendarEventsAdapter(dayEvents)
            eventsRecyclerView.adapter = eventsAdapter
        }

        daysRecyclerView.adapter = calendarAdapter

        // Сбрасываем выбранный день
        selectedDay = null

        // Обновляем заголовок месяца
        updateMonthYearText()

        // Очищаем список событий
        eventsAdapter = CalendarEventsAdapter(emptyList())
        eventsRecyclerView.adapter = eventsAdapter
    }
}