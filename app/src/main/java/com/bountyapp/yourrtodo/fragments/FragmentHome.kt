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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.adapter.CategoryAdapter
import com.bountyapp.yourrtodo.adapter.TaskAdapter
import com.bountyapp.yourrtodo.model.Category
import com.bountyapp.yourrtodo.model.Task
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

    // Данные
    private var isSearchMode = false
    private val tasks = mutableListOf<Task>()
    private val allTasks = mutableListOf<Task>()
    private val categories = mutableListOf<Category>()
    private var currentCategoryId: String = "all"
    private lateinit var categoryAdapter: CategoryAdapter

    private var lastButtonClickTime = 0L
    private val DOUBLE_CLICK_THRESHOLD = 300L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        initViews(view)
        setupData()
        setupUI()
        return view
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

    private fun setupData() {
        // Категории
        categories.addAll(listOf(
            Category(id = "all", name = "Все", color = "#2196F3", isSelected = true),
            Category(id = "work", name = "Работа", color = "#4CAF50", isSelected = false),
            Category(id = "personal", name = "Личное", color = "#FF9800", isSelected = false),
            Category(id = "study", name = "Учеба", color = "#9C27B0", isSelected = false),
            Category(id = "shopping", name = "Покупки", color = "#FF5722", isSelected = false)
        ))

        // Задачи
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
        val filteredTasks = if (currentCategoryId == "all") tasks else tasks.filter { it.categoryId == currentCategoryId }

        allTasks.add(Task.createSectionHeader("Сегодня"))
        allTasks.addAll(filteredTasks.filter { it.dueDate == null && !it.isCompleted })

        allTasks.add(Task.createSectionHeader("В планах"))
        allTasks.addAll(filteredTasks.filter { it.dueDate != null && !it.isCompleted })

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
                val pointsEarned = tasks[index].calculatePoints()
                Toast.makeText(
                    requireContext(),
                    "Задача выполнена: ${tasks[index].title} (+$pointsEarned очков)",
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
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    fun selectCategory(categoryId: String) {
        currentCategoryId = categoryId

        // Обновляем выбор категорий
        categories.forEach { it.isSelected = false }
        categories.find { it.id == categoryId }?.isSelected = true

        // Обновляем адаптер
        categoryAdapter?.notifyDataSetChanged()

        // Обновляем верхнюю панель
        val category = categories.find { it.id == categoryId } ?: categories[0]
        updateTopBar(category)

        // Обновляем задачи
        rebuildTasksList()
    }

    private fun updateTopBar(category: Category) {
        categoryTitle.text = category.name
        topBar.setBackgroundColor(Color.parseColor(category.color))
    }

    // Методы для связи с MainActivity
    fun getCategories(): List<Category> = categories

    fun setCategoryAdapter(adapter: CategoryAdapter) {
        categoryAdapter = adapter
    }

    fun getCurrentCategoryId(): String = currentCategoryId
}