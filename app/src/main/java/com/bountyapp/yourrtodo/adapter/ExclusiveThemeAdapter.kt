// com/bountyapp/yourrtodo/adapter/ExclusiveThemeAdapter.kt
package com.bountyapp.yourrtodo.adapter

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
 * Адаптер для списка эксклюзивных тем с превью
 */
class ExclusiveThemeAdapter(
    private val onThemeClick: (ThemeItem) -> Unit
) : ListAdapter<ThemeItem, ExclusiveThemeAdapter.ThemeViewHolder>(ThemeDiffCallback()) {

    private var selectedThemeId: String? = null

    fun setSelectedThemeId(themeId: String?) {
        selectedThemeId = themeId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exclusive_theme, parent, false)
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
        private val overlay: View = itemView.findViewById(R.id.theme_overlay)

        fun bind(theme: ThemeItem, isSelected: Boolean) {
            themeName.text = theme.name

            // Превью темы
            if (theme.previewDrawable != 0) {
                previewImage.setImageResource(theme.previewDrawable)
            }

            // Статус блокировки
            if (theme.isUnlocked) {
                lockIcon.visibility = View.GONE
                statusLabel.visibility = View.GONE
                overlay.visibility = View.GONE
                itemView.alpha = 1.0f
            } else {
                lockIcon.visibility = View.VISIBLE
                statusLabel.visibility = View.VISIBLE
                statusLabel.text = "Закрыт: ${theme.requiredStatus.title}"
                overlay.visibility = View.VISIBLE
                itemView.alpha = 0.7f
            }

            // Маркер выбранной темы
            checkmark.visibility = if (isSelected) View.VISIBLE else View.GONE

            // Подсветка выбранной карточки
            val cardView = itemView as? com.google.android.material.card.MaterialCardView
            cardView?.apply {
                strokeWidth = if (isSelected) 4 else 0
                strokeColor = itemView.context.getColor(R.color.yellow_star)
            }
        }
    }

    private class ThemeDiffCallback : DiffUtil.ItemCallback<ThemeItem>() {
        override fun areItemsTheSame(oldItem: ThemeItem, newItem: ThemeItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ThemeItem, newItem: ThemeItem) =
            oldItem == newItem
    }
}