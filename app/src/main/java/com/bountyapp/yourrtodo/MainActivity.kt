package com.bountyapp.yourrtodo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.bountyapp.yourrtodo.adapter.CategoryAdapter
import com.bountyapp.yourrtodo.callbacks.CategoryItemTouchCallback
import com.bountyapp.yourrtodo.callbacks.CategorySwipeCallback
import com.bountyapp.yourrtodo.fragments.FragmentAchievements
import com.bountyapp.yourrtodo.fragments.FragmentCalendar
import com.bountyapp.yourrtodo.fragments.FragmentHome
import com.bountyapp.yourrtodo.fragments.FragmentSettings
import com.bountyapp.yourrtodo.model.Category
import com.bountyapp.yourrtodo.viewmodel.AchievementsViewModel
import com.bountyapp.yourrtodo.viewmodel.CategoriesViewModel
import com.bountyapp.yourrtodo.viewmodel.SharedEventViewModel
import com.bountyapp.yourrtodo.viewmodel.TasksViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), CategorySwipeCallback {

    private lateinit var categoryItemTouchHelper: ItemTouchHelper

    // UI элементы
    private lateinit var sidePanel: LinearLayout
    private lateinit var categoriesRecyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var collapseButton: LinearLayout
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var mainContentContainer: LinearLayout
    private lateinit var drawerHandle: LinearLayout

    private lateinit var fragmentHome: FragmentHome
    private var isDrawerOpen = false
    private var categoryAdapter: CategoryAdapter? = null

    // ViewModel
    private lateinit var categoriesViewModel: CategoriesViewModel
    private lateinit var tasksViewModel: TasksViewModel
    private lateinit var achievementsViewModel: AchievementsViewModel
    private lateinit var sharedEventViewModel: SharedEventViewModel


    // Константы для размеров
    private companion object {
        const val DRAWER_WIDTH_DP = 320f
        const val VISIBLE_HANDLE_WIDTH_DP = 32f
    }

    private var closedPosition: Float = 0f
    private var openPosition: Float = 0f
    private var shiftAmount: Float = 0f



    // Добавляем флаг для предотвращения дублирования
    private var lastProcessedTaskId: String? = null
    private var lastProcessedTime = 0L
    private val DEBOUNCE_TIME_MS = 1000L
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Разрешение на уведомления не получено", Toast.LENGTH_SHORT).show()
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация всех ViewModel
        categoriesViewModel = ViewModelProvider(this)[CategoriesViewModel::class.java]
        tasksViewModel = ViewModelProvider(this)[TasksViewModel::class.java]
        achievementsViewModel = ViewModelProvider(this)[AchievementsViewModel::class.java]
        sharedEventViewModel = ViewModelProvider(this)[SharedEventViewModel::class.java]

        // Связываем ViewModel друг с другом
        tasksViewModel.setAchievementsViewModel(achievementsViewModel)
        tasksViewModel.setSharedEventViewModel(sharedEventViewModel)
        achievementsViewModel.setSharedEventViewModel(sharedEventViewModel)

        askNotificationPermission()

        // НАБЛЮДАЕМ ТОЛЬКО ЗА UI-СОБЫТИЯМИ
        observeUiEvents()

        // Инициализация UI элементов
        initViews()

        // Настройка BottomNavigation (БЕЗ установки selectedItemId)
        setupBottomNavigation()

        // Загружаем главный фрагмент ТОЛЬКО если нет сохраненного состояния
        if (savedInstanceState == null) {
            Log.d("MainActivity", "Creating initial FragmentHome")
            fragmentHome = FragmentHome()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragmentHome)
                .commit()

            // ДАЕМ ВРЕМЯ ФРАГМЕНТУ СОЗДАТЬСЯ, ЗАТЕМ НАСТРАИВАЕМ АДАПТЕР
            Handler(Looper.getMainLooper()).postDelayed({
                if (::fragmentHome.isInitialized) {
                    setupCategoriesRecyclerView()
                }
            }, 200)
        }

        // Наблюдаем за изменениями в ViewModel
        observeViewModel()
    }


    private fun observeUiEvents() {
        // Наблюдаем за тостами
        sharedEventViewModel.toastMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        // Для логирования (опционально)
        sharedEventViewModel.taskCompletedEvent.observe(this) { event ->
            event?.let { (title, points) ->
                Log.d("MainActivity", "Task completed UI event: $title +$points")
            }
        }

        sharedEventViewModel.achievementUnlockedEvent.observe(this) { achievementName ->
            achievementName?.let {
                // Можно добавить анимацию или звук
                Log.d("MainActivity", "Achievement unlocked UI event: $it")
            }
        }
    }

    private fun observeViewModel() {
        categoriesViewModel.categories.observe(this) { categories ->
            // Когда категории обновляются в ViewModel, обновляем адаптер
            if (::fragmentHome.isInitialized && categoryAdapter != null) {
                categoryAdapter?.updateCategories(categories)
                fragmentHome.refreshCategories()
            }
        }

        // Можно добавить наблюдение за достижениями, если нужно
        achievementsViewModel.userStats.observe(this) { stats ->
            // Если нужно отображать статистику где-то в MainActivity
            Log.d("MainActivity", "User stats updated: ${stats?.totalPoints}")
        }
    }

    private fun initViews() {
        sidePanel = findViewById(R.id.side_panel)
        categoriesRecyclerView = findViewById(R.id.categories_recycler_view)
        collapseButton = findViewById(R.id.collapse_button)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        mainContentContainer = findViewById(R.id.main_content_container)
        drawerHandle = findViewById(R.id.drawer_handle)

        val density = resources.displayMetrics.density
        closedPosition = -(DRAWER_WIDTH_DP - VISIBLE_HANDLE_WIDTH_DP) * density
        openPosition = 0f
        shiftAmount = (DRAWER_WIDTH_DP - VISIBLE_HANDLE_WIDTH_DP) * density

        sidePanel.post {
            sidePanel.translationX = closedPosition
            sidePanel.isClickable = false
            drawerHandle.visibility = View.VISIBLE
        }

        setupDrawer()
    }

    private fun setupDrawer() {
        collapseButton.setOnClickListener {
            toggleDrawer()
        }

        drawerHandle.setOnClickListener {
            toggleDrawer()
        }
    }

    private fun setupCategoriesRecyclerView() {
        if (!::fragmentHome.isInitialized) {
            return
        }

        categoriesRecyclerView.layoutManager = LinearLayoutManager(this)

        val currentCategories = fragmentHome.getCategories()

        if (categoryAdapter == null) {
            categoryAdapter = CategoryAdapter(
                categories = currentCategories,
                onCategoryClick = { category ->
                    Log.d("MainActivity", "Category clicked: ${category.name}")
                    fragmentHome.selectCategory(category.id)
                    if (isDrawerOpen) {
                        updateDrawerState(false)
                    }
                },
                onAddCategoryClick = {
                    // Ничего не делаем
                },
                onCreateCategory = { name, color ->
                    fragmentHome.addCategory(name, color)
                },
                onDeleteCategory = { category ->
                    showDeleteCategoryConfirmationDialog(category)
                }
            )
            categoriesRecyclerView.adapter = categoryAdapter
            fragmentHome.setCategoryAdapter(categoryAdapter!!)

            // Добавляем обработчик свайпов для категорий
            val callback = CategoryItemTouchCallback(this, ItemTouchHelper.RIGHT)
            categoryItemTouchHelper = ItemTouchHelper(callback)
            categoryItemTouchHelper.attachToRecyclerView(categoriesRecyclerView)
        } else {
            categoryAdapter?.updateCategories(currentCategories)
        }
    }

    // Реализация метода интерфейса CategorySwipeCallback
    override fun onCategorySwiped(position: Int) {
        categoryAdapter?.let { adapter ->
            val category = adapter.getCategoryAtPosition(position)
            showDeleteCategoryConfirmationDialog(category, position)
        }
    }

    private fun showDeleteCategoryConfirmationDialog(category: Category, position: Int? = null) {
        // Запрещаем удаление категории "Все"
        if (category.id == "all") {
            if (position != null) {
                categoryAdapter?.notifyItemChanged(position)
            }
            Toast.makeText(this, "Категорию \"Все\" нельзя удалить", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Удаление категории")
            .setMessage("Вы уверены, что хотите удалить категорию \"${category.name}\"? Все задачи в этой категории также будут удалены.")
            .setPositiveButton("Удалить") { _, _ ->
                deleteCategory(category, position)
            }
            .setNegativeButton("Отмена") { _, _ ->
                // Возвращаем элемент на место
                if (position != null) {
                    categoryAdapter?.notifyItemChanged(position)
                }
            }
            .setOnCancelListener {
                // Возвращаем элемент на место при отмене
                if (position != null) {
                    categoryAdapter?.notifyItemChanged(position)
                }
            }
            .show()
    }

    private fun deleteCategory(category: Category, position: Int?) {
        lifecycleScope.launch {
            try {
                // 1. Сначала удаляем все задачи и подзадачи этой категории
                val tasksToDelete = tasksViewModel.getTasksByCategory(category.id)
                for (task in tasksToDelete) {
                    tasksViewModel.deleteTask(task.id)
                }

                // 2. Затем удаляем саму категорию
                categoriesViewModel.deleteCategory(category.id)

                // 3. Обновляем адаптер
                if (position != null) {
                    categoryAdapter?.removeCategory(position)
                }

                Toast.makeText(this@MainActivity, "Категория и все задачи удалены", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Ошибка при удалении: ${e.message}", Toast.LENGTH_SHORT).show()
                // Возвращаем элемент на место в случае ошибки
                if (position != null) {
                    categoryAdapter?.notifyItemChanged(position)
                }
            }
        }
    }

    private fun toggleDrawer() {
        if (bottomNavigation.selectedItemId == R.id.nav_home) {
            updateDrawerState(!isDrawerOpen)
        }
    }

    private fun updateDrawerState(isOpen: Boolean) {
        isDrawerOpen = isOpen

        if (isOpen) {
            if (!::fragmentHome.isInitialized) {
                return
            }

            setupCategoriesRecyclerView()

            sidePanel.isClickable = true

            sidePanel.animate()
                .translationX(openPosition)
                .setDuration(300)
                .start()

            mainContentContainer.animate()
                .translationX(shiftAmount)
                .setDuration(300)
                .start()

            drawerHandle.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction {
                    drawerHandle.visibility = View.GONE
                }
                .start()

        } else {
            categoryAdapter?.exitAddingMode()

            sidePanel.isClickable = false

            sidePanel.animate()
                .translationX(closedPosition)
                .setDuration(300)
                .withEndAction {
                    sidePanel.translationX = closedPosition
                }
                .start()

            mainContentContainer.animate()
                .translationX(0f)
                .setDuration(300)
                .withEndAction {
                    mainContentContainer.translationX = 0f
                }
                .start()

            drawerHandle.visibility = View.VISIBLE
            drawerHandle.animate()
                .alpha(1f)
                .setDuration(150)
                .start()
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    showDrawer()
                    if (supportFragmentManager.findFragmentById(R.id.fragment_container) !is FragmentHome) {
                        fragmentHome = FragmentHome()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragmentHome)
                            .commit()
                    }
                    true
                }
                R.id.nav_calendar -> {
                    hideDrawer()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, FragmentCalendar())
                        .commit()
                    true
                }
                R.id.nav_achievements -> {
                    hideDrawer()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, FragmentAchievements())
                        .commit()
                    true
                }
                R.id.nav_settings -> {
                    hideDrawer()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, FragmentSettings())
                        .commit()
                    true
                }
                else -> false
            }
        }

        // НЕ устанавливаем selectedItemId здесь!
    }

    private fun showDrawer() {
        sidePanel.visibility = View.VISIBLE
        drawerHandle.visibility = View.VISIBLE

        sidePanel.translationX = closedPosition
        sidePanel.isClickable = false
        isDrawerOpen = false

        mainContentContainer.translationX = 0f
        val params = mainContentContainer.layoutParams as FrameLayout.LayoutParams
        val density = resources.displayMetrics.density
        params.marginStart = (32 * density).toInt()
        mainContentContainer.layoutParams = params
    }

    private fun hideDrawer() {
        sidePanel.visibility = View.GONE
        drawerHandle.visibility = View.GONE

        mainContentContainer.translationX = 0f
        val params = mainContentContainer.layoutParams as FrameLayout.LayoutParams
        params.marginStart = 0
        mainContentContainer.layoutParams = params
        mainContentContainer.requestLayout()

        sidePanel.isClickable = false
        isDrawerOpen = false
    }

    override fun onBackPressed() {
        if (bottomNavigation.selectedItemId == R.id.nav_home) {
            if (isDrawerOpen) {
                updateDrawerState(false)
            } else {
                super.onBackPressed()
            }
        } else {
            bottomNavigation.selectedItemId = R.id.nav_home
        }
    }

    fun onFragmentReady() {
        lifecycleScope.launch {
            delay(100)
            if (::fragmentHome.isInitialized && categoryAdapter != null) {
                categoryAdapter?.updateCategories(fragmentHome.getCategories())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::fragmentHome.isInitialized) {
            fragmentHome.refreshCategories()
        }
    }
}