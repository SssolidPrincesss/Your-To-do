// com/bountyapp/yourrtodo/fragments/FragmentUserTops.kt
package com.bountyapp.yourrtodo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.databinding.FragmentUserTopsBinding

/**
 * Фрагмент топа пользователей
 * Отображает рейтинг игроков по очкам
 */
class FragmentUserTops : Fragment() {

    private var _binding: FragmentUserTopsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserTopsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настраиваем кнопку "Назад"
        setupToolbar()

        // Загружаем топ пользователей
        loadUserTops()
    }

    /**
     * Настраивает верхнюю панель с кнопкой "Назад"
     */
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.toolbar.title = "Топ пользователей"
    }

    private fun loadUserTops() {
        // TODO: Загрузить список лидеров из БД или сервера
        // Пока отображаем заглушку
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}