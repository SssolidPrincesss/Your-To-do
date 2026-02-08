package com.bountyapp.yourrtodo

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bountyapp.yourrtodo.adapter.CategoryAdapter
import com.bountyapp.yourrtodo.adapter.TaskAdapter
import com.bountyapp.yourrtodo.model.Category
import com.bountyapp.yourrtodo.model.Task
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var categoryTitle: TextView
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: ImageView
    private lateinit var searchActionButton: ImageView
    private lateinit var searchContainer: LinearLayout
    private lateinit var topBar: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var fabAddTask: Button
    private lateinit var collapseButton: LinearLayout
    private lateinit var sidePanel: LinearLayout
    private lateinit var drawerHandle: LinearLayout

    private var isSearchMode = false
    private var isDrawerOpen = false
    private val tasks = mutableListOf<Task>()
    private val allTasks = mutableListOf<Task>()
    private val categories = mutableListOf<Category>()
    private var currentCategoryId: String = "all"
    private lateinit var mainContent: RelativeLayout
    private lateinit var categoriesRecyclerView: RecyclerView

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var searchClearButton: ImageView

    private var lastButtonClickTime = 0L
    private val DOUBLE_CLICK_THRESHOLD = 300L


    // Константы для размеров
    private companion object {
        const val DRAWER_WIDTH_DP = 320f
        const val VISIBLE_WIDTH_DP = 32f // ИЗМЕНИТЬ: 48f → 32f (отступ 32dp)
    }

    private var closedPosition: Float = 0f
    private var shiftAmount: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainContent = findViewById(R.id.main_content)

        // Инициализация всех элементов
        categoryTitle = findViewById(R.id.category_title)
        searchEditText = findViewById(R.id.search_edit_text)
        searchButton = findViewById(R.id.search_button)
        searchActionButton = findViewById(R.id.search_action_button)
        searchContainer = findViewById(R.id.search_container)
        topBar = findViewById(R.id.top_bar)
        recyclerView = findViewById(R.id.recycler_view)
        fabAddTask = findViewById(R.id.fab_add_task)
        collapseButton = findViewById(R.id.collapse_button)
        sidePanel = findViewById(R.id.side_panel)
        drawerHandle = findViewById(R.id.drawer_handle)
        categoriesRecyclerView = findViewById(R.id.categories_recycler_view)

        setupTopBarTransparency()

        // Вычисляем позиции с учетом плотности экрана
        val density = resources.displayMetrics.density
        // Закрытая позиция: -(320dp - 32dp) = -288dp
        closedPosition = -(DRAWER_WIDTH_DP - VISIBLE_WIDTH_DP) * density
        // Сдвиг при открытии: 320dp
        shiftAmount = (DRAWER_WIDTH_DP - VISIBLE_WIDTH_DP) * density

        // Гарантируем начальное состояние
        sidePanel.post {
            sidePanel.translationX = closedPosition
            mainContent.translationX = 0f
            drawerHandle.visibility = View.VISIBLE
            drawerHandle.alpha = 1f
            sidePanel.isClickable = false // Шторка не кликабельна в закрытом состоянии
        }

        // Настройка данных
        setupCategories()
        setupTasks()
        setupRecyclerView()
        setupSearch()
        setupCategoriesRecyclerView()
        setupDrawer()

        fabAddTask.setOnClickListener {
            Toast.makeText(this, getString(R.string.add_new_task), Toast.LENGTH_SHORT).show()
        }

        // Явно устанавливаем начальное состояние
        isDrawerOpen = false
    }

    private fun setupTopBarTransparency() {
        topBar.alpha = 0.7f
    }

    private fun setupCategories() {
        // Дефолтные категории
        categories.addAll(listOf(
            Category(id = "all", name = "Все", color = "#2196F3", isSelected = true),
            Category(id = "work", name = "Работа", color = "#4CAF50", isSelected = false),
            Category(id = "personal", name = "Личное", color = "#FF9800", isSelected = false),
            Category(id = "study", name = "Учеба", color = "#9C27B0", isSelected = false),
            Category(id = "shopping", name = "Покупки", color = "#FF5722", isSelected = false)
        ))
    }

    private fun setupCategoriesRecyclerView() {
        categoriesRecyclerView.layoutManager = LinearLayoutManager(this)
        categoryAdapter = CategoryAdapter(
            categories = categories,
            onCategoryClick = { category ->
                selectCategory(category.id)
            },
            onAddCategoryClick = {
                showAddCategoryDialog()
            }
        )
        categoriesRecyclerView.adapter = categoryAdapter
    }

    private fun setupDrawer() {
        drawerHandle.setOnClickListener {
            toggleDrawer()
        }

        collapseButton.setOnClickListener {
            toggleDrawer()
        }
    }

    private fun setupTasks() {
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
            ),
            Task(
                id = "6",
                title = "Погулять с динозварвом ",
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
            ),
            Task(
                id = "7",
                title = "Купить дом",
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

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        rebuildTasksList()
        taskAdapter = TaskAdapter(
            context = this,
            originalTasks = allTasks,
            onTaskChecked = ::handleTaskCompletion
        )
        recyclerView.adapter = taskAdapter
    }

    private fun rebuildTasksList() {
        allTasks.clear()
        val filteredTasks = if (currentCategoryId == "all") tasks else tasks.filter { it.categoryId == currentCategoryId }

        allTasks.add(Task.createSectionHeader(getString(R.string.section_today)))
        allTasks.addAll(filteredTasks.filter { it.dueDate == null && !it.isCompleted })

        allTasks.add(Task.createSectionHeader(getString(R.string.section_planned)))
        allTasks.addAll(filteredTasks.filter { it.dueDate != null && !it.isCompleted })

        allTasks.add(Task.createSectionHeader(getString(R.string.section_completed)))
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
                val pointsEarned = tasks[index].calculatePoints()
                Toast.makeText(
                    this,
                    getString(R.string.task_completed_with_points, tasks[index].title, pointsEarned),
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
                // Режим поиска активен
                if (searchEditText.text.isNullOrEmpty()) {
                    // Поле пустое → закрываем поиск
                    exitSearchMode()
                } else {
                    // Есть текст → проверяем двойной клик
                    if (timeDiff < DOUBLE_CLICK_THRESHOLD) {
                        // Двойной клик → закрываем поиск
                        exitSearchMode()
                    } else {
                        // Первый клик → очищаем текст
                        searchEditText.setText("")
                        searchEditText.requestFocus()
                    }
                }
            } else {
                // Обычный режим → открываем поиск
                enterSearchMode()
            }

            lastButtonClickTime = currentTime
        }

        // Обработка клавиатуры
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                true
            } else {
                false
            }
        }

        // Обновление иконки при изменении текста
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

                    // Меняем иконку
                    searchButton.setImageResource(
                        if (s.isNullOrEmpty()) R.drawable.ic_close
                        else R.drawable.ic_close
                    )
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun toggleDrawer() {
        updateDrawerState(!isDrawerOpen)
    }

    private fun updateDrawerState(isOpen: Boolean) {
        isDrawerOpen = isOpen

        if (isOpen) {
            // Включаем клики по шторке
            sidePanel.isClickable = true

            sidePanel.animate()
                .translationX(0f)
                .setDuration(300)
                .start()

            mainContent.animate()
                .translationX(shiftAmount)
                .setDuration(300)
                .start()

            // Прячем кнопку открытия
            drawerHandle.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction {
                    drawerHandle.visibility = View.GONE
                }
                .start()

        } else {
            // Отключаем клики по шторке
            sidePanel.isClickable = false

            sidePanel.animate()
                .translationX(closedPosition)
                .setDuration(300)
                .start()

            mainContent.animate()
                .translationX(0f)
                .setDuration(300)
                .start()

            // Показываем кнопку открытия
            drawerHandle.visibility = View.VISIBLE
            drawerHandle.animate()
                .alpha(1f)
                .setDuration(150)
                .start()
        }
    }

    private fun selectCategory(categoryId: String) {
        currentCategoryId = categoryId

        // Обновляем выбор категорий
        categories.forEach { it.isSelected = false }
        categories.find { it.id == categoryId }?.isSelected = true

        // Обновляем адаптер
        categoryAdapter.notifyDataSetChanged()

        // Обновляем верхнюю панель
        val category = categories.find { it.id == categoryId } ?: categories[0]
        updateTopBar(category)

        // Обновляем задачи
        rebuildTasksList()

        // Закрываем шторку
        if (isDrawerOpen) {
            toggleDrawer()
        }
    }

    private fun updateTopBar(category: Category) {
        categoryTitle.text = category.name
        topBar.setBackgroundColor(Color.parseColor(category.color))
    }

    private fun showAddCategoryDialog() {
        // TODO: Реализовать диалог добавления категории
        Toast.makeText(this, "Добавить новую категорию", Toast.LENGTH_SHORT).show()
    }

    private fun enterSearchMode() {
        isSearchMode = true
        searchButton.setImageResource(R.drawable.ic_close)
        categoryTitle.visibility = View.GONE
        searchContainer.visibility = View.VISIBLE
        searchEditText.requestFocus()

        searchEditText.postDelayed({
            showKeyboard()
        }, 100)
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
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    override fun onBackPressed() {
        if (isDrawerOpen) {
            toggleDrawer()
        } else if (isSearchMode) {
            exitSearchMode()
        } else {
            super.onBackPressed()
        }
    }
}