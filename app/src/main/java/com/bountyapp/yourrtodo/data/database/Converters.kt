package com.bountyapp.yourrtodo.data.database

import androidx.room.TypeConverter
import com.bountyapp.yourrtodo.data.entities.AchievementType
import java.util.*

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // Добавлены конвертеры для AchievementType
    @TypeConverter
    fun fromAchievementType(type: AchievementType): String {
        return type.name
    }

    @TypeConverter
    fun toAchievementType(name: String): AchievementType {
        return AchievementType.valueOf(name)
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String = value.joinToString("|")

    @TypeConverter
    fun toStringList(value: String): List<String> =
        if (value.isBlank()) emptyList() else value.split("|")
}