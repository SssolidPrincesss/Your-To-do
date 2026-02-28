// com/bountyapp/yourrtodo/fragments/FragmentLink.kt
package com.bountyapp.yourrtodo.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.databinding.FragmentLinkBinding

/**
 * Фрагмент внешних ссылок
 * Отображает ссылки на социальные сети, сайт проекта и другие ресурсы
 */
class FragmentLink : Fragment() {

    private var _binding: FragmentLinkBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLinkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настраиваем кнопку "Назад"
        setupToolbar()

        // Настраиваем обработчики ссылок
        setupLinkClickListeners()
    }

    /**
     * Настраивает верхнюю панель с кнопкой "Назад"
     */
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.toolbar.title = "Ссылки"
    }

    /**
     * Настраивает обработчики нажатий на кнопки ссылок
     * Каждая кнопка открывает URL в браузере
     */
    private fun setupLinkClickListeners() {

    }

    /**
     * Открывает URL в браузере по умолчанию
     * @param url Ссылка для открытия
     */
    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            // Если нет браузера, показываем ошибку
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}