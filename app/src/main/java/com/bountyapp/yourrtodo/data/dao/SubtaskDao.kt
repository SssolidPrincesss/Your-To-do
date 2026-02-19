package com.bountyapp.yourrtodo.data.dao

import androidx.room.*
import com.bountyapp.yourrtodo.data.entities.SubtaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubtaskDao {
    @Query("SELECT * FROM subtasks WHERE taskId = :taskId")
    fun getSubtasksForTask(taskId: String): Flow<List<SubtaskEntity>>

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId")
    suspend fun getSubtasksList(taskId: String): List<SubtaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: SubtaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSubtasks(subtasks: List<SubtaskEntity>)

    @Update
    suspend fun updateSubtask(subtask: SubtaskEntity)

    @Delete
    suspend fun deleteSubtask(subtask: SubtaskEntity)

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    suspend fun deleteSubtasksForTask(taskId: String)
}