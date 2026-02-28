// com/bountyapp.yourrtodo/fragments/FragmentSettings.kt
package com.bountyapp.yourrtodo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.databinding.FragmentSettingsBinding
import com.bountyapp.yourrtodo.viewmodel.AchievementsViewModel

/**
 * Главный фрагмент настроек
 * Обрабатывает навигацию к подэкранам через FragmentManager
 */
class FragmentSettings : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var achievementsViewModel: AchievementsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем ViewModel из Activity (общая для всех фрагментов)
        achievementsViewModel = ViewModelProvider(requireActivity())[AchievementsViewModel::class.java]

        // Наблюдаем за статусом пользователя для обновления UI
        observeUserStatus()

        // Настраиваем обработчики кнопок
        setupClickListeners()
    }

    /**
     * Наблюдает за изменением статуса пользователя
     * Обновляет отображение статуса в профиле
     */
    private fun observeUserStatus() {
        achievementsViewModel.currentStatus.observe(viewLifecycleOwner) { status ->
            binding.tvUserStatus.text = status.title
            // Можно обновить цвет статуса
        }

        achievementsViewModel.userStats.observe(viewLifecycleOwner) { stats ->
            stats?.let {
                binding.tvRating.text = it.totalPoints.toString()
            }
        }
    }

    /**
     * Настраивает обработчики нажатий на кнопки меню
     * Каждая кнопка открывает соответствующий фрагмент с добавлением в Back Stack
     */
    private fun setupClickListeners() {
        // Аватар - выбор фото
        binding.ivUserAvatar.setOnClickListener {
            // TODO: Открыть галерею для выбора фото
            Toast.makeText(requireContext(), "Выбор аватара", Toast.LENGTH_SHORT).show()
        }

        // Кнопка: Темы
        binding.btnThemes.setOnClickListener {
            navigateToFragment(FragmentThemes(), "FragmentThemes")
        }

        // Кнопка: Профиль
        binding.btnProfile.setOnClickListener {
            navigateToFragment(FragmentProfile(), "FragmentProfile")
        }

        // Кнопка: Топы пользователей
        binding.btnUserTops.setOnClickListener {
            navigateToFragment(FragmentUserTops(), "FragmentUserTops")
        }

        // Кнопка: Поддержать нас → теперь открывает FragmentLink
        binding.btnSupport.setOnClickListener {
            navigateToFragment(FragmentLink(), "FragmentLink")
        }

        // Кнопка: Статусы
        binding.btnStatuses.setOnClickListener {
            navigateToFragment(FragmentStatuses(), "FragmentStatuses")
        }

        // Кнопка: Связаться с нами
        binding.btnContact.setOnClickListener {
            navigateToFragment(FragmentLink(), "FragmentLink")
        }
    }

    /**
     * Навигация к другому фрагменту с добавлением в Back Stack
     * @param fragment Фрагмент для отображения
     * @param tag Тег для идентификации в Back Stack
     *
     * Это обеспечивает корректную работу кнопки "Назад"
     */
    private fun navigateToFragment(fragment: Fragment, tag: String) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .addToBackStack(tag)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}