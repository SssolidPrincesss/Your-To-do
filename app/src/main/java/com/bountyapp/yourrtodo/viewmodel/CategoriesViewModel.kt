// CategoriesViewModel.kt
package com.bountyapp.yourrtodo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bountyapp.yourrtodo.model.Category

class CategoriesViewModel : ViewModel() {

    // MutableLiveData для внутреннего использования
    private val _categories = MutableLiveData<MutableList<Category>>(mutableListOf())

    // LiveData для наблюдения извне (только чтение)
    val categories: LiveData<MutableList<Category>> = _categories

    // Выбранная категория
    private val _selectedCategoryId = MutableLiveData<String>("all")
    val selectedCategoryId: LiveData<String> = _selectedCategoryId

    init {
        // Загружаем начальные категории
        loadDefaultCategories()
    }

    private fun loadDefaultCategories() {
        val defaultCategories = mutableListOf(
            Category(id = "all", name = "Все", color = "#2196F3", isSelected = true),
            Category(id = "work", name = "Работа", color = "#4CAF50", isSelected = false),
            Category(id = "personal", name = "Личное", color = "#FF9800", isSelected = false),
            Category(id = "study", name = "Учеба", color = "#9C27B0", isSelected = false),
            Category(id = "shopping", name = "Покупки", color = "#FF5722", isSelected = false)
        )
        _categories.value = defaultCategories
    }

    // Получить текущий список категорий
    fun getCategoriesList(): MutableList<Category> {
        return _categories.value ?: mutableListOf()
    }

    // Добавить новую категорию
    fun addCategory(name: String, color: String) {
        val currentList = _categories.value ?: mutableListOf()

        // Генерируем уникальный ID
        val newId = "category_${System.currentTimeMillis()}"

        val newCategory = Category(
            id = newId,
            name = name,
            color = color,
            isSelected = false
        )

        currentList.add(newCategory)
        _categories.value = currentList
    }

    // Выбрать категорию
    fun selectCategory(categoryId: String) {
        val currentList = _categories.value ?: return

        // Снимаем выделение со всех
        currentList.forEach { it.isSelected = false }

        // Выделяем выбранную
        currentList.find { it.id == categoryId }?.isSelected = true

        _categories.value = currentList
        _selectedCategoryId.value = categoryId
    }

    // Получить категорию по ID
    fun getCategoryById(categoryId: String): Category? {
        return _categories.value?.find { it.id == categoryId }
    }

    // Обновить категорию
    fun updateCategory(updatedCategory: Category) {
        val currentList = _categories.value ?: return
        val index = currentList.indexOfFirst { it.id == updatedCategory.id }
        if (index != -1) {
            currentList[index] = updatedCategory
            _categories.value = currentList
        }
    }

    // Удалить категорию
    fun deleteCategory(categoryId: String) {
        val currentList = _categories.value ?: return
        currentList.removeAll { it.id == categoryId }

        // Если удалили выбранную категорию, выбираем "Все"
        if (_selectedCategoryId.value == categoryId) {
            selectCategory("all")
        } else {
            _categories.value = currentList
        }
    }
}