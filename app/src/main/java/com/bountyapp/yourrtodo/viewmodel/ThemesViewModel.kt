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
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана выбора тем
 * Следует принципам MVVM: хранит состояние UI и бизнес-логику
 */
class ThemesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AchievementRepository(application.applicationContext)

    // === Состояние UI ===

    /**
     * Список всех тем для отображения
     * Обновляется при загрузке данных
     */
    private val _themes = MutableLiveData<List<ThemeItem>>()
    val themes: LiveData<List<ThemeItem>> = _themes

    /**
     * Текущий статус пользователя (нужен для разблокировки тем)
     */
    private val _currentStatus = MutableLiveData<UserStatus>()
    val currentStatus: LiveData<UserStatus> = _currentStatus

    /**
     * ID выбранной темы (сохраняется в БД или SharedPreferences)
     */
    private val _selectedThemeId = MutableLiveData<String>()
    val selectedThemeId: LiveData<String> = _selectedThemeId

    /**
     * Сообщение для показа пользователю (Toast/Snackbar)
     */
    private val _uiMessage = MutableLiveData<String?>()
    val uiMessage: LiveData<String?> = _uiMessage

    /**
     * Событие: тема успешно применена
     */
    private val _themeApplied = MutableLiveData<String>()
    val themeApplied: LiveData<String> = _themeApplied

    init {
        loadThemes()
    }

    /**
     * Загружает список тем с учётом текущего статуса пользователя
     * Вызывается при создании ViewModel
     */
    private fun loadThemes() {
        viewModelScope.launch {
            try {
                // Получаем текущий статус из репозитория
                val status = repository.getCurrentStatus()
                _currentStatus.postValue(status)

                // Создаём список тем с актуальным статусом разблокировки
                val themesList = ThemeItem.getDefaultThemes(status)
                _themes.postValue(themesList)

                // Загружаем сохранённую тему (из SharedPreferences или БД)
                loadSelectedTheme()
            } catch (e: Exception) {
                _uiMessage.postValue("Ошибка загрузки тем: ${e.message}")
            }
        }
    }

    /**
     * Загружает ID сохранённой темы
     * В реальном проекте - из SharedPreferences или Room
     */
    private fun loadSelectedTheme() {
        // TODO: Загрузить из хранилища
        // Для примера устанавливаем тему по умолчанию
        _selectedThemeId.postValue("theme_default")
    }

    /**
     * Обрабатывает нажатие на тему
     * @param theme Выбранная тема
     * @return true если тема применена, false если заблокирована
     */
    fun onThemeSelected(theme: ThemeItem): Boolean {
        return if (theme.isUnlocked) {
            applyTheme(theme)
            true
        } else {
            // Тема заблокирована - показываем сообщение
            _uiMessage.postValue(
                "Тема '${theme.name}' доступна со статуса '${theme.requiredStatus.title}'"
            )
            false
        }
    }

    /**
     * Применяет выбранную тему
     * В реальном проекте - сохраняет в БД/SharedPreferences и обновляет UI
     */
    private fun applyTheme(theme: ThemeItem) {
        viewModelScope.launch {
            try {
                // Сохраняем выбор
                _selectedThemeId.postValue(theme.id)

                // TODO: Сохранить в SharedPreferences
                // val prefs = getApplication<Application>().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                // prefs.edit().putString("selected_theme", theme.id).apply()

                // TODO: Применить тему ко всему приложению
                // ThemeManager.applyTheme(theme.id)

                _themeApplied.postValue(theme.id)
                _uiMessage.postValue("Тема '${theme.name}' применена")
            } catch (e: Exception) {
                _uiMessage.postValue("Ошибка применения темы: ${e.message}")
            }
        }
    }

    /**
     * Сбрасывает сообщение после показа
     * Вызывается из Fragment после отображения Toast
     */
    fun clearMessage() {
        _uiMessage.postValue(null)
    }

    /**
     * Принудительно обновляет список тем
     * Используется при возврате на экран после изменения статуса
     */
    fun refreshThemes() {
        loadThemes()
    }
}