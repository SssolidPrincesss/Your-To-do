// com/bountyapp/yourrtodo/utils/ThemeManager.kt
package com.bountyapp.yourrtodo.utils

import android.content.Context
import android.view.View
import androidx.annotation.DrawableRes
import com.bountyapp.yourrtodo.R

/**
 * Управление глобальной темой приложения
 * Сохраняет выбор и применяет фон к любому View
 */
object ThemeManager {

    private const val PREFS_NAME = "app_prefs"
    private const val KEY_THEME = "selected_theme"

    // ID тем по умолчанию
    const val THEME_DARK = "theme_dark"
    const val THEME_LIGHT = "theme_light"
    const val THEME_AVERAGE = "theme_average"
    const val THEME_ADVANCED = "theme_advanced"
    const val THEME_GENIUS = "theme_genius"
    const val THEME_INSANE = "theme_insane"

    /**
     * Сохраняет ID выбранной темы
     */
    fun saveTheme(context: Context, themeId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, themeId)
            .apply()
    }

    /**
     * Возвращает ID сохранённой темы (по умолчанию — тёмная)
     */
    fun getSavedTheme(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME, THEME_DARK) ?: THEME_DARK
    }

    /**
     * Возвращает ресурс фона по ID темы
     */
    @DrawableRes
    fun getBackgroundDrawable(themeId: String): Int {
        return when (themeId) {
            THEME_LIGHT -> R.drawable.theme_overlay_light  // или твой светлый фон
            THEME_AVERAGE -> R.drawable.genius1
            THEME_ADVANCED -> R.drawable.genius2
            THEME_GENIUS -> R.drawable.genius3
            THEME_INSANE -> R.drawable.genius4
            else -> R.drawable.todobackground  // дефолт — тёмная
        }
    }

    /**
     * Применяет фон темы к любому View (например, корневому Layout)
     */
    fun applyThemeToView(context: Context, view: View, themeId: String) {
        val bgRes = getBackgroundDrawable(themeId)
        view.setBackgroundResource(bgRes)
    }


    /**
     * Применяет сохранённую тему к View
     */
    fun applySavedTheme(context: Context, view: View) {
        val themeId = getSavedTheme(context)
        applyThemeToView(context, view, themeId)
    }

}