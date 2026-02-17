package com.bountyapp.yourrtodo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bountyapp.yourrtodo.adapter.CategoryAdapter
import com.bountyapp.yourrtodo.fragments.FragmentAchievements
import com.bountyapp.yourrtodo.fragments.FragmentCalendar
import com.bountyapp.yourrtodo.fragments.FragmentHome
import com.bountyapp.yourrtodo.fragments.FragmentSettings
import com.bountyapp.yourrtodo.viewmodel.CategoriesViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

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

    // Константы для размеров
    private companion object {
        const val DRAWER_WIDTH_DP = 320f
        const val VISIBLE_HANDLE_WIDTH_DP = 32f
    }

    private var closedPosition: Float = 0f
    private var openPosition: Float = 0f
    private var shiftAmount: Float = 0f
    private var defaultMarginStart: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация ViewModel
        categoriesViewModel = ViewModelProvider(this)[CategoriesViewModel::class.java]

        // Инициализация UI элементов
        initViews()

        // Настройка BottomNavigation
        setupBottomNavigation()

        // Загружаем главный фрагмент
        fragmentHome = FragmentHome()
        loadFragment(fragmentHome)

        // Наблюдаем за изменениями в ViewModel
        observeViewModel()
    }

    private fun observeViewModel() {
        categoriesViewModel.categories.observe(this) { categories ->
            // Когда категории обновляются в ViewModel, обновляем адаптер
            if (::fragmentHome.isInitialized && categoryAdapter != null) {
                categoryAdapter?.updateCategories(categories)
                fragmentHome.refreshCategories()
            }
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

        // Сохраняем исходный отступ
        defaultMarginStart = (mainContentContainer.layoutParams as FrameLayout.LayoutParams).marginStart

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
                }
            )
            categoriesRecyclerView.adapter = categoryAdapter
            fragmentHome.setCategoryAdapter(categoryAdapter!!)
        } else {
            categoryAdapter?.updateCategories(currentCategories)
            fragmentHome.setCategoryAdapter(categoryAdapter!!)
        }
    }

    private fun toggleDrawer() {
        // Разрешаем открывать/закрывать только на главной
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
                    // Показываем шторку (в закрытом состоянии)
                    showDrawer()

                    // Загружаем или показываем главный фрагмент
                    val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                    if (currentFragment !is FragmentHome) {
                        fragmentHome = FragmentHome()
                        loadFragment(fragmentHome)
                    }
                    true
                }
                R.id.nav_calendar -> {
                    // Скрываем шторку полностью
                    hideDrawer()
                    loadFragment(FragmentCalendar())
                    true
                }
                R.id.nav_achievements -> {
                    // Скрываем шторку полностью
                    hideDrawer()
                    loadFragment(FragmentAchievements())
                    true
                }
                R.id.nav_settings -> {
                    // Скрываем шторку полностью
                    hideDrawer()
                    loadFragment(FragmentSettings())
                    true
                }
                else -> false
            }
        }

        bottomNavigation.selectedItemId = R.id.nav_home
    }

    private fun showDrawer() {
        // Показываем шторку и ручку
        sidePanel.visibility = View.VISIBLE
        drawerHandle.visibility = View.VISIBLE

        // Устанавливаем правильные позиции
        sidePanel.translationX = closedPosition
        sidePanel.isClickable = false
        isDrawerOpen = false

        // Восстанавливаем отступ для контента
        mainContentContainer.translationX = 0f
        val params = mainContentContainer.layoutParams as FrameLayout.LayoutParams
        val density = resources.displayMetrics.density
        params.marginStart = (32 * density).toInt()
        mainContentContainer.layoutParams = params
    }

    private fun hideDrawer() {
        // Полностью скрываем шторку и ручку
        sidePanel.visibility = View.GONE
        drawerHandle.visibility = View.GONE

        // Убираем отступ у контента - ВАЖНО: устанавливаем translationX и marginStart в 0
        mainContentContainer.translationX = 0f
        val params = mainContentContainer.layoutParams as FrameLayout.LayoutParams
        params.marginStart = 0
        mainContentContainer.layoutParams = params

        // Принудительно запрашиваем перерисовку
        mainContentContainer.requestLayout()

        // Сбрасываем состояние
        sidePanel.isClickable = false
        isDrawerOpen = false
    }

    private fun loadFragment(fragment: Fragment) {
        if (fragment is FragmentHome) {
            fragmentHome = fragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss()

            Handler(Looper.getMainLooper()).postDelayed({
                if (::fragmentHome.isInitialized) {
                    setupCategoriesRecyclerView()
                }
            }, 200)
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
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
        Handler(Looper.getMainLooper()).postDelayed({
            if (::fragmentHome.isInitialized && categoryAdapter != null) {
                categoryAdapter?.updateCategories(fragmentHome.getCategories())
                fragmentHome.refreshCategories()
            }
        }, 100)
    }
}