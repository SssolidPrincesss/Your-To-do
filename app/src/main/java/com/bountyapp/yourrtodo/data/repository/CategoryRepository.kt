package com.bountyapp.yourrtodo.data.repository

import android.content.Context
import com.bountyapp.yourrtodo.data.database.AppDatabase
import com.bountyapp.yourrtodo.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepository(context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val categoryDao = database.categoryDao()

    fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { entities ->
            entities.map { entity ->
                Category(
                    id = entity.id,
                    name = entity.name,
                    color = entity.color,
                    isSelected = entity.isSelected
                )
            }
        }
    }

    suspend fun insertCategory(category: Category) {
        val existing = categoryDao.getCategoryByName(category.name)
        if (existing == null) {
            categoryDao.insertCategory(
                com.bountyapp.yourrtodo.data.entities.CategoryEntity(
                    id = category.id,
                    name = category.name,
                    color = category.color,
                    isSelected = category.isSelected
                )
            )
        }
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(
            com.bountyapp.yourrtodo.data.entities.CategoryEntity(
                id = category.id,
                name = category.name,
                color = category.color,
                isSelected = category.isSelected
            )
        )
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(
            com.bountyapp.yourrtodo.data.entities.CategoryEntity(
                id = category.id,
                name = category.name,
                color = category.color,
                isSelected = category.isSelected
            )
        )
    }

    suspend fun selectCategory(categoryId: String) {
        categoryDao.clearAllSelected()
        categoryDao.setCategorySelected(categoryId)
    }

    suspend fun initDefaultCategories() {
        val categories = categoryDao.getCategoriesList()
        if (categories.isEmpty()) {
            // Важно: только "Все" должна быть selected=true, остальные false
            val defaultCategories = listOf(
                com.bountyapp.yourrtodo.data.entities.CategoryEntity(
                    id = "all", name = "Все", color = "#2196F3", isSelected = true
                ),
                com.bountyapp.yourrtodo.data.entities.CategoryEntity(
                    id = "work", name = "Работа", color = "#4CAF50", isSelected = false
                ),
                com.bountyapp.yourrtodo.data.entities.CategoryEntity(
                    id = "personal", name = "Личное", color = "#FF9800", isSelected = false
                ),
                com.bountyapp.yourrtodo.data.entities.CategoryEntity(
                    id = "study", name = "Учеба", color = "#9C27B0", isSelected = false
                ),
                com.bountyapp.yourrtodo.data.entities.CategoryEntity(
                    id = "shopping", name = "Покупки", color = "#FF5722", isSelected = false
                )
            )
            defaultCategories.forEach { categoryDao.insertCategory(it) }
        } else {
            // Проверяем, что категория "all" существует
            val allCategory = categories.find { it.id == "all" }
            if (allCategory == null) {
                // Если категории "all" нет, создаём её
                categoryDao.insertCategory(
                    com.bountyapp.yourrtodo.data.entities.CategoryEntity(
                        id = "all", name = "Все", color = "#2196F3", isSelected = true
                    )
                )
            }

            // Проверяем, что выбрана только одна категория
            val selectedCategories = categories.filter { it.isSelected }
            if (selectedCategories.size != 1) {
                // Если выбрано несколько или ни одной, сбрасываем и выбираем "Все"
                categoryDao.clearAllSelected()
                categoryDao.setCategorySelected("all")
            }
        }
    }

}