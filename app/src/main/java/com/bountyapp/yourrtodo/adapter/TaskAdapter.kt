package com.bountyapp.yourrtodo.adapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.model.Task

class TaskAdapter(
    private val context: Context,
    private var originalTasks: List<Task>,
    private val onTaskChecked: (Task) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var filteredTasks = originalTasks.toMutableList()
    private val VIEW_TYPE_SECTION = 0
    private val VIEW_TYPE_TASK = 1

    // Храним состояние свернутости для каждой секции
    private val collapsedSections = mutableSetOf<String>()

    // Текущий запрос поиска
    private var currentSearchQuery = ""

    // Новый метод для обновления списка
    fun updateOriginalTasks(newTasks: List<Task>) {
        originalTasks = newTasks
        applySearchAndSections() // Перестраиваем с учетом поиска
    }

    private fun applySearchAndSections() {
        filteredTasks.clear()

        if (currentSearchQuery.isEmpty()) {
            // Нормальный режим - показываем с секциями
            var currentSection: String? = null

            for (task in originalTasks) {
                if (task.isSectionHeader) {
                    currentSection = task.sectionTitle
                    // Всегда добавляем заголовок секции
                    filteredTasks.add(task)
                } else {
                    // Добавляем задачи только если их секция не свернута
                    if (currentSection != null && !collapsedSections.contains(currentSection)) {
                        filteredTasks.add(task)
                    }
                }
            }
        } else {
            // Режим поиска - показываем только подходящие задачи
            val query = currentSearchQuery.lowercase().trim()

            // Собираем все задачи, которые подходят под поиск
            val matchingTasks = originalTasks.filter { task ->
                if (task.isSectionHeader) return@filter false

                val matchesTitle = task.title.lowercase().contains(query)
                val matchesDate = task.getDisplayDate().lowercase().contains(query)

                matchesTitle || matchesDate
            }

            // Если есть результаты поиска, добавляем заголовок
            if (matchingTasks.isNotEmpty()) {
                filteredTasks.add(Task.createSectionHeader("Результаты поиска: \"$currentSearchQuery\""))
                filteredTasks.addAll(matchingTasks)
            } else {
                // Если нет результатов, показываем заголовок "Нет результатов"
                filteredTasks.add(Task.createSectionHeader("Ничего не найдено"))
            }
        }

        notifyDataSetChanged()
    }

    fun toggleSection(sectionTitle: String) {
        if (collapsedSections.contains(sectionTitle)) {
            collapsedSections.remove(sectionTitle)
        } else {
            collapsedSections.add(sectionTitle)
        }
        applySearchAndSections()
    }

    fun isSectionCollapsed(sectionTitle: String): Boolean {
        return collapsedSections.contains(sectionTitle)
    }

    fun filter(query: String) {
        currentSearchQuery = query
        applySearchAndSections()
    }

    fun clearSearch() {
        currentSearchQuery = ""
        applySearchAndSections()
    }

    fun isInSearchMode(): Boolean {
        return currentSearchQuery.isNotEmpty()
    }

    override fun getItemViewType(position: Int): Int {
        return if (filteredTasks[position].isSectionHeader) VIEW_TYPE_SECTION else VIEW_TYPE_TASK
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SECTION -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.section_header, parent, false)
                SectionViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.task_item, parent, false)
                TaskViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val task = filteredTasks[position]
        when (holder) {
            is SectionViewHolder -> {
                val title = task.sectionTitle.orEmpty()
                val isSearchResult = title.startsWith("Результаты поиска:") || title == "Ничего не найдено"
                holder.bind(title, isSearchResult)
            }
            is TaskViewHolder -> holder.bind(task)
        }
    }

    override fun getItemCount() = filteredTasks.size

    inner class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sectionTitle: TextView = itemView.findViewById(R.id.section_title)
        private val sectionArrow: ImageView = itemView.findViewById(R.id.section_arrow)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val section = filteredTasks[position]
                    val title = section.sectionTitle.orEmpty()

                    // Не позволяем сворачивать результаты поиска
                    if (!title.startsWith("Результаты поиска:") && title != "Ничего не найдено") {
                        toggleSection(title)
                    }
                }
            }
        }

        fun bind(title: String, isSearchResult: Boolean = false) {
            sectionTitle.text = title

            if (isSearchResult) {
                // Для результатов поиска скрываем стрелку
                sectionArrow.visibility = View.GONE
            } else {
                sectionArrow.visibility = View.VISIBLE
                // Устанавливаем правильную стрелку в зависимости от состояния
                if (isSectionCollapsed(title)) {
                    sectionArrow.setImageResource(R.drawable.ic_arrow_up)
                } else {
                    sectionArrow.setImageResource(R.drawable.ic_arrow_down)
                }
            }
        }
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskCheckbox: CheckBox = itemView.findViewById(R.id.task_checkbox)
        private val taskTitle: TextView = itemView.findViewById(R.id.task_title)
        private val taskDate: TextView = itemView.findViewById(R.id.task_date)
        private val taskCard: View = itemView.findViewById(R.id.task_card)

        init {
            // ТОЛЬКО клик по чекбоксу
            taskCheckbox.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION && !filteredTasks[position].isSectionHeader) {
                    val task = filteredTasks[position]
                    onTaskChecked(task)
                }
            }

            // Клик по карточке для открытия деталей задачи
            taskCard.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION && !filteredTasks[position].isSectionHeader) {
                    val task = filteredTasks[position]
                    // TODO: Здесь будет переход в активити деталей задачи
                    Toast.makeText(context, "Открыть задачу: ${task.title}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fun bind(task: Task) {
            if (task.isSectionHeader) return

            taskTitle.text = task.title
            taskDate.text = task.getDisplayDate()

            // Устанавливаем состояние чекбокса
            taskCheckbox.isChecked = task.isCompleted

            // Визуальные эффекты для завершенных задач
            if (task.isCompleted) {
                taskTitle.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG
                taskTitle.setTextColor(context.getColor(android.R.color.darker_gray))
                taskDate.setTextColor(context.getColor(android.R.color.darker_gray))
            } else {
                taskTitle.paintFlags = Paint.ANTI_ALIAS_FLAG
                taskTitle.setTextColor(context.getColor(android.R.color.black))
                taskDate.setTextColor(context.getColor(android.R.color.darker_gray))
            }

            // Показываем/скрываем иконки в зависимости от состояния задачи
            val reminderIcon: ImageView = itemView.findViewById(R.id.reminder_icon)
            val recurringIcon: ImageView = itemView.findViewById(R.id.recurring_icon)
            val subtasksIcon: ImageView = itemView.findViewById(R.id.subtasks_icon)

            reminderIcon.visibility = if (task.hasReminder && !task.isCompleted) View.VISIBLE else View.GONE
            recurringIcon.visibility = if (task.isRecurring && !task.isCompleted) View.VISIBLE else View.GONE
            subtasksIcon.visibility = if (task.hasSubtasks && !task.isCompleted) View.VISIBLE else View.GONE

            // Меняем прозрачность для выполненных задач
            taskCard.alpha = if (task.isCompleted) 0.6f else 1.0f
        }
    }
}