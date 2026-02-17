package com.bountyapp.yourrtodo.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bountyapp.yourrtodo.MainActivity
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.adapter.CategoryAdapter
import com.bountyapp.yourrtodo.adapter.TaskAdapter
import com.bountyapp.yourrtodo.model.Category
import com.bountyapp.yourrtodo.model.Task
import com.bountyapp.yourrtodo.viewmodel.CategoriesViewModel
import java.util.*

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

    // ViewModel - используем activityViewModels для общего доступа
    private val categoriesViewModel: CategoriesViewModel by activityViewModels()

    // Данные
    private var isSearchMode = false
    private val tasks = mutableListOf<Task>()
    private val allTasks = mutableListOf<Task>()
    private var currentCategoryId: String = "all"
    private var categoryAdapter: CategoryAdapter? = null

    private var lastButtonClickTime = 0L
    private val DOUBLE_CLICK_THRESHOLD = 300L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        initViews(view)

        // Инициализируем данные ТОЛЬКО если список категорий пуст
        if (categoriesViewModel.getCategoriesList().isEmpty()) {
            setupDefaultCategories()
        }

        setupTasks()
        setupUI()
        observeViewModel()

        return view
    }

    override fun onResume() {
        super.onResume()
        // При возврате на фрагмент обновляем адаптер
        refreshCategories()
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

    private fun setupDefaultCategories() {
        val defaultCategories = listOf(
            Category(id = "all", name = "Все", color = "#2196F3", isSelected = true),
            Category(id = "work", name = "Работа", color = "#4CAF50", isSelected = false),
            Category(id = "personal", name = "Личное", color = "#FF9800", isSelected = false),
            Category(id = "study", name = "Учеба", color = "#9C27B0", isSelected = false),
            Category(id = "shopping", name = "Покупки", color = "#FF5722", isSelected = false)
        )

        defaultCategories.forEach { category ->
            categoriesViewModel.addCategory(category.name, category.color)
        }

        // Выбираем категорию "Все"
        categoriesViewModel.selectCategory("all")
    }

    private fun observeViewModel() {
        // Наблюдаем за изменениями списка категорий
        categoriesViewModel.categories.observe(viewLifecycleOwner) { categories ->
            // Обновляем адаптер при изменении списка
            categoryAdapter?.updateCategories(categories)

            // Обновляем текущую категорию, если нужно
            val currentCategory = categories.find { it.id == currentCategoryId }
            if (currentCategory != null) {
                updateTopBar(currentCategory)
            } else if (categories.isNotEmpty()) {
                // Если текущая категория не найдена (например, удалена), выбираем первую
                categoriesViewModel.selectCategory(categories[0].id)
            }
        }

        // Наблюдаем за изменениями выбранной категории
        categoriesViewModel.selectedCategoryId.observe(viewLifecycleOwner) { categoryId ->
            currentCategoryId = categoryId

            // Обновляем топ бар
            val category = categoriesViewModel.getCategoryById(categoryId)
            if (category != null) {
                updateTopBar(category)
            }

            // Обновляем список задач
            rebuildTasksList()

            // Обновляем выделение в адаптере категорий
            categoryAdapter?.notifyDataSetChanged()
        }
    }

    private fun setupTasks() {
        tasks.clear()
        tasks.addAll(listOf(
            Task(
                id = "1",
                title = "Создать годовой отчет",
                dueDate = null,
                isCompleted = false,
                isOverdue = false,
                hasReminder = true,
                isRecurring = false,
                hasSubtasks = true,
                flagColor = "#FFC107",
                categoryId = "work"
            ),
            Task(
                id = "2",
                title = "Проверить почту",
                dueDate = null,
                isCompleted = false,
                isOverdue = false,
                hasReminder = false,
                isRecurring = true,
                hasSubtasks = false,
                flagColor = "#4CAF50",
                categoryId = "work"
            ),
            Task(
                id = "3",
                title = "Купить продукты",
                dueDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 1)
                }.time,
                isCompleted = false,
                isOverdue = false,
                hasReminder = true,
                isRecurring = false,
                hasSubtasks = false,
                flagColor = "#2196F3",
                categoryId = "shopping"
            ),
            Task(
                id = "4",
                title = "Сделать домашнее задание",
                dueDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 3)
                }.time,
                isCompleted = false,
                isOverdue = false,
                hasReminder = false,
                isRecurring = false,
                hasSubtasks = true,
                flagColor = "#9C27B0",
                categoryId = "study"
            ),
            Task(
                id = "5",
                title = "Позвонить родителям",
                dueDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, -1)
                }.time,
                isCompleted = true,
                isOverdue = false,
                hasReminder = false,
                isRecurring = false,
                hasSubtasks = false,
                flagColor = "#FF9800",
                categoryId = "personal"
            )
        ))
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
        rebuildTasksList()
        taskAdapter = TaskAdapter(
            context = requireContext(),
            originalTasks = allTasks,
            onTaskChecked = ::handleTaskCompletion
        )
        recyclerView.adapter = taskAdapter
    }

    private fun rebuildTasksList() {
        allTasks.clear()

        // Фильтруем задачи по выбранной категории
        val filteredTasks = if (currentCategoryId == "all") {
            tasks
        } else {
            tasks.filter { it.categoryId == currentCategoryId }
        }

        // Сегодня (задачи без даты)
        allTasks.add(Task.createSectionHeader("Сегодня"))
        allTasks.addAll(filteredTasks.filter { it.dueDate == null && !it.isCompleted })

        // В планах (задачи с датой)
        allTasks.add(Task.createSectionHeader("В планах"))
        allTasks.addAll(filteredTasks.filter { it.dueDate != null && !it.isCompleted })

        // Выполнено
        allTasks.add(Task.createSectionHeader("Выполнено"))
        allTasks.addAll(filteredTasks.filter { it.isCompleted })

        if (::taskAdapter.isInitialized) {
            taskAdapter.updateOriginalTasks(allTasks)
        }
    }

    private fun handleTaskCompletion(task: Task) {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index != -1) {
            val isNowCompleted = !tasks[index].isCompleted
            tasks[index] = tasks[index].copy(isCompleted = isNowCompleted)
            rebuildTasksList()

            if (isNowCompleted) {
                Toast.makeText(
                    requireContext(),
                    "Задача выполнена: ${tasks[index].title}",
                    Toast.LENGTH_SHORT
                ).show()
            }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Уведомляем активность, что фрагмент готов
        (activity as? MainActivity)?.onFragmentReady()

        // Принудительно обновляем категории
        refreshCategories()

        // Обновляем топ бар с текущей категорией
        val currentCategory = categoriesViewModel.getCategoryById(currentCategoryId)
        if (currentCategory != null) {
            updateTopBar(currentCategory)
        }
    }

    private fun showKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    fun selectCategory(categoryId: String) {
        categoriesViewModel.selectCategory(categoryId)
    }

    private fun updateTopBar(category: Category) {
        categoryTitle.text = category.name
        topBar.setBackgroundColor(Color.parseColor(category.color))
    }

    // Методы для связи с MainActivity
    fun getCategories(): MutableList<Category> = categoriesViewModel.getCategoriesList()

    fun setCategoryAdapter(adapter: CategoryAdapter) {
        categoryAdapter = adapter
        // Сразу обновляем адаптер текущими данными
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
}