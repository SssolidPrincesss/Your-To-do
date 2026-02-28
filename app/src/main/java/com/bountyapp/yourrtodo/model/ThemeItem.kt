// com/bountyapp/yourrtodo/model/ThemeItem.kt
package com.bountyapp.yourrtodo.model

import com.bountyapp.yourrtodo.model.UserStatus

/**
 * Модель элемента темы для отображения в списке
 * @param id Уникальный идентификатор темы
 * @param name Название темы
 * @param previewColor Цвет превью (для заглушки или градиента)
 * @param requiredStatus Минимальный статус пользователя для разблокировки
 * @param isExclusive Является ли тема эксклюзивной
 * @param isUnlocked Разблокирована ли тема для текущего пользователя
 */
data class ThemeItem(
    val id: String,
    val name: String,
    val previewColor: String, // HEX цвет или URL изображения
    val requiredStatus: UserStatus,
    val isExclusive: Boolean = false,
    val isUnlocked: Boolean = false
) {
    companion object {
        /**
         * Создаёт список тем по умолчанию
         * В будущем можно загружать из БД или удалённого источника
         */
        fun getDefaultThemes(currentStatus: UserStatus): List<ThemeItem> {
            return listOf(
                // Стандартные темы (всегда доступны)
                ThemeItem(
                    id = "theme_default",
                    name = "Классическая",
                    previewColor = "#FFD700", // Золотой
                    requiredStatus = UserStatus.BEGINNER,
                    isExclusive = false,
                    isUnlocked = true
                ),
                ThemeItem(
                    id = "theme_light",
                    name = "Светлая",
                    previewColor = "#E3F2FD", // Светло-голубой
                    requiredStatus = UserStatus.BEGINNER,
                    isExclusive = false,
                    isUnlocked = true
                ),
                // Эксклюзивные темы (требуют статус)
                ThemeItem(
                    id = "theme_average",
                    name = "Average",
                    previewColor = "#FF8A65", // Оранжевый градиент
                    requiredStatus = UserStatus.AVERAGE,
                    isExclusive = true,
                    isUnlocked = currentStatus.level >= UserStatus.AVERAGE.level
                ),
                ThemeItem(
                    id = "theme_advanced",
                    name = "Advanced",
                    previewColor = "#2196F3", // Синий
                    requiredStatus = UserStatus.ADVANCED,
                    isExclusive = true,
                    isUnlocked = currentStatus.level >= UserStatus.ADVANCED.level
                ),
                ThemeItem(
                    id = "theme_genius",
                    name = "The Genius",
                    previewColor = "#FF9800", // Оранжевый
                    requiredStatus = UserStatus.GENIUS,
                    isExclusive = true,
                    isUnlocked = currentStatus.level >= UserStatus.GENIUS.level
                )
            )
        }
    }
}