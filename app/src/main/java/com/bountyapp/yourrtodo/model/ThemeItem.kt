// com/bountyapp/yourrtodo/model/ThemeItem.kt
package com.bountyapp.yourrtodo.model

import com.bountyapp.yourrtodo.R

/**
 * Модель элемента темы для отображения в списке
 */
data class ThemeItem(
    val id: String,
    val name: String,
    val previewDrawable: Int,      // Ресурс превью (drawable)
    val overlayDrawable: Int = 0,  // Ресурс затемнения (опционально)
    val requiredStatus: UserStatus,
    val isExclusive: Boolean = false,
    val isUnlocked: Boolean = false,
    val isDark: Boolean = false    // Для стандартных тем
) {
    companion object {
        /**
         * Создаёт список стандартных тем (всегда доступны)
         */
        fun getStandardThemes(): List<ThemeItem> {
            return listOf(
                ThemeItem(
                    id = "theme_dark",
                    name = "Тёмная",
                    previewDrawable = R.drawable.todobackground,
                    overlayDrawable = R.drawable.theme_overlay_dark,
                    requiredStatus = UserStatus.BEGINNER,
                    isExclusive = false,
                    isUnlocked = true,
                    isDark = true
                ),
                ThemeItem(
                    id = "theme_light",
                    name = "Светлая",
                    previewDrawable = R.drawable.todobackground,
                    overlayDrawable = R.drawable.theme_overlay_light,
                    requiredStatus = UserStatus.BEGINNER,
                    isExclusive = false,
                    isUnlocked = true,
                    isDark = false
                )
            )
        }

        /**
         * Создаёт список эксклюзивных тем с учётом статуса пользователя
         */
        fun getExclusiveThemes(currentStatus: UserStatus): List<ThemeItem> {
            return listOf(
                ThemeItem(
                    id = "theme_average",
                    name = "Average",
                    previewDrawable = R.drawable.genius1,
                    requiredStatus = UserStatus.AVERAGE,
                    isExclusive = true,
                    isUnlocked = currentStatus.level >= UserStatus.AVERAGE.level
                ),
                ThemeItem(
                    id = "theme_advanced",
                    name = "Advanced",
                    previewDrawable = R.drawable.genius2,
                    requiredStatus = UserStatus.ADVANCED,
                    isExclusive = true,
                    isUnlocked = currentStatus.level >= UserStatus.ADVANCED.level
                ),
                ThemeItem(
                    id = "theme_genius",
                    name = "The Genius",
                    previewDrawable = R.drawable.genius3,
                    requiredStatus = UserStatus.GENIUS,
                    isExclusive = true,
                    isUnlocked = currentStatus.level >= UserStatus.GENIUS.level
                ),
                ThemeItem(
                    id = "theme_insane",
                    name = "Insane",
                    previewDrawable = R.drawable.genius4,
                    requiredStatus = UserStatus.INSANE,
                    isExclusive = true,
                    isUnlocked = currentStatus.level >= UserStatus.INSANE.level
                )
            )
        }
    }
}