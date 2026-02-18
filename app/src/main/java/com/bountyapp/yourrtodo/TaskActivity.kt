package com.bountyapp.yourrtodo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bountyapp.yourrtodo.adapter.CategorySpinnerAdapter
import com.bountyapp.yourrtodo.adapter.SubtaskAdapter
import com.bountyapp.yourrtodo.databinding.ActivityTaskBinding
import com.bountyapp.yourrtodo.interfaces.TaskUpdateListener
import com.bountyapp.yourrtodo.model.Category
import com.bountyapp.yourrtodo.model.Subtask
import com.bountyapp.yourrtodo.model.Task
import com.bountyapp.yourrtodo.viewmodel.CategoriesViewModel
import java.util.*

class TaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskBinding
    private lateinit var categoriesViewModel: CategoriesViewModel
    private lateinit var subtaskAdapter: SubtaskAdapter

    private var currentTask: Task? = null
    private val subtasks = mutableListOf<Subtask>()
    private var selectedCategory: Category? = null
    private var dueDate: Date? = null
    private var reminderTime: Date? = null
    private var recurrenceRule: String? = null
    private var notes: String? = null

    // Флаг для отслеживания изменений
    private var hasChanges = false

    // Таймер для автосохранения
    private var autoSaveHandler = Handler(Looper.getMainLooper())
    private var autoSaveRunnable: Runnable? = null

    // Listener для отправки обновлений
    private var taskUpdateListener: TaskUpdateListener? = null

    companion object {
        const val EXTRA_TASK = "extra_task"
        const val EXTRA_TASK_UPDATED = "extra_task_updated"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoriesViewModel = ViewModelProvider(this)[CategoriesViewModel::class.java]

        // Получаем задачу из Intent
        currentTask = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_TASK, Task::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_TASK)
        }

        initViews()
        setupSpinner()
        setupSubtasksRecyclerView()
        setupClickListeners()
        setupTextWatchers()
        loadTaskData()
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.toolbar.setNavigationOnClickListener {
            saveAndExit()
        }
    }

    private fun setupTextWatchers() {
        binding.etTaskTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                hasChanges = true
                scheduleAutoSave()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun scheduleAutoSave() {
        autoSaveRunnable?.let { autoSaveHandler.removeCallbacks(it) }

        autoSaveRunnable = Runnable {
            saveTask()
        }

        autoSaveHandler.postDelayed(autoSaveRunnable!!, 1000)
    }

    private fun setupSpinner() {
        val categories = categoriesViewModel.getCategoriesList()

        val adapter = CategorySpinnerAdapter(this, categories)
        binding.spinnerCategories.adapter = adapter

        currentTask?.let { task ->
            val category = categories.find { it.id == task.categoryId }
            if (category != null) {
                val position = categories.indexOf(category)
                binding.spinnerCategories.setSelection(position)
                selectedCategory = category
            }
        }

        binding.spinnerCategories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newCategory = categories[position]
                if (selectedCategory?.id != newCategory.id) {
                    selectedCategory = newCategory
                    hasChanges = true
                    scheduleAutoSave()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSubtasksRecyclerView() {
        subtaskAdapter = SubtaskAdapter(
            subtasks = subtasks,
            onSubtaskChecked = { subtask ->
                updateSubtaskCompletion(subtask)
                hasChanges = true
                scheduleAutoSave()
            },
            onSubtaskClick = { subtask ->
                showEditSubtaskDialog(subtask)
            }
        )

        binding.rvSubtasks.layoutManager = LinearLayoutManager(this)
        binding.rvSubtasks.adapter = subtaskAdapter
    }

    private fun setupClickListeners() {
        binding.layoutAddSubtask.setOnClickListener {
            showAddSubtaskDialog()
        }

        binding.layoutDates.setOnClickListener {
            showDatePicker()
        }

        binding.layoutReminder.setOnClickListener {
            showReminderPicker()
        }

        binding.layoutRecurrence.setOnClickListener {
            showRecurrenceDialog()
        }

        binding.layoutNotes.setOnClickListener {
            showNotesDialog()
        }

        binding.layoutAttachments.setOnClickListener {
            showAttachmentsDialog()
        }

        binding.btnCompleteTask.setOnClickListener {
            completeTask()
        }
    }

    private fun loadTaskData() {
        currentTask?.let { task ->
            binding.etTaskTitle.setText(task.title)

            subtasks.clear()
            task.subtasks?.let {
                subtasks.addAll(it)
            }
            subtaskAdapter.updateSubtasks(subtasks)

            dueDate = task.dueDate
            updateDueDateDisplay()

            reminderTime = task.reminderTime
            updateReminderDisplay()

            recurrenceRule = task.recurrenceRule
            updateRecurrenceDisplay()

            notes = task.notes
            updateNotesDisplay()

            updateCompleteButtonState(task.isCompleted)
        }

        hasChanges = false
    }

    private fun showAddSubtaskDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_subtask, null)
        val input = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_subtask_title)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Новая подзадача")
            .setView(dialogView)
            .setPositiveButton("Добавить", null)
            .setNegativeButton("Отмена", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val title = input.text.toString().trim()
                if (title.isNotEmpty()) {
                    val exists = subtasks.any { it.title.equals(title, ignoreCase = true) }

                    if (!exists) {
                        val newSubtask = Subtask(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            taskId = currentTask?.id ?: ""
                        )

                        subtasks.add(newSubtask)
                        subtaskAdapter.updateSubtasks(subtasks)

                        hasChanges = true
                        scheduleAutoSave()

                        input.text?.clear()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, "Подзадача с таким названием уже существует", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    input.error = "Введите название подзадачи"
                }
            }
        }

        dialog.show()
    }

    private fun showEditSubtaskDialog(subtask: Subtask) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_subtask, null)
        val input = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_subtask_title)
        input.setText(subtask.title)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Редактировать подзадачу")
            .setView(dialogView)
            .setPositiveButton("Сохранить", null)
            .setNegativeButton("Отмена", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val newTitle = input.text.toString().trim()
                if (newTitle.isNotEmpty()) {
                    val index = subtasks.indexOfFirst { it.id == subtask.id }
                    if (index != -1) {
                        subtasks[index].title = newTitle
                        subtaskAdapter.updateSubtasks(subtasks)

                        hasChanges = true
                        scheduleAutoSave()

                        dialog.dismiss()
                    }
                } else {
                    input.error = "Введите название подзадачи"
                }
            }
        }

        dialog.show()
    }

    private fun updateSubtaskCompletion(subtask: Subtask) {
        val index = subtasks.indexOfFirst { it.id == subtask.id }
        if (index != -1) {
            subtasks[index].isCompleted = subtask.isCompleted
            subtaskAdapter.updateSubtasks(subtasks)
            checkAllSubtasksCompleted()
        }
    }

    private fun checkAllSubtasksCompleted() {
        if (subtasks.isNotEmpty() && subtasks.all { it.isCompleted }) {
            Toast.makeText(this, "Все подзадачи выполнены!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        dueDate?.let {
            calendar.time = it
        }

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                dueDate = calendar.time
                updateDueDateDisplay()
                hasChanges = true
                scheduleAutoSave()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDueDateDisplay() {
        binding.tvDueDate.text = dueDate?.let {
            val format = java.text.SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            format.format(it)
        } ?: "Нет срока"
    }

    private fun showReminderPicker() {
        val calendar = Calendar.getInstance()
        reminderTime?.let {
            calendar.time = it
        }

        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                reminderTime = calendar.time
                updateReminderDisplay()
                hasChanges = true
                scheduleAutoSave()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun updateReminderDisplay() {
        binding.tvReminder.text = reminderTime?.let {
            val format = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
            "Напоминание в ${format.format(it)}"
        } ?: "Нет напоминания"
    }

    private fun showRecurrenceDialog() {
        val options = arrayOf("Не повторять", "Каждый день", "Каждую неделю", "Каждый месяц", "Каждый год")
        val currentSelection = when (recurrenceRule) {
            "DAILY" -> 1
            "WEEKLY" -> 2
            "MONTHLY" -> 3
            "YEARLY" -> 4
            else -> 0
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Повторение")
            .setSingleChoiceItems(options, currentSelection) { _, which ->
                recurrenceRule = when (which) {
                    1 -> "DAILY"
                    2 -> "WEEKLY"
                    3 -> "MONTHLY"
                    4 -> "YEARLY"
                    else -> null
                }
                updateRecurrenceDisplay()
                hasChanges = true
                scheduleAutoSave()
            }
            .setPositiveButton("ОК", null)
            .show()
    }

    private fun updateRecurrenceDisplay() {
        binding.tvRecurrence.text = when (recurrenceRule) {
            "DAILY" -> "Каждый день"
            "WEEKLY" -> "Каждую неделю"
            "MONTHLY" -> "Каждый месяц"
            "YEARLY" -> "Каждый год"
            else -> "Нет повторения"
        }
    }

    private fun showNotesDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_notes, null)
        val input = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_notes)
        input.setText(notes)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Заметки")
            .setView(dialogView)
            .setPositiveButton("Сохранить", null)
            .setNegativeButton("Отмена", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                notes = input.text.toString()
                updateNotesDisplay()
                hasChanges = true
                scheduleAutoSave()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun updateNotesDisplay() {
        binding.tvNotes.text = if (notes.isNullOrEmpty()) "Нет заметок" else notes
    }

    private fun showAttachmentsDialog() {
        Toast.makeText(this, "Функция вложений будет доступна в следующем обновлении", Toast.LENGTH_SHORT).show()
    }

    private fun completeTask() {
        currentTask?.let { task ->
            task.isCompleted = !task.isCompleted
            updateCompleteButtonState(task.isCompleted)
            hasChanges = true
            saveTask()

            val message = if (task.isCompleted) "Задача выполнена!" else "Задача возвращена в работу"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCompleteButtonState(isCompleted: Boolean) {
        if (isCompleted) {
            binding.btnCompleteTask.text = "Вернуть в работу"
        } else {
            binding.btnCompleteTask.text = "Выполнить задачу"
        }
    }

    private fun saveTask() {
        if (!hasChanges && currentTask != null) return

        currentTask?.let { task ->
            task.title = binding.etTaskTitle.text.toString()
            task.dueDate = dueDate
            task.reminderTime = reminderTime
            task.recurrenceRule = recurrenceRule
            task.notes = notes
            task.categoryId = selectedCategory?.id ?: "all"
            task.hasSubtasks = subtasks.isNotEmpty()
            task.subtasks = subtasks.toMutableList()

            hasChanges = false

            // Отправляем обновленную задачу через Intent
            val resultIntent = Intent()
            resultIntent.putExtra(EXTRA_TASK, task)
            setResult(RESULT_OK, resultIntent)
        }
    }

    private fun saveAndExit() {
        saveTask()
        finish()
    }

    override fun onPause() {
        super.onPause()
        saveTask()
    }

    override fun onBackPressed() {
        saveAndExit()
        super.onBackPressed()
    }
}