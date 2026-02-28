// com/bountyapp/yourrtodo/fragments/FragmentStatuses.kt
package com.bountyapp.yourrtodo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.databinding.FragmentStatusesBinding
import com.bountyapp.yourrtodo.model.UserStatus
import com.bountyapp.yourrtodo.viewmodel.AchievementsViewModel

/**
 * Фрагмент информации о статусах
 * Отображает все доступные статусы и требования для их получения
 */
class FragmentStatuses : Fragment() {

    private var _binding: FragmentStatusesBinding? = null
    private val binding get() = _binding!!

    private lateinit var achievementsViewModel: AchievementsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatusesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем ViewModel из Activity (общая для всех фрагментов)
        achievementsViewModel = ViewModelProvider(requireActivity())[AchievementsViewModel::class.java]

        // Настраиваем кнопку "Назад"
        setupToolbar()

        // Загружаем список статусов
        loadStatuses()

        // Наблюдаем за текущим статусом пользователя
        observeCurrentStatus()
    }

    /**
     * Настраивает верхнюю панель с кнопкой "Назад"
     * При нажатии возвращает к экрану настроек через Back Stack
     */
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            // Возврат к предыдущему фрагменту (FragmentSettings)
            parentFragmentManager.popBackStack()
        }
        binding.toolbar.title = "Статусы"
    }

    /**
     * Загружает информацию о всех статусах
     */
    private fun loadStatuses() {
        // Статусы определены в enum UserStatus, отображаем их в UI
        // Подробнее в layout-файле
    }

    /**
     * Наблюдает за текущим статусом пользователя для подсветки
     */
    private fun observeCurrentStatus() {
        achievementsViewModel.currentStatus.observe(viewLifecycleOwner) { status ->
            // Можно подсветить текущий статус в списке
            highlightCurrentStatus(status)
        }
    }

    /**
     * Подсвечивает текущий статус пользователя в списке
     */
    private fun highlightCurrentStatus(status: UserStatus) {
        // TODO: Реализовать подсветку текущего статуса
        // Например, изменить цвет карточки или добавить галочку
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}