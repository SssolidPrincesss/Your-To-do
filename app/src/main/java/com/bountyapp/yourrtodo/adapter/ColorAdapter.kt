package com.bountyapp.yourrtodo.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.model.ColorOption

class ColorAdapter(
    private val colors: MutableList<ColorOption>,
    private val onColorSelected: (ColorOption) -> Unit
) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    private var selectedPosition = colors.indexOfFirst { it.isSelected }.takeIf { it != -1 } ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_color_picker, parent, false)
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(colors[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = colors.size

    fun getSelectedColor(): String = colors[selectedPosition].colorHex

    fun addCustomColor(colorOption: ColorOption) {
        // Снимаем выделение с предыдущего цвета
        colors[selectedPosition].isSelected = false
        notifyItemChanged(selectedPosition)

        // Добавляем новый цвет
        colors.add(colorOption)
        val newPosition = colors.size - 1
        colors[newPosition].isSelected = true
        selectedPosition = newPosition
        notifyItemInserted(newPosition)
    }

    inner class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorView: View = itemView.findViewById(R.id.color_view)
        private val checkIcon: ImageView = itemView.findViewById(R.id.check_icon)

        fun bind(colorOption: ColorOption, isSelected: Boolean) {
            // Устанавливаем цвет фона
            colorView.setBackgroundColor(Color.parseColor(colorOption.colorHex))

            // Показываем/скрываем иконку выбора
            checkIcon.visibility = if (isSelected) View.VISIBLE else View.GONE

            // Обработка клика
            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition

                // Обновляем состояние выбора
                colors[previousPosition].isSelected = false
                colors[selectedPosition].isSelected = true

                // Уведомляем об изменениях
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)

                // Вызываем колбэк
                onColorSelected(colorOption)
            }
        }
    }
}