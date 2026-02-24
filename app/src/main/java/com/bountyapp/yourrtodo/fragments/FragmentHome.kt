package com.bountyapp.yourrtodo.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bountyapp.yourrtodo.MainActivity
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.TaskActivity
import com.bountyapp.yourrtodo.adapter.CategoryAdapter
import com.bountyapp.yourrtodo.adapter.TaskAdapter
import com.bountyapp.yourrtodo.callbacks.TaskItemTouchCallback
import com.bountyapp.yourrtodo.callbacks.TaskSwipeCallback
import com.bountyapp.yourrtodo.model.Category
import com.bountyapp.yourrtodo.model.Task
import com.bountyapp.yourrtodo.viewmodel.AchievementsViewModel
import com.bountyapp.yourrtodo.viewmodel.CategoriesViewModel
import com.bountyapp.yourrtodo.viewmodel.SharedEventViewModel
import com.bountyapp.yourrtodo.viewmodel.TasksViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class FragmentHome : Fragment(), TaskSwipeCallback {

    // UI элементы
    private var _binding: View? = null
    private val binding get() = _binding!!

    private lateinit var categoryTitle: TextView
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: ImageView
    private lateinit var searchActionButton: ImageView
    private lateinit var searchContainer: LinearLayout
    private lateinit var topBar: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var fabAddTask: ImageButton
    private lateinit var mainContent: RelativeLayout

    // ViewModel
    private val categoriesViewModel: CategoriesViewModel by activityViewModels()
    private val tasksViewModel: TasksViewModel by activityViewModels()
    private val achievementsViewModel: AchievementsViewModel by activityViewModels()
    private val sharedEventViewModel: SharedEventViewModel by activityViewModels()

    // Данные
    private var isSearchMode = false
    private var currentCategoryId: String = "all"
    private var categoryAdapter: CategoryAdapter? = null

    private var lastButtonClickTime = 0L
    private val DOUBLE_CLICK_THRESHOLD = 300L

    private lateinit var itemTouchHelper: ItemTouchHelper

    companion object {
        const val REQUEST_CREATE_TASK = 1002
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflater.inflate(R.layout.fragment_home, container, false)
        return binding
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("FragmentHome", "onViewCreated called")

        initViews(view)
        setupUI()

        // Наблюдаем за ViewModel
        observeViewModels()

        // Наблюдаем только за UI-событиями (тосты уже в MainActivity, но можно и здесь)
        observeUiEvents()

        // Уведомляем активность, что фрагмент готов
        lifecycleScope.launch {
            delay(100)
            (activity as? MainActivity)?.onFragmentReady()
        }
    }
    private fun observeUiEvents() {
        // Можно наблюдать за тостами здесь, если хотите показывать их по-разному
        // Но обычно тосты удобнее показывать в MainActivity

        // Для примера - наблюдаем за событиями выполнения задач
        sharedEventViewModel.taskCompletedEvent.observe(viewLifecycleOwner) { event ->
            event?.let { (title, points) ->
                Log.d("FragmentHome", "Task completed UI: $title +$points")
                // Здесь можно обновить анимацию или какой-то UI элемент
            }
        }

        sharedEventViewModel.taskUncompletedEvent.observe(viewLifecycleOwner) { title ->
            title?.let {
                Log.d("FragmentHome", "Task uncompleted: $it")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("FragmentHome", "onResume called")
        val currentCategory = categoriesViewModel.getCategoryById(currentCategoryId)
        if (currentCategory != null) {
            updateTopBar(currentCategory)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("FragmentHome", "onDestroyView called")
        _binding = null
    }

    private fun initViews(view: View) {
        categoryTitle = view.findViewById(R.id.category_title)
        searchEditText = view.findViewById(R.id.search_edit_text)
        searchButton = view.findViewById(R.id.search_button)
        searchActionButton = view.findViewById(R.id.search_action_button)
        searchContainer = view.findViewById(R.id.search_container)
        topBar = view.findViewById(R.id.top_bar)
        recyclerView = view.findViewById(R.id.recycler_view)
        fabAddTask = view.findViewById(R.id.fab_add_task)
        mainContent = view.findViewById(R.id.main_content)
    }

    private fun observeViewModels() {
        categoriesViewModel.categories.observe(viewLifecycleOwner) { categories ->
            Log.d("FragmentHome", "Categories updated: ${categories.size}")

            val sortedCategories = categories.sortedWith(
                compareBy<Category> {
                    when (it.id) {
                        "all" -> 0
                        else -> 1
                    }
                }.thenBy { it.name }
            )

            categoryAdapter?.updateCategories(sortedCategories)

            // НЕ выбираем категорию автоматически, просто обновляем UI
            val selectedCategory = sortedCategories.find { it.isSelected }
            if (selectedCategory != null) {
                currentCategoryId = selectedCategory.id
                updateTopBar(selectedCategory)
                rebuildTasksList()
            }
        }

        tasksViewModel.tasks.observe(viewLifecycleOwner) { taskList ->
            Log.d("FragmentHome", "Tasks updated: ${taskList.size}")
            rebuildTasksList()
        }
    }

    private fun setupUI() {
        setupTopBarTransparency()
        setupRecyclerView()
        setupSearch()

        fabAddTask.setOnClickListener {
            createNewTask()
        }
    }

    private fun setupTopBarTransparency() {
        topBar.alpha = 0.7f
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        taskAdapter = TaskAdapter(
            context = requireContext(),
            originalTasks = emptyList(),
            onTaskChecked = ::handleTaskCompletion,
            onTaskClick = { task -> openTaskForEdit(task) }
        )
        recyclerView.adapter = taskAdapter

        val callback = TaskItemTouchCallback(this)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onTaskSwiped(position: Int) {
        if (position < 0 || position >= taskAdapter.itemCount) {
            return
        }

        val task = taskAdapter.getTaskAtPosition(position)

        if (task.isSectionHeader) {
            taskAdapter.notifyItemChanged(position)
            return
        }

        showDeleteConfirmationDialog(task, position)
    }

    private fun showDeleteConfirmationDialog(task: Task, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление задачи")
            .setMessage("Вы уверены, что хотите удалить задачу \"${task.title}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                deleteTask(task, position)
            }
            .setNegativeButton("Отмена") { _, _ ->
                taskAdapter.notifyItemChanged(position)
            }
            .setOnCancelListener {
                taskAdapter.notifyItemChanged(position)
            }
            .show()
    }

    private fun deleteTask(task: Task, position: Int) {
        lifecycleScope.launch {
            try {
                tasksViewModel.deleteTask(task.id)
                taskAdapter.removeTask(position)
                Toast.makeText(requireContext(), "Задача удалена", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка при удалении задачи", Toast.LENGTH_SHORT).show()
                taskAdapter.notifyItemChanged(position)
            }
        }
    }

    private fun createNewTask() {
        Log.d("FragmentHome", "Creating new task")

        val newTask = Task(
            id = UUID.randomUUID().toString(),
            title = "",
            dueDate = null,
            isCompleted = false,
            isOverdue = false,
            hasReminder = false,
            isRecurring = false,
            hasSubtasks = false,
            flagColor = "#2196F3",
            categoryId = currentCategoryId,
            notes = null,
            reminderTime = null,
            recurrenceRule = null,
            subtasks = mutableListOf()
        )

        openTask(newTask, isNewTask = true)
    }

    private fun openTaskForEdit(task: Task) {
        Log.d("FragmentHome", "Opening task for edit: ${task.id}")
        openTask(task, isNewTask = false)
    }

    private fun openTask(task: Task, isNewTask: Boolean) {
        val intent = Intent(requireContext(), TaskActivity::class.java)
        intent.putExtra(TaskActivity.EXTRA_TASK, task)
        intent.putExtra(TaskActivity.EXTRA_IS_NEW_TASK, isNewTask)

        if (isNewTask) {
            taskCreateLauncher.launch(intent)
        } else {
            taskResultLauncher.launch(intent)
        }
    }

    private fun rebuildTasksList() {
        Log.d("FragmentHome", "Rebuilding tasks for category: $currentCategoryId")

        val allTasks = tasksViewModel.getTasksByCategory(currentCategoryId)
        Log.d("FragmentHome", "Found ${allTasks.size} tasks")

        val sectionedTasks = mutableListOf<Task>()

        // Сегодня (задачи без даты)
        sectionedTasks.add(Task.createSectionHeader("Сегодня"))
        val todayTasks = allTasks.filter { it.dueDate == null && !it.isCompleted }
        sectionedTasks.addAll(todayTasks)

        // В планах (задачи с датой)
        sectionedTasks.add(Task.createSectionHeader("В планах"))
        val plannedTasks = allTasks.filter { it.dueDate != null && !it.isCompleted }
        sectionedTasks.addAll(plannedTasks)

        // Выполнено
        sectionedTasks.add(Task.createSectionHeader("Выполнено"))
        val completedTasks = allTasks.filter { it.isCompleted }
        sectionedTasks.addAll(completedTasks)

        if (::taskAdapter.isInitialized) {
            taskAdapter.updateOriginalTasks(sectionedTasks)
        }
    }

    private fun setupSearch() {
        searchButton.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            val timeDiff = currentTime - lastButtonClickTime

            if (isSearchMode) {
                if (searchEditText.text.isNullOrEmpty()) {
                    exitSearchMode()
                } else {
                    if (timeDiff < DOUBLE_CLICK_THRESHOLD) {
                        exitSearchMode()
                    } else {
                        searchEditText.setText("")
                        searchEditText.requestFocus()
                    }
                }
            } else {
                enterSearchMode()
            }

            lastButtonClickTime = currentTime
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                true
            } else {
                false
            }
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isSearchMode) {
                    val query = s?.toString()?.trim() ?: ""
                    if (query.isEmpty()) {
                        taskAdapter.clearSearch()
                    } else {
                        taskAdapter.filter(query)
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun enterSearchMode() {
        isSearchMode = true
        searchButton.setImageResource(R.drawable.ic_close)
        categoryTitle.visibility = View.GONE
        searchContainer.visibility = View.VISIBLE
        searchEditText.requestFocus()
        showKeyboard()
    }

    private fun exitSearchMode() {
        isSearchMode = false
        searchButton.setImageResource(R.drawable.ic_search)
        searchEditText.setText("")
        searchContainer.visibility = View.GONE
        categoryTitle.visibility = View.VISIBLE
        taskAdapter.clearSearch()
        hideKeyboard()
    }

    private fun showKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    private fun handleTaskCompletion(task: Task) {
        val currentTask = tasksViewModel.getTaskById(task.id)

        if (currentTask != null) {
            val wasCompleted = currentTask.isCompleted

            // ПРОСТО ВЫЗЫВАЕМ МЕТОД VIEWMODEL - ВСЮ РАБОТУ ДЕЛАЕТ ОНА
            val updatedTask = tasksViewModel.toggleTaskCompletion(task.id)

            if (updatedTask != null) {
                Log.d("FragmentHome", "Task completion toggled: ${task.title}, was: $wasCompleted, now: ${!wasCompleted}")
                // НЕ вызываем sharedEventViewModel.onTaskCompleted() здесь!
                // НЕ вызываем achievementsViewModel.onTaskCompleted() здесь!
                // НЕ показываем тосты здесь!
            }
        }
    }

    // Исправленный лаунчер для редактирования
    private val taskResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getParcelableExtra<Task>(TaskActivity.EXTRA_TASK)?.let { updatedTask ->
                val oldTask = tasksViewModel.getTaskById(updatedTask.id)
                val wasCompleted = oldTask?.isCompleted ?: false
                val isNowCompleted = updatedTask.isCompleted

                tasksViewModel.updateTask(updatedTask)

                // ТОЛЬКО UI через SharedEventViewModel
                sharedEventViewModel.showTaskUpdated(updatedTask.title)

                if (!wasCompleted && isNowCompleted) {
                    // НЕ вызываем onTaskCompleted здесь!
                    // TasksViewModel сам обработает это при toggle
                }
            }
        }
    }

    // Исправленный лаунчер для создания
    private val taskCreateLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getParcelableExtra<Task>(TaskActivity.EXTRA_TASK)?.let { newTask ->
                tasksViewModel.addTask(newTask)
                // TasksViewModel сам покажет тост через SharedEventViewModel
            }
        }
    }


    fun selectCategory(categoryId: String) {
        Log.d("FragmentHome", "selectCategory called with: $categoryId")
        categoriesViewModel.selectCategory(categoryId)
    }

    private fun updateTopBar(category: Category) {
        categoryTitle.text = category.name
        topBar.setBackgroundColor(Color.parseColor(category.color))
    }

    fun getCategories(): List<Category> = categoriesViewModel.getCategoriesList()

    fun setCategoryAdapter(adapter: CategoryAdapter) {
        categoryAdapter = adapter
        categoryAdapter?.updateCategories(categoriesViewModel.getCategoriesList())
    }

    fun getCurrentCategoryId(): String = currentCategoryId

    fun addCategory(name: String, color: String) {
        categoriesViewModel.addCategory(name, color)
        Toast.makeText(requireContext(), "Категория '$name' добавлена", Toast.LENGTH_SHORT).show()
    }

    fun refreshCategories() {
        categoryAdapter?.updateCategories(categoriesViewModel.getCategoriesList())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("current_category_id", currentCategoryId)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            currentCategoryId = it.getString("current_category_id", "all")
        }
    }
}