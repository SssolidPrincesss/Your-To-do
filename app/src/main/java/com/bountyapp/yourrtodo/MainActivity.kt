package com.bountyapp.yourrtodo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bountyapp.yourrtodo.adapter.CategoryAdapter
import com.bountyapp.yourrtodo.fragments.FragmentAchievements
import com.bountyapp.yourrtodo.fragments.FragmentCalendar
import com.bountyapp.yourrtodo.fragments.FragmentHome
import com.bountyapp.yourrtodo.fragments.FragmentSettings
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    // UI элементы
    private lateinit var sidePanel: LinearLayout
    private lateinit var categoriesRecyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var collapseButton: LinearLayout
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var mainContentContainer: LinearLayout
    private lateinit var drawerHandle: LinearLayout // Добавляем ручку

    private lateinit var fragmentHome: FragmentHome
    private var isDrawerOpen = false
    private lateinit var categoryAdapter: CategoryAdapter

    // Константы для размеров
    private companion object {
        const val DRAWER_WIDTH_DP = 320f
        const val VISIBLE_HANDLE_WIDTH_DP = 32f
    }

    private var closedPosition: Float = 0f
    private var openPosition: Float = 0f
    private var shiftAmount: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация UI элементов
        initViews()

        // Настройка BottomNavigation
        setupBottomNavigation()

        // Загружаем главный фрагмент
        fragmentHome = FragmentHome()
        loadFragment(fragmentHome)
    }

    private fun initViews() {
        // Находим элементы через findViewById
        sidePanel = findViewById(R.id.side_panel)
        categoriesRecyclerView = findViewById(R.id.categories_recycler_view)
        collapseButton = findViewById(R.id.collapse_button)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        mainContentContainer = findViewById(R.id.main_content_container)
        drawerHandle = findViewById(R.id.drawer_handle) // Находим ручку

        // Вычисляем позиции с учетом плотности экрана
        val density = resources.displayMetrics.density

        // Шторка изначально видна на 32dp (ручка)
        closedPosition = -(DRAWER_WIDTH_DP - VISIBLE_HANDLE_WIDTH_DP) * density  // -288dp

        // Когда открыта - полностью видна
        openPosition = 0f

        // Контент сдвигается на ширину шторки минус ручка
        shiftAmount = (DRAWER_WIDTH_DP - VISIBLE_HANDLE_WIDTH_DP) * density  // 288dp

        // Гарантируем начальное состояние
        sidePanel.post {
            sidePanel.translationX = closedPosition  // Видно только 32dp ручки
            sidePanel.isClickable = false
            drawerHandle.visibility = android.view.View.VISIBLE // Ручка видна
        }

        setupDrawer()
    }

    private fun setupDrawer() {
        // 1. Обработка клика по кнопке "Свернуть" ВНУТРИ шторки
        collapseButton.setOnClickListener {
            toggleDrawer()
        }

        // 2. Обработка клика по РУЧКЕ шторки (сама полоска 32dp)
        drawerHandle.setOnClickListener {
            toggleDrawer()
        }

        // Настройка RecyclerView для категорий
        setupCategoriesRecyclerView()
    }

    private fun setupCategoriesRecyclerView() {
        if (!::fragmentHome.isInitialized) {
            return
        }

        categoriesRecyclerView.layoutManager = LinearLayoutManager(this)

        categoryAdapter = CategoryAdapter(
            categories = fragmentHome.getCategories(),
            onCategoryClick = { category ->
                // 1. Выбираем категорию
                fragmentHome.selectCategory(category.id)
                // 2. Закрываем шторку после выбора
                if (isDrawerOpen) {
                    toggleDrawer()
                }
            },
            onAddCategoryClick = {
                android.widget.Toast.makeText(this, "Добавить новую категорию", android.widget.Toast.LENGTH_SHORT).show()
            }
        )

        categoriesRecyclerView.adapter = categoryAdapter
        fragmentHome.setCategoryAdapter(categoryAdapter)
    }


    private fun toggleDrawer() {
        // Проверяем, что мы на главной вкладке
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

            // Обновляем адаптер перед открытием
            categoryAdapter = CategoryAdapter(
                categories = fragmentHome.getCategories(),
                onCategoryClick = { category ->
                    // Выбираем категорию и закрываем шторку
                    fragmentHome.selectCategory(category.id)
                    if (isDrawerOpen) {
                        toggleDrawer()
                    }
                },
                onAddCategoryClick = {
                    android.widget.Toast.makeText(this, "Добавить новую категорию", android.widget.Toast.LENGTH_SHORT).show()
                }
            )
            categoriesRecyclerView.adapter = categoryAdapter
            fragmentHome.setCategoryAdapter(categoryAdapter)

            sidePanel.isClickable = true

            // Анимация открытия
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
                    drawerHandle.visibility = android.view.View.GONE
                }
                .start()

        } else {
            sidePanel.isClickable = false

            // Анимация закрытия
            sidePanel.animate()
                .translationX(closedPosition)
                .setDuration(300)
                .start()

            mainContentContainer.animate()
                .translationX(0f)
                .setDuration(300)
                .start()

            drawerHandle.visibility = android.view.View.VISIBLE
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
                    // Показываем шторку на главной вкладке
                    showDrawer()
                    loadFragment(FragmentHome())
                    true
                }
                R.id.nav_calendar -> {
                    // Скрываем шторку на других вкладках
                    hideDrawer()
                    loadFragment(FragmentCalendar())
                    true
                }
                R.id.nav_achievements -> {
                    // Скрываем шторку на других вкладках
                    hideDrawer()
                    loadFragment(FragmentAchievements())
                    true
                }
                R.id.nav_settings -> {
                    // Скрываем шторку на других вкладках
                    hideDrawer()
                    loadFragment(FragmentSettings())
                    true
                }
                else -> false
            }
        }

        // Устанавливаем начальный выбранный элемент
        bottomNavigation.selectedItemId = R.id.nav_home
    }
    private fun showDrawer() {
        // Показываем шторку и ручку
        sidePanel.visibility = android.view.View.VISIBLE
        drawerHandle.visibility = android.view.View.VISIBLE

        // Возвращаем отступ для контента
        val density = resources.displayMetrics.density
        mainContentContainer.translationX = 0f
        mainContentContainer.layoutParams = (mainContentContainer.layoutParams as FrameLayout.LayoutParams).apply {
            marginStart = (32 * density).toInt() // 32dp отступ
        }
        mainContentContainer.requestLayout()

        // Сбрасываем состояние шторки
        sidePanel.translationX = closedPosition
        sidePanel.isClickable = false
        isDrawerOpen = false
    }

    private fun hideDrawer() {
        // Скрываем шторку и ручку
        sidePanel.visibility = android.view.View.GONE
        drawerHandle.visibility = android.view.View.GONE

        // Убираем отступ у контента (растягиваем на весь экран)
        mainContentContainer.translationX = 0f
        mainContentContainer.layoutParams = (mainContentContainer.layoutParams as FrameLayout.LayoutParams).apply {
            marginStart = 0 // убираем отступ
        }
        mainContentContainer.requestLayout()

        // Сбрасываем состояние
        sidePanel.isClickable = false
        isDrawerOpen = false
    }
    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        if (fragment is FragmentHome) {
            fragmentHome = fragment
            // Обновляем адаптер категорий при переключении на главный экран
            Handler(Looper.getMainLooper()).postDelayed({
                setupCategoriesRecyclerView()
            }, 100)
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // Обновляем toggleDrawer чтобы он работал только на главной


    override fun onBackPressed() {
        if (bottomNavigation.selectedItemId == R.id.nav_home && isDrawerOpen) {
            toggleDrawer()
        } else if (bottomNavigation.selectedItemId != R.id.nav_home) {
            // Если не на главной, переходим на главную
            bottomNavigation.selectedItemId = R.id.nav_home
        } else {
            super.onBackPressed()
        }
    }
}