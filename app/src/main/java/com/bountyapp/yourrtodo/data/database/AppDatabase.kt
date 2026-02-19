package com.bountyapp.yourrtodo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bountyapp.yourrtodo.data.dao.CategoryDao
import com.bountyapp.yourrtodo.data.dao.SubtaskDao
import com.bountyapp.yourrtodo.data.dao.TaskDao
import com.bountyapp.yourrtodo.data.entities.CategoryEntity
import com.bountyapp.yourrtodo.data.entities.SubtaskEntity
import com.bountyapp.yourrtodo.data.entities.TaskEntity

@Database(
    entities = [
        CategoryEntity::class,
        TaskEntity::class,
        SubtaskEntity::class
    ],
    version = 2, // Увеличьте версию
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun taskDao(): TaskDao
    abstract fun subtaskDao(): SubtaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration() // Это очистит БД и создаст заново
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}