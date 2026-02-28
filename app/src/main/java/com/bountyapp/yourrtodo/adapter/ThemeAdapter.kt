// com/bountyapp/yourrtodo/adapter/ThemeAdapter.kt
package com.bountyapp.yourrtodo.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.model.ThemeItem

/**
 * Адаптер для отображения списка тем
 * Использует ListAdapter для автоматического обновления UI
 */
class ThemeAdapter(
    private val onThemeClick: (ThemeItem) -> Unit
) : ListAdapter<ThemeItem, ThemeAdapter.ThemeViewHolder>(ThemeDiffCallback()) {

    private var selectedThemeId: String? = null

    /**
     * Устанавливает ID выбранной темы для подсветки
     */
    fun setSelectedThemeId(themeId: String?) {
        selectedThemeId = themeId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_theme, parent, false)
        return ThemeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        val theme = getItem(position)
        holder.bind(theme, theme.id == selectedThemeId)
        holder.itemView.setOnClickListener { onThemeClick(theme) }
    }

    inner class ThemeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val previewImage: ImageView = itemView.findViewById(R.id.theme_preview)
        private val themeName: TextView = itemView.findViewById(R.id.theme_name)
        private val lockIcon: ImageView = itemView.findViewById(R.id.lock_icon)
        private val statusLabel: TextView = itemView.findViewById(R.id.status_label)
        private val checkmark: ImageView = itemView.findViewById(R.id.selected_checkmark)

        fun bind(theme: ThemeItem, isSelected: Boolean) {
            // Название темы
            themeName.text = theme.name

            // Цвет превью
            previewImage.setBackgroundColor(Color.parseColor(theme.previewColor))

            // Статус блокировки
            if (theme.isUnlocked) {
                lockIcon.visibility = View.GONE
                statusLabel.visibility = View.GONE
                itemView.alpha = 1.0f
            } else {
                lockIcon.visibility = View.VISIBLE
                statusLabel.visibility = View.VISIBLE
                statusLabel.text = "Закрыт: ${theme.requiredStatus.title}"
                itemView.alpha = 0.6f
            }

            // Маркер выбранной темы
            checkmark.visibility = if (isSelected) View.VISIBLE else View.GONE

            // Метка эксклюзивности
            if (theme.isExclusive) {
                // Можно добавить бейдж "Exclusive"
            }
        }
    }

    /**
     * DiffUtil для эффективного обновления списка
     * Сравнивает элементы и обновляет только изменённые
     */
    private class ThemeDiffCallback : DiffUtil.ItemCallback<ThemeItem>() {
        override fun areItemsTheSame(oldItem: ThemeItem, newItem: ThemeItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ThemeItem, newItem: ThemeItem): Boolean {
            return oldItem == newItem
        }
    }
}