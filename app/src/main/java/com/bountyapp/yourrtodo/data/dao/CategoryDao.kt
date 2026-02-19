package com.bountyapp.yourrtodo.data.dao

import androidx.room.*
import com.bountyapp.yourrtodo.data.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY CASE WHEN id = 'all' THEN 0 ELSE 1 END, name")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories")
    suspend fun getCategoriesList(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: String): CategoryEntity?

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("UPDATE categories SET isSelected = 0")
    suspend fun clearAllSelected()

    @Query("UPDATE categories SET isSelected = 1 WHERE id = :categoryId")
    suspend fun setCategorySelected(categoryId: String)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id != 'all'")
    suspend fun deleteAllCustomCategories()
}