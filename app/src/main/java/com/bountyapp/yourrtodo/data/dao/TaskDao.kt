package com.bountyapp.yourrtodo.data.dao

import androidx.room.*
import com.bountyapp.yourrtodo.data.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    suspend fun getTasksList(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE categoryId = :categoryId OR categoryId = 'all'")
    suspend fun getTasksByCategory(categoryId: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)
}