// com/bountyapp/yourrtodo/viewmodel/ThemesViewModel.kt
package com.bountyapp.yourrtodo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bountyapp.yourrtodo.data.repository.AchievementRepository
import com.bountyapp.yourrtodo.model.ThemeItem
import com.bountyapp.yourrtodo.model.UserStatus
import com.bountyapp.yourrtodo.utils.ThemeManager
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана выбора тем
 * Хранит состояние UI и бизнес-логику
 */
class ThemesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AchievementRepository(application.applicationContext)

    // === Состояние UI ===

    /**
     * Текущий статус пользователя
     */
    private val _currentStatus = MutableLiveData<UserStatus>()
    val currentStatus: LiveData<UserStatus> = _currentStatus

    /**
     * ID выбранной темы
     */
    private val _selectedThemeId = MutableLiveData<String>()
    val selectedThemeId: LiveData<String> = _selectedThemeId

    /**
     * Сообщение для пользователя (Toast/Snackbar)
     */
    private val _uiMessage = MutableLiveData<String?>()
    val uiMessage: LiveData<String?> = _uiMessage

    /**
     * Эксклюзивные темы для отображения
     */
    private val _exclusiveThemes = MutableLiveData<List<ThemeItem>>()
    val exclusiveThemes: LiveData<List<ThemeItem>> = _exclusiveThemes

    init {
        loadThemes()
    }

    /**
     * Загружает данные о темах
     */
    private fun loadThemes() {
        viewModelScope.launch {
            try {
                // Получаем текущий статус
                val status = repository.getCurrentStatus()
                _currentStatus.postValue(status)

                // Загружаем эксклюзивные темы с актуальным статусом
                val exclusiveList = ThemeItem.getExclusiveThemes(status)
                _exclusiveThemes.postValue(exclusiveList)

                // Загружаем сохранённую тему
                loadSelectedTheme()
            } catch (e: Exception) {
                _uiMessage.postValue("Ошибка загрузки: ${e.message}")
            }
        }
    }

    /**
     * Загружает ID сохранённой темы
     */
    private fun loadSelectedTheme() {
        val savedTheme = ThemeManager.getSavedTheme(getApplication())
        _selectedThemeId.postValue(savedTheme)
    }

    /**
     * Обрабатывает выбор стандартной темы
     */
    fun onStandardThemeSelected(isDark: Boolean) {
        val themeId = if (isDark) "theme_dark" else "theme_light"
        applyTheme(themeId, if (isDark) "Тёмная" else "Светлая")
    }

    /**
     * Обрабатывает выбор эксклюзивной темы
     */
    fun onExclusiveThemeSelected(theme: ThemeItem) {
        if (theme.isUnlocked) {
            applyTheme(theme.id, theme.name)
        } else {
            _uiMessage.postValue(
                "Тема '${theme.name}' доступна со статуса '${theme.requiredStatus.title}'"
            )
        }
    }

    /**
     * Применяет выбранную тему
     */
    private fun applyTheme(themeId: String, themeName: String) {
        viewModelScope.launch {
            try {
                _selectedThemeId.postValue(themeId)

                // 👇 Сохраняем тему глобально
                ThemeManager.saveTheme(getApplication(), themeId)

                // Опционально: сразу применяем, если нужно (но MainActivity подхватит сам)
                // ThemeManager.applyThemeToView(getApplication(), someView, themeId)

                _uiMessage.postValue("Тема '$themeName' применена")
            } catch (e: Exception) {
                _uiMessage.postValue("Ошибка применения темы")
            }
        }
    }

    /**
     * Сбрасывает сообщение после показа
     */
    fun clearMessage() {
        _uiMessage.postValue(null)
    }

    /**
     * Обновляет список тем (при возврате на экран)
     */
    fun refreshThemes() {
        loadThemes()
    }
}