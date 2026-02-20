package com.bountyapp.yourrtodo.adapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.model.Task
import com.google.android.material.card.MaterialCardView

class TaskAdapter(
    private val context: Context,
    private var originalTasks: List<Task>,
    private val onTaskChecked: (Task) -> Unit,
    private val onTaskClick: (Task) -> Unit,
    private val onFlagClick: ((Task) -> Unit)? = null // Опциональный обработчик для флага
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var filteredTasks = originalTasks.toMutableList()
    private val VIEW_TYPE_SECTION = 0
    private val VIEW_TYPE_TASK = 1

    private val collapsedSections = mutableSetOf<String>()
    private var currentSearchQuery = ""

    // Флаг для отслеживания, находится ли адаптер в режиме свайпа
    private var isSwiping = false

    fun updateOriginalTasks(newTasks: List<Task>) {
        originalTasks = newTasks
        applySearchAndSections()
    }

    private fun applySearchAndSections() {
        filteredTasks.clear()

        if (currentSearchQuery.isEmpty()) {
            var currentSection: String? = null

            for (task in originalTasks) {
                if (task.isSectionHeader) {
                    currentSection = task.sectionTitle
                    filteredTasks.add(task)
                } else {
                    if (currentSection != null && !collapsedSections.contains(currentSection)) {
                        filteredTasks.add(task)
                    }
                }
            }
        } else {
            val query = currentSearchQuery.lowercase().trim()

            val matchingTasks = originalTasks.filter { task ->
                if (task.isSectionHeader) return@filter false
                task.title.lowercase().contains(query) ||
                        task.getDisplayDate().lowercase().contains(query)
            }

            if (matchingTasks.isNotEmpty()) {
                filteredTasks.add(Task.createSectionHeader("Результаты поиска: \"$currentSearchQuery\""))
                filteredTasks.addAll(matchingTasks)
            } else {
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

    fun isSectionCollapsed(sectionTitle: String): Boolean = collapsedSections.contains(sectionTitle)

    fun filter(query: String) {
        currentSearchQuery = query
        applySearchAndSections()
    }

    fun clearSearch() {
        currentSearchQuery = ""
        applySearchAndSections()
    }

    fun isInSearchMode(): Boolean = currentSearchQuery.isNotEmpty()

    fun getTaskAtPosition(position: Int): Task {
        return filteredTasks[position]
    }

    fun removeTask(position: Int): Task {
        val task = filteredTasks[position]

        if (task.isSectionHeader) {
            return task
        }

        val originalPosition = originalTasks.indexOfFirst { it.id == task.id }
        if (originalPosition != -1) {
            originalTasks = originalTasks.toMutableList().apply { removeAt(originalPosition) }
        }

        filteredTasks.removeAt(position)
        notifyItemRemoved(position)

        return task
    }

    // Метод для временного скрытия элемента при свайпе
    fun setSwiping(swiping: Boolean) {
        isSwiping = swiping
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
            is TaskViewHolder -> holder.bind(task, position)
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

                    if (!title.startsWith("Результаты поиска:") && title != "Ничего не найдено") {
                        toggleSection(title)
                    }
                }
            }
        }

        fun bind(title: String, isSearchResult: Boolean = false) {
            sectionTitle.text = title

            if (isSearchResult) {
                sectionArrow.visibility = View.GONE
            } else {
                sectionArrow.visibility = View.VISIBLE
                sectionArrow.setImageResource(
                    if (isSectionCollapsed(title)) R.drawable.ic_arrow_up
                    else R.drawable.ic_arrow_down
                )
            }
        }
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskCard: MaterialCardView = itemView.findViewById(R.id.task_card)
        private val taskCheckbox: CheckBox = itemView.findViewById(R.id.task_checkbox)
        private val taskTitle: TextView = itemView.findViewById(R.id.task_title)
        private val taskDate: TextView = itemView.findViewById(R.id.task_date)
        private val flagButton: ImageButton = itemView.findViewById(R.id.flag_button)
        private val reminderIcon: ImageView = itemView.findViewById(R.id.reminder_icon)
        private val recurringIcon: ImageView = itemView.findViewById(R.id.recurring_icon)
        private val subtasksIcon: ImageView = itemView.findViewById(R.id.subtasks_icon)
        private val completedOverlay: View = itemView.findViewById(R.id.completed_overlay)

        init {
            // Обработка клика по чекбоксу
            taskCheckbox.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val task = filteredTasks[position]
                    if (!task.isSectionHeader) {
                        onTaskChecked(task)
                    }
                }
            }

            // Обработка клика по карточке (открытие задачи)
            taskCard.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val task = filteredTasks[position]
                    if (!task.isSectionHeader) {
                        onTaskClick(task)
                    }
                }
            }

            // Обработка клика по флагу (если нужна)
            flagButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val task = filteredTasks[position]
                    if (!task.isSectionHeader) {
                        onFlagClick?.invoke(task)
                    }
                }
            }
        }

        fun bind(task: Task, position: Int) {
            if (task.isSectionHeader) return

            // Устанавливаем данные
            taskTitle.text = task.title
            taskDate.text = task.getDisplayDate()

            // Устанавливаем цвет флага (если используется)
            try {
                flagButton.setColorFilter(ContextCompat.getColor(context, android.R.color.transparent))
                // Здесь можно установить цвет флага из task.flagColor
            } catch (e: Exception) {
                flagButton.visibility = View.GONE
            }

            // Устанавливаем состояние чекбокса
            taskCheckbox.isChecked = task.isCompleted

            // Визуальные эффекты для завершенных задач
            if (task.isCompleted) {
                taskTitle.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG
                taskTitle.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                taskDate.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                completedOverlay.visibility = View.VISIBLE
                taskCard.alpha = 0.7f
            } else {
                taskTitle.paintFlags = Paint.ANTI_ALIAS_FLAG
                taskTitle.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                taskDate.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                completedOverlay.visibility = View.GONE
                taskCard.alpha = 1.0f
            }

            // Показываем/скрываем иконки
            reminderIcon.visibility = if (task.hasReminder && !task.isCompleted) View.VISIBLE else View.GONE
            recurringIcon.visibility = if (task.isRecurring && !task.isCompleted) View.VISIBLE else View.GONE
            subtasksIcon.visibility = if (task.hasSubtasks && !task.isCompleted) View.VISIBLE else View.GONE

            // Устанавливаем contentDescription для доступности
            taskCard.contentDescription = "Задача: ${task.title}"
        }
    }
}