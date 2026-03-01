// com/bountyapp/yourrtodo/fragments/FragmentThemes.kt
package com.bountyapp.yourrtodo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.adapter.ExclusiveThemeAdapter
import com.bountyapp.yourrtodo.databinding.FragmentThemesBinding
import com.bountyapp.yourrtodo.viewmodel.ThemesViewModel

/**
 * Фрагмент отображения и выбора тем оформления
 * Соответствует макету: два блока (стандартные + эксклюзивные)
 * Стандартные темы имеют превью с изображениями
 */
class FragmentThemes : Fragment() {

    private var _binding: FragmentThemesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ThemesViewModel
    private lateinit var exclusiveAdapter: ExclusiveThemeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThemesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[ThemesViewModel::class.java]

        setupToolbar()
        setupStandardThemes()
        setupExclusiveRecyclerView()
        observeViewModel()
    }

    /**
     * Настраивает верхнюю панель только с кнопкой "Назад"
     */
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
        // Заголовок не устанавливаем - только стрелка
    }

    /**
     * Настраивает обработчики для стандартных тем с превью
     */
    private fun setupStandardThemes() {
        // Тёмная тема
        binding.cardDarkTheme.apply {
            setOnClickListener {
                viewModel.onStandardThemeSelected(isDark = true)
            }
            // Анимация нажатия
            isClickable = true
            isFocusable = true
        }

        // Светлая тема
        binding.cardLightTheme.apply {
            setOnClickListener {
                viewModel.onStandardThemeSelected(isDark = false)
            }
            // Анимация нажатия
            isClickable = true
            isFocusable = true
        }
    }

    /**
     * Настраивает RecyclerView для эксклюзивных тем
     */
    private fun setupExclusiveRecyclerView() {
        exclusiveAdapter = ExclusiveThemeAdapter { theme ->
            viewModel.onExclusiveThemeSelected(theme)
        }

        binding.recyclerViewExclusive.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = exclusiveAdapter
            // Отключаем анимацию для стабильности
            itemAnimator = null
            // Отключаем оверсколл для красоты
            overScrollMode = View.OVER_SCROLL_NEVER
        }
    }

    /**
     * Наблюдает за изменениями в ViewModel
     */
    private fun observeViewModel() {
        // Выбранная тема - обновляем маркеры
        viewModel.selectedThemeId.observe(viewLifecycleOwner) { selectedId ->
            updateStandardThemeMarkers(selectedId)
            exclusiveAdapter.setSelectedThemeId(selectedId)
        }

        // Список эксклюзивных тем
        viewModel.exclusiveThemes.observe(viewLifecycleOwner) { themes ->
            exclusiveAdapter.submitList(themes)
            fixRecyclerViewHeight(binding.recyclerViewExclusive)
        }

        // Сообщения
        viewModel.uiMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearMessage()
            }
        }
    }

    /**
     * Обновляет маркеры на стандартных темах
     */
    private fun updateStandardThemeMarkers(selectedId: String?) {
        binding.checkmarkDark.visibility =
            if (selectedId == "theme_dark") View.VISIBLE else View.GONE
        binding.checkmarkLight.visibility =
            if (selectedId == "theme_light") View.VISIBLE else View.GONE

        // Добавляем визуальную подсветку выбранной карточки
        binding.cardDarkTheme.strokeWidth =
            if (selectedId == "theme_dark") 4 else 0
        binding.cardLightTheme.strokeWidth =
            if (selectedId == "theme_light") 4 else 0

        binding.cardDarkTheme.strokeColor =
            ContextCompat.getColor(requireContext(), R.color.yellow_star)
        binding.cardLightTheme.strokeColor =
            ContextCompat.getColor(requireContext(), R.color.yellow_star)
    }

    private fun fixRecyclerViewHeight(recyclerView: RecyclerView) {
        val adapter = recyclerView.adapter ?: return

        recyclerView.post {
            var totalHeight = 0
            for (i in 0 until adapter.itemCount) {
                val viewType = adapter.getItemViewType(i)
                val holder = adapter.onCreateViewHolder(recyclerView, viewType)
                @Suppress("UNCHECKED_CAST")
                adapter.onBindViewHolder(holder as RecyclerView.ViewHolder, i)

                holder.itemView.measure(
                    View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.UNSPECIFIED
                )
                totalHeight += holder.itemView.measuredHeight

                val params = holder.itemView.layoutParams as? ViewGroup.MarginLayoutParams
                if (params != null) {
                    totalHeight += params.topMargin + params.bottomMargin
                }
            }

            totalHeight += recyclerView.paddingTop + recyclerView.paddingBottom
            recyclerView.layoutParams.height = totalHeight
            recyclerView.requestLayout()
        }
    }



    /**
     * Обновляет темы при возврате на экран
     */
    override fun onResume() {
        super.onResume()
        viewModel.refreshThemes()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}