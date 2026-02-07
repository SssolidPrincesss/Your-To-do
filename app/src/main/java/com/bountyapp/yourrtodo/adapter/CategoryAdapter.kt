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
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val colorMarker: View = itemView.findViewById(R.id.color_marker)
        val categoryCircle: ImageView = itemView.findViewById(R.id.category_circle)
        val categoryName: TextView = itemView.findViewById(R.id.category_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        val context = holder.itemView.context

        // Устанавливаем цвет маркера
        holder.colorMarker.setBackgroundColor(Color.parseColor(category.color))

        // Устанавливаем название категории
        holder.categoryName.text = category.name

        // Устанавливаем цвет кружочка
        val circleColor = Color.parseColor(category.color)

        if (category.isSelected) {
            // Для выбранной категории - заполненный кружок того же цвета
            holder.categoryCircle.setImageResource(R.drawable.ic_circle_filled)
            holder.categoryCircle.setColorFilter(circleColor, PorterDuff.Mode.SRC_IN)
        } else {
            // Для невыбранной категории - пустой кружок с обводкой того же цвета
            holder.categoryCircle.setImageResource(R.drawable.ic_circle_outline)
            holder.categoryCircle.setColorFilter(circleColor, PorterDuff.Mode.SRC_IN)
        }

        // Обработка клика
        holder.itemView.setOnClickListener {
            onCategoryClick(category)
        }
    }

    override fun getItemCount() = categories.size
}