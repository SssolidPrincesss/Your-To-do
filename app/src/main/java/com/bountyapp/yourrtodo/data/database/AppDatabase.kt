package com.bountyapp.yourrtodo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bountyapp.yourrtodo.data.dao.AchievementDao
import com.bountyapp.yourrtodo.data.dao.CategoryDao
import com.bountyapp.yourrtodo.data.dao.SubtaskDao
import com.bountyapp.yourrtodo.data.dao.TaskDao
import com.bountyapp.yourrtodo.data.entities.*

@Database(
    entities = [
        CategoryEntity::class,
        TaskEntity::class,
        SubtaskEntity::class,
        AchievementEntity::class,      // Добавлено
        UserStatsEntity::class          // Добавлено
    ],
    version = 3, // Увеличена версия с 2 до 3
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun taskDao(): TaskDao
    abstract fun subtaskDao(): SubtaskDao
    abstract fun achievementDao(): AchievementDao   // Добавлен новый Dao

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