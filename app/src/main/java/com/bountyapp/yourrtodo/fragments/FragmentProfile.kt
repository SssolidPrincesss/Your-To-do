// com/bountyapp/yourrtodo/fragments/FragmentProfile.kt
package com.bountyapp.yourrtodo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.databinding.FragmentProfileBinding
import com.bountyapp.yourrtodo.viewmodel.AchievementsViewModel

/**
 * Фрагмент профиля пользователя
 * Отображает детальную информацию о профиле и позволяет редактировать данные
 */
class FragmentProfile : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var achievementsViewModel: AchievementsViewModel



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        achievementsViewModel = ViewModelProvider(requireActivity())[AchievementsViewModel::class.java]

        // Настраиваем кнопку "Назад"
        setupToolbar()

        // Настраиваем UI
        setupClickListeners()

        // Наблюдаем за данными пользователя
        observeUserData()
    }

    /**
     * Настраивает верхнюю панель с кнопкой "Назад"
     */
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.toolbar.title = "Профиль"
    }

    private fun setupClickListeners() {
        // Аватар — выбор фото

    }

    private fun observeUserData() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}