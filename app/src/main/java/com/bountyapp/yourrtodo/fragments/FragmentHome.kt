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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bountyapp.yourrtodo.MainActivity
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.TaskActivity
import com.bountyapp.yourrtodo.adapter.CategoryAdapter
import com.bountyapp.yourrtodo.adapter.TaskAdapter
import com.bountyapp.yourrtodo.model.Category
import com.bountyapp.yourrtodo.model.Task
import com.bountyapp.yourrtodo.viewmodel.CategoriesViewModel
import com.bountyapp.yourrtodo.viewmodel.TasksViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentHome : Fragment() {

    // UI элементы
    private lateinit var categoryTitle: TextView
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: ImageView
    private lateinit var searchActionButton: ImageView
    private lateinit var searchContainer: LinearLayout
    private lateinit var topBar: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var fabAddTask: Button
    private lateinit var mainContent: RelativeLayout

    // ViewModel
    private val categoriesViewModel: CategoriesViewModel by activityViewModels()
    private val tasksViewModel: TasksViewModel by activityViewModels()

    // Данные
    private var isSearchMode = false
    private var currentCategoryId: String = "all"
    private var categoryAdapter: CategoryAdapter? = null

    private var lastButtonClickTime = 0L
    private val DOUBLE_CLICK_THRESHOLD = 300L


    private var isReturningFromTask = false

    // Регистрируем контракт для получения результата из TaskActivity
    private val taskResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // СНАЧАЛА устанавливаем флаг, потом обрабатываем результат
        isReturningFromTask = true
        Log.d("FragmentHome", "Returning from TaskActivity, setting flag to true")

        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getParcelableExtra<Task>(TaskActivity.EXTRA_TASK)?.let { updatedTask ->
                tasksViewModel.updateTask(updatedTask)
                Toast.makeText(requireContext(), "Задача обновлена", Toast.LENGTH_SHORT).show()
            }
        }

        // НЕ сбрасываем флаг сразу, дадим время на обработку обновлений
        viewLifecycleOwner.lifecycleScope.launch {
            // Ждем немного, чтобы все обновления успели обработаться
            delay(1000)
            isReturningFromTask = false
            Log.d("FragmentHome", "Resetting return flag")
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        initViews(view)
        setupUI()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Всегда регистрируем наблюдатели в onViewCreated, но с проверкой жизненного цикла
        Log.d("FragmentHome", "Setting up observers in onViewCreated")
        observeViewModels()

        // Уведомляем активность, что фрагмент готов
        lifecycleScope.launch {
            delay(100)
            (activity as? MainActivity)?.onFragmentReady()
        }
    }

    override fun onResume() {
        super.onResume()
        // Обновляем текущую категорию
        val currentCategory = categoriesViewModel.getCategoryById(currentCategoryId)
        if (currentCategory != null) {
            updateTopBar(currentCategory)
        }
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

            val selectedCategory = sortedCategories.find { it.isSelected }
            if (selectedCategory != null) {
                if (currentCategoryId != selectedCategory.id) {
                    Log.d("FragmentHome", "Selected category changed to: ${selectedCategory.id}")
                    currentCategoryId = selectedCategory.id
                    updateTopBar(selectedCategory)
                    rebuildTasksList()
                }
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
            Toast.makeText(requireContext(), "Добавить новую задачу", Toast.LENGTH_SHORT).show()
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
            onTaskClick = { task -> openTask(task) }
        )
        recyclerView.adapter = taskAdapter
    }

    private fun openTask(task: Task) {
        val intent = Intent(requireContext(), TaskActivity::class.java)
        intent.putExtra(TaskActivity.EXTRA_TASK, task)
        taskResultLauncher.launch(intent)
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
        Log.d("FragmentHome", "Task completion toggled: ${task.title}")
        tasksViewModel.toggleTaskCompletion(task.id)
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