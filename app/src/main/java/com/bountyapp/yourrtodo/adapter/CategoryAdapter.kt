package com.bountyapp.yourrtodo.adapter

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bountyapp.yourrtodo.AdvancedColorPickerDialog
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.model.Category
import com.bountyapp.yourrtodo.model.ColorOption

class CategoryAdapter(
    private val categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit,
    private val onAddCategoryClick: () -> Unit,
    private val onCreateCategory: (String, String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_CATEGORY = 0
        private const val VIEW_TYPE_ADD_BUTTON = 1
        private const val VIEW_TYPE_ADD_INPUT = 2
    }

    private var isAddingMode = false
    private var selectedColor = "#2196F3"

    override fun getItemViewType(position: Int): Int {
        return when {
            isAddingMode && position == categories.size + 1 -> VIEW_TYPE_ADD_INPUT
            position == categories.size -> VIEW_TYPE_ADD_BUTTON
            else -> VIEW_TYPE_CATEGORY
        }
    }

    override fun getItemCount(): Int {
        return categories.size + 1 + if (isAddingMode) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CATEGORY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.category_item, parent, false)
                CategoryViewHolder(view)
            }
            VIEW_TYPE_ADD_BUTTON -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.add_category_item, parent, false)
                AddCategoryViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.add_category_input, parent, false)
                AddCategoryInputViewHolder(view)
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
                holder.bind()
            }
            is AddCategoryInputViewHolder -> {
                holder.bind()
            }
        }
    }

    fun enterAddingMode() {
        if (!isAddingMode) {
            isAddingMode = true
            notifyItemInserted(categories.size + 1)
        }
    }

    fun exitAddingMode() {
        if (isAddingMode) {
            isAddingMode = false
            notifyItemRemoved(categories.size + 1)
        }
    }

    inner class AddCategoryInputViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameInput: EditText = itemView.findViewById(R.id.category_name_input)
        private val cancelButton: TextView = itemView.findViewById(R.id.cancel_button)
        private val addButton: TextView = itemView.findViewById(R.id.add_button)

        // Элементы для выбора цвета
        private val colorBlue = itemView.findViewById<FrameLayout>(R.id.color_blue)
        private val colorGreen = itemView.findViewById<FrameLayout>(R.id.color_green)
        private val colorOrange = itemView.findViewById<FrameLayout>(R.id.color_orange)
        private val colorYellow = itemView.findViewById<FrameLayout>(R.id.color_yellow)
        private val colorPurple = itemView.findViewById<FrameLayout>(R.id.color_purple)
        private val colorRed = itemView.findViewById<FrameLayout>(R.id.color_red)
        private val colorPink = itemView.findViewById<FrameLayout>(R.id.color_pink)

        private val checkBlue = itemView.findViewById<ImageView>(R.id.check_blue)
        private val checkGreen = itemView.findViewById<ImageView>(R.id.check_green)
        private val checkOrange = itemView.findViewById<ImageView>(R.id.check_orange)
        private val checkYellow = itemView.findViewById<ImageView>(R.id.check_yellow)
        private val checkPurple = itemView.findViewById<ImageView>(R.id.check_purple)
        private val checkRed = itemView.findViewById<ImageView>(R.id.check_red)
        private val checkPink = itemView.findViewById<ImageView>(R.id.check_pink)

        private val selectedColorPreview = itemView.findViewById<View>(R.id.selected_color_preview)
        private val btnCustomColorContainer = itemView.findViewById<LinearLayout>(R.id.btn_custom_color_container)

        fun bind() {
            // Сброс значений
            nameInput.setText("")
            nameInput.error = null

            // Устанавливаем цвет по умолчанию (синий)
            selectedColor = "#2196F3"
            updateSelectedColor(selectedColor)
            updateCheckMarks(checkBlue)

            // Настройка выбора цветов
            setupColorSelection()

            // Обработка кнопки кастомного цвета
            btnCustomColorContainer.setOnClickListener {
                openColorPicker()
            }

            // Обработка кнопки отмены
            cancelButton.setOnClickListener {
                exitAddingMode()
            }

            // Обработка кнопки добавления
            addButton.setOnClickListener {
                val categoryName = nameInput.text.toString().trim()
                if (categoryName.isNotEmpty()) {
                    onCreateCategory(categoryName, selectedColor)
                    exitAddingMode()
                } else {
                    nameInput.error = "Введите название категории"
                }
            }
        }

        private fun setupColorSelection() {
            val colorMap = mapOf(
                colorBlue to Pair("#2196F3", checkBlue),
                colorGreen to Pair("#4CAF50", checkGreen),
                colorOrange to Pair("#FF9800", checkOrange),
                colorYellow to Pair("#FFC107", checkYellow),
                colorPurple to Pair("#9C27B0", checkPurple),
                colorRed to Pair("#F44336", checkRed),
                colorPink to Pair("#E91E63", checkPink)
            )

            colorMap.forEach { (view, pair) ->
                val (colorHex, checkView) = pair
                view.setOnClickListener {
                    selectedColor = colorHex
                    updateSelectedColor(colorHex)
                    updateCheckMarks(checkView)
                }
            }
        }

        private fun updateSelectedColor(colorHex: String) {
            selectedColorPreview.backgroundTintList = android.content.res.ColorStateList.valueOf(
                Color.parseColor(colorHex)
            )
        }

        private fun updateCheckMarks(selectedCheck: ImageView) {
            // Скрываем все галочки
            listOf(checkBlue, checkGreen, checkOrange, checkYellow,
                checkPurple, checkRed, checkPink).forEach { it.visibility = View.GONE }

            // Показываем галочку на выбранном цвете
            selectedCheck.visibility = View.VISIBLE
        }

        private fun openColorPicker() {
            val dialog = AdvancedColorPickerDialog { colorOption ->
                // Добавляем кастомный цвет
                selectedColor = colorOption.colorHex
                updateSelectedColor(colorOption.colorHex)

                // Скрываем все галочки предустановленных цветов
                listOf(checkBlue, checkGreen, checkOrange, checkYellow,
                    checkPurple, checkRed, checkPink).forEach { it.visibility = View.GONE }

                android.util.Log.d("CategoryAdapter", "Custom color selected: ${colorOption.colorHex}")
            }

            try {
                dialog.show((itemView.context as androidx.fragment.app.FragmentActivity).supportFragmentManager, "AdvancedColorPickerDialog")
            } catch (e: Exception) {
                android.util.Log.e("CategoryAdapter", "Error showing color picker", e)
            }
        }
    }

    inner class AddCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            itemView.setOnClickListener {
                android.util.Log.d("CategoryAdapter", "Add button clicked")
                onAddCategoryClick()
                enterAddingMode()
            }
        }
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorMarker: View = itemView.findViewById(R.id.color_marker)
        private val categoryCircle: ImageView = itemView.findViewById(R.id.category_circle)
        private val categoryName: TextView = itemView.findViewById(R.id.category_name)

        fun bind(category: Category, onCategoryClick: (Category) -> Unit) {
            colorMarker.setBackgroundColor(Color.parseColor(category.color))
            categoryName.text = category.name

            val circleColor = Color.parseColor(category.color)

            if (category.isSelected) {
                categoryCircle.setImageResource(R.drawable.ic_circle_filled)
                categoryCircle.setColorFilter(circleColor, PorterDuff.Mode.SRC_IN)
            } else {
                categoryCircle.setImageResource(R.drawable.ic_circle_outline)
                categoryCircle.setColorFilter(circleColor, PorterDuff.Mode.SRC_IN)
            }

            itemView.setOnClickListener { onCategoryClick(category) }
        }
    }
}