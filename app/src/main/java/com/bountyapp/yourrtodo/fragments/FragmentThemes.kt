// com/bountyapp/yourrtodo/fragments/FragmentThemes.kt
package com.bountyapp.yourrtodo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.adapter.ThemeAdapter
import com.bountyapp.yourrtodo.databinding.FragmentThemesBinding
import com.bountyapp.yourrtodo.viewmodel.ThemesViewModel

/**
 * Фрагмент отображения и выбора тем оформления
 * Следует принципам MVVM: только UI-логика, бизнес-логика в ViewModel
 */
class FragmentThemes : Fragment() {

    private var _binding: FragmentThemesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ThemesViewModel
    private lateinit var themeAdapter: ThemeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Используем ViewBinding для безопасной работы с views
        _binding = FragmentThemesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация ViewModel
        viewModel = ViewModelProvider(this)[ThemesViewModel::class.java]

        // Настройка UI
        setupToolbar()
        setupRecyclerView()
        observeViewModel()
    }

    /**
     * Настраивает верхнюю панель с кнопкой "Назад"
     */
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            // Возврат к экрану настроек через Back Stack
            parentFragmentManager.popBackStack()
        }
        binding.toolbar.title = "Темы"
    }

    /**
     * Настраивает RecyclerView для отображения списка тем
     */
    private fun setupRecyclerView() {
        themeAdapter = ThemeAdapter(
            onThemeClick = { theme ->
                // Передаём выбор в ViewModel
                viewModel.onThemeSelected(theme)
            }
        )

        binding.recyclerViewThemes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = themeAdapter
            // Отключаем анимацию для стабильности
            itemAnimator = null
        }
    }

    /**
     * Наблюдает за изменениями в ViewModel и обновляет UI
     * Разделяет ответственность: ViewModel хранит данные, Fragment отображает
     */
    private fun observeViewModel() {
        // Список тем
        viewModel.themes.observe(viewLifecycleOwner) { themes ->
            themeAdapter.submitList(themes)
        }

        // ID выбранной темы (для подсветки текущего выбора)
        viewModel.selectedThemeId.observe(viewLifecycleOwner) { selectedId ->
            themeAdapter.setSelectedThemeId(selectedId)
        }

        // Сообщения для пользователя
        viewModel.uiMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                // Очищаем сообщение после показа
                viewModel.clearMessage()
            }
        }

        // Событие успешного применения темы
        viewModel.themeApplied.observe(viewLifecycleOwner) { themeId ->
            // Можно добавить анимацию или обновить UI
            themeAdapter.notifyDataSetChanged()
        }
    }

    /**
     * Обновляет список тем при возврате на экран
     * Например, если статус пользователя изменился в другом месте
     */
    override fun onResume() {
        super.onResume()
        viewModel.refreshThemes()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Освобождаем память
    }
}