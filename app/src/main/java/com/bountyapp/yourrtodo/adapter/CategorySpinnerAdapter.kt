package com.bountyapp.yourrtodo.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.model.Category

class CategorySpinnerAdapter(
    context: Context,
    private val categories: List<Category>
) : ArrayAdapter<Category>(context, 0, categories) {

    override fun getCount(): Int = categories.size

    override fun getItem(position: Int): Category = categories[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent, false)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent, true)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup, isDropDown: Boolean): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.item_category_spinner,
            parent,
            false
        )

        val category = getItem(position)
        val tvName = view.findViewById<TextView>(R.id.tv_category_name)
        val viewColor = view.findViewById<View>(R.id.view_color)

        tvName.text = category.name

        // Устанавливаем цвет
        try {
            val colorInt = Color.parseColor(category.color)
            viewColor.setBackgroundColor(colorInt)

            // Для выпадающего списка тоже применяем цвет
            if (isDropDown) {
                tvName.setTextColor(colorInt)
            }
        } catch (e: Exception) {
            viewColor.setBackgroundColor(Color.GRAY)
        }

        return view
    }
}