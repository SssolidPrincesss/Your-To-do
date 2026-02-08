package com.bountyapp.yourrtodo.adapter

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.model.Category

class CategoryAdapter(
    private val categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit,
    private val onAddCategoryClick: () -> Unit // Новый параметр
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_CATEGORY = 0
        private const val VIEW_TYPE_ADD_BUTTON = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == categories.size) VIEW_TYPE_ADD_BUTTON else VIEW_TYPE_CATEGORY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CATEGORY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.category_item, parent, false)
                CategoryViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.add_category_item, parent, false)
                AddCategoryViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CategoryViewHolder -> {
                val category = categories[position]
                holder.bind(category, onCategoryClick)
            }
            is AddCategoryViewHolder -> {
                holder.itemView.setOnClickListener { onAddCategoryClick() }
            }
        }
    }

    override fun getItemCount() = categories.size + 1 // +1 для кнопки

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorMarker: View = itemView.findViewById(R.id.color_marker)
        private val categoryCircle: ImageView = itemView.findViewById(R.id.category_circle)
        private val categoryName: TextView = itemView.findViewById(R.id.category_name)

        fun bind(category: Category, onCategoryClick: (Category) -> Unit) {
            val context = itemView.context

            // Устанавливаем цвет маркера
            colorMarker.setBackgroundColor(Color.parseColor(category.color))

            // Устанавливаем название категории
            categoryName.text = category.name

            // Устанавливаем цвет кружочка
            val circleColor = Color.parseColor(category.color)

            if (category.isSelected) {
                // Для выбранной категории - заполненный кружок того же цвета
                categoryCircle.setImageResource(R.drawable.ic_circle_filled)
                categoryCircle.setColorFilter(circleColor, PorterDuff.Mode.SRC_IN)
            } else {
                // Для невыбранной категории - пустой кружок с обводкой того же цвета
                categoryCircle.setImageResource(R.drawable.ic_circle_outline)
                categoryCircle.setColorFilter(circleColor, PorterDuff.Mode.SRC_IN)
            }

            // Обработка клика
            itemView.setOnClickListener { onCategoryClick(category) }
        }
    }

    class AddCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}