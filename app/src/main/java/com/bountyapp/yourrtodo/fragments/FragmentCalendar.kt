package com.bountyapp.yourrtodo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.adapter.CalendarAdapter
import com.bountyapp.yourrtodo.adapter.CalendarEventsAdapter
import com.bountyapp.yourrtodo.model.CalendarDay
import com.bountyapp.yourrtodo.model.CalendarEvent
import com.bountyapp.yourrtodo.model.Category
import com.bountyapp.yourrtodo.model.Task
import com.bountyapp.yourrtodo.viewmodel.CategoriesViewModel
import com.bountyapp.yourrtodo.viewmodel.TasksViewModel
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

    private val tasksViewModel: TasksViewModel by activityViewModels()
    private val categoriesViewModel: CategoriesViewModel by activityViewModels()

    private var tasksMap: Map<String, List<Task>> = emptyMap()
    private var categoriesMap: Map<String, Category> = emptyMap()
    private var selectedDate: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        monthYearText = view.findViewById(R.id.month_year_text)
        daysRecyclerView = view.findViewById(R.id.days_recycler_view)
        eventsRecyclerView = view.findViewById(R.id.events_recycler_view)

        daysRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
        eventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        setupAdapters()
        setupNavigationButtons(view)
        updateMonthYearText()

        observeViewModels()

        return view
    }

    private fun observeViewModels() {
        categoriesViewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoriesMap = categories.associateBy { it.id }
            updateCalendar()
        }

        tasksViewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            val map = mutableMapOf<String, MutableList<Task>>()
            tasks.forEach { task ->
                task.dueDate?.let { date ->
                    val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(date)
                    map.getOrPut(dateStr) { mutableListOf() }.add(task)
                }
            }
            tasksMap = map
            updateCalendar()
        }
    }

    private fun updateCalendar() {
        val previousSelectedDate = selectedDate
        val newDays = generateCalendarData()
        calendarAdapter.updateDays(newDays)

        if (previousSelectedDate != null) {
            val matchingDay = newDays.find { isSameDay(it.date, previousSelectedDate) && it.isCurrentMonth }
            if (matchingDay != null) {
                calendarAdapter.updateSelection(matchingDay)
                selectedDate = matchingDay.date
                showEventsForDay(matchingDay)
            } else {
                selectedDate = null
                eventsAdapter = CalendarEventsAdapter(emptyList())
                eventsRecyclerView.adapter = eventsAdapter
            }
        } else {
            val today = Calendar.getInstance().time
            val todayDay = newDays.find { isSameDay(it.date, today) && it.isCurrentMonth }
            if (todayDay != null) {
                calendarAdapter.updateSelection(todayDay)
                selectedDate = todayDay.date
                showEventsForDay(todayDay)
            } else {
                eventsAdapter = CalendarEventsAdapter(emptyList())
                eventsRecyclerView.adapter = eventsAdapter
            }
        }
    }

    private fun generateCalendarData(): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val tempCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        val firstDayOfMonth = tempCalendar.get(Calendar.DAY_OF_WEEK)
        val offset = when (firstDayOfMonth) {
            Calendar.SUNDAY -> 6
            else -> firstDayOfMonth - 2
        }

        tempCalendar.add(Calendar.DAY_OF_MONTH, -offset)
        repeat(offset) {
            val date = tempCalendar.time
            val events = getEventsForDate(date)
            days.add(createCalendarDay(tempCalendar, date, isCurrentMonth = false, events))
            tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        tempCalendar.set(Calendar.YEAR, currentYear)
        tempCalendar.set(Calendar.MONTH, currentMonth)
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val daysInMonth = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        repeat(daysInMonth) {
            val date = tempCalendar.time
            val events = getEventsForDate(date)
            days.add(createCalendarDay(tempCalendar, date, isCurrentMonth = true, events))
            tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        val remaining = 42 - days.size
        repeat(remaining) {
            val date = tempCalendar.time
            val events = getEventsForDate(date)
            days.add(createCalendarDay(tempCalendar, date, isCurrentMonth = false, events))
            tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return days
    }

    private fun createCalendarDay(cal: Calendar, date: Date, isCurrentMonth: Boolean, events: List<CalendarEvent>): CalendarDay {
        return CalendarDay(
            date = date,
            dayOfMonth = cal.get(Calendar.DAY_OF_MONTH),
            isCurrentMonth = isCurrentMonth,
            isToday = isToday(date),
            hasEvents = events.isNotEmpty(),
            events = events
        )
    }

    private fun getEventsForDate(date: Date): List<CalendarEvent> {
        val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(date)
        val tasks = tasksMap[dateStr] ?: return emptyList()
        return tasks.map { task ->
            val categoryColor = categoriesMap[task.categoryId]?.color ?: "#808080"
            CalendarEvent(
                id = task.id,
                title = task.title,
                date = date,
                categoryColor = categoryColor,
                isAllDay = true
            )
        }
    }

    private fun isToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val checkDate = Calendar.getInstance().apply { time = date }
        return today.get(Calendar.YEAR) == checkDate.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == checkDate.get(Calendar.DAY_OF_YEAR)
    }

    private fun setupAdapters() {
        calendarAdapter = CalendarAdapter(emptyList()) { selectedDay ->
            this.selectedDate = selectedDay.date
            showEventsForDay(selectedDay)
        }
        daysRecyclerView.adapter = calendarAdapter

        eventsAdapter = CalendarEventsAdapter(emptyList())
        eventsRecyclerView.adapter = eventsAdapter
    }

    private fun showEventsForDay(day: CalendarDay) {
        eventsAdapter = CalendarEventsAdapter(day.events)
        eventsRecyclerView.adapter = eventsAdapter
        eventsRecyclerView.scrollToPosition(0)
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
        calendar.add(Calendar.MONTH, delta)
        currentMonth = calendar.get(Calendar.MONTH)
        currentYear = calendar.get(Calendar.YEAR)
        updateMonthYearText()
        updateCalendar()
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1; clearTime() }
        val cal2 = Calendar.getInstance().apply { time = date2; clearTime() }
        return cal1.timeInMillis == cal2.timeInMillis
    }

    private fun Calendar.clearTime() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}