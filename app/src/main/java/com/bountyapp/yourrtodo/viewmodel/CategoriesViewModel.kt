package com.bountyapp.yourrtodo.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bountyapp.yourrtodo.data.repository.CategoryRepository
import com.bountyapp.yourrtodo.model.Category
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

class CategoriesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CategoryRepository(application.applicationContext)

    private val _categories = MutableLiveData<List<Category>>(emptyList())
    val categories: LiveData<List<Category>> = _categories

    private val _selectedCategoryId = MutableLiveData<String>("all")
    val selectedCategoryId: LiveData<String> = _selectedCategoryId

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    // Флаг для предотвращения множественной инициализации
    private var isInitialized = false

    init {
        viewModelScope.launch {
            repository.initDefaultCategories()
            loadCategories()
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getAllCategories().collect { categoryList ->
                    val sortedList = categoryList.sortedWith(
                        compareBy<Category> {
                            when (it.id) {
                                "all" -> 0
                                else -> 1
                            }
                        }.thenBy { it.name }
                    )

                    _categories.postValue(sortedList)

                    // ВАЖНО: Находим выбранную категорию из БД
                    val selectedCategory = sortedList.find { it.isSelected }

                    if (selectedCategory != null) {
                        // Если есть выбранная категория, просто обновляем ID
                        // НЕ выбираем новую категорию, а используем ту, что уже выбрана
                        if (_selectedCategoryId.value != selectedCategory.id) {
                            _selectedCategoryId.postValue(selectedCategory.id)
                        }
                    } else if (sortedList.isNotEmpty() && !isInitialized) {
                        // Только при первой загрузке, если нет выбранной категории, выбираем "Все"
                        val allCategory = sortedList.find { it.id == "all" }
                        allCategory?.let {
                            // Устанавливаем "Все" как выбранную в БД
                            repository.selectCategory("all")
                        }
                    }

                    isInitialized = true
                    _isLoading.postValue(false)
                }
            } catch (e: Exception) {
                _error.postValue("Ошибка загрузки категорий: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }

    fun getCategoriesList(): List<Category> = _categories.value ?: emptyList()

    fun addCategory(name: String, color: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newCategory = Category(
                    id = "cat_${System.currentTimeMillis()}",
                    name = name,
                    color = color,
                    isSelected = false
                )
                repository.insertCategory(newCategory)
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Ошибка добавления категории: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun selectCategory(categoryId: String) {
        // Проверяем, не выбрана ли уже эта категория
        val currentSelected = _selectedCategoryId.value
        if (currentSelected == categoryId) {
            Log.d("CategoriesViewModel", "Category $categoryId already selected, ignoring")
            return
        }

        viewModelScope.launch {
            try {
                Log.d("CategoriesViewModel", "Selecting category: $categoryId")
                repository.selectCategory(categoryId)
                // Не обновляем _selectedCategoryId здесь - это сделает Flow после обновления БД
            } catch (e: Exception) {
                _error.value = "Ошибка выбора категории: ${e.message}"
            }
        }
    }

    fun getCategoryById(categoryId: String): Category? {
        return _categories.value?.find { it.id == categoryId }
    }

    fun updateCategory(updatedCategory: Category) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateCategory(updatedCategory)
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Ошибка обновления категории: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val category = getCategoryById(categoryId)
                if (category != null) {
                    repository.deleteCategory(category)
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Ошибка удаления категории: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}