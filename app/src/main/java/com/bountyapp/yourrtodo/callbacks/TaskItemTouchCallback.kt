package com.bountyapp.yourrtodo.callbacks

import android.graphics.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.adapter.TaskAdapter

class TaskItemTouchCallback(
    private val swipeCallback: TaskSwipeCallback,
    private val swipeDirection: Int = ItemTouchHelper.LEFT
) : ItemTouchHelper.SimpleCallback(
    0,
    swipeDirection
) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val gradientColors = intArrayOf(
        Color.parseColor("#FFFF4444"),
        Color.parseColor("#FFFF0000")
    )

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    // Для рисования иконки
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    // Радиус закругления
    private val cornerRadius = 40f

    // Размер иконки
    private val iconSize = 48f

    // Для хранения bitmap иконки
    private var trashIcon: Bitmap? = null

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.bindingAdapterPosition
        if (position != RecyclerView.NO_POSITION) {
            // В методе onSwiped нет параметра recyclerView, поэтому используем другой подход
            // Просто передаем позицию в колбэк, а проверку делаем в onChildDraw
            swipeCallback.onTaskSwiped(position)
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        // Получаем позицию и проверяем, является ли элемент заголовком
        val position = viewHolder.bindingAdapterPosition
        if (position != RecyclerView.NO_POSITION) {
            val adapter = recyclerView.adapter
            if (adapter is TaskAdapter) {
                val task = adapter.getTaskAtPosition(position)
                // Если это заголовок секции, не даем свайпать
                if (task.isSectionHeader) {
                    // Важно: возвращаем dX = 0, чтобы элемент не двигался
                    super.onChildDraw(c, recyclerView, viewHolder, 0f, dY, actionState, isCurrentlyActive)
                    return
                }
            }
        }

        // Ленивая загрузка иконки при первом использовании
        if (trashIcon == null) {
            try {
                val drawable = ContextCompat.getDrawable(
                    recyclerView.context,
                    R.drawable.ic_delete
                )
                trashIcon = Bitmap.createBitmap(
                    iconSize.toInt(),
                    iconSize.toInt(),
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(trashIcon!!)
                drawable?.setBounds(0, 0, iconSize.toInt(), iconSize.toInt())
                drawable?.draw(canvas)
            } catch (e: Exception) {
                trashIcon = null
            }
        }

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val itemView = viewHolder.itemView

            when {
                // Свайп влево
                dX < 0 -> {
                    val background = RectF(
                        itemView.right + dX,
                        itemView.top.toFloat(),
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat()
                    )

                    // Создаем градиент
                    val gradient = LinearGradient(
                        background.left, background.top,
                        background.right, background.top,
                        gradientColors,
                        null,
                        Shader.TileMode.CLAMP
                    )
                    paint.shader = gradient

                    // Рисуем закругленный прямоугольник
                    c.drawRoundRect(background, cornerRadius, cornerRadius, paint)
                    paint.shader = null

                    val width = -dX

                    if (width > 120) {
                        // Рисуем иконку
                        if (trashIcon != null) {
                            val iconLeft = itemView.right - iconSize - 40f
                            val iconTop = itemView.top + (itemView.height - iconSize) / 2
                            c.drawBitmap(trashIcon!!, iconLeft, iconTop, null)
                        }

                        // Рисуем текст
                        val text = "Удалить"
                        val textWidth = textPaint.measureText(text)

                        if (width > textWidth + iconSize + 80) {
                            val textX = itemView.right - iconSize - 60f - textWidth / 2
                            val textY = itemView.top + (itemView.height - textPaint.ascent() - textPaint.descent()) / 2
                            c.drawText(text, textX, textY, textPaint)
                        }
                    }
                }
                // Свайп вправо
                dX > 0 -> {
                    val background = RectF(
                        itemView.left.toFloat(),
                        itemView.top.toFloat(),
                        itemView.left + dX,
                        itemView.bottom.toFloat()
                    )

                    val gradient = LinearGradient(
                        background.left, background.top,
                        background.right, background.top,
                        gradientColors,
                        null,
                        Shader.TileMode.CLAMP
                    )
                    paint.shader = gradient

                    c.drawRoundRect(background, cornerRadius, cornerRadius, paint)
                    paint.shader = null

                    val width = dX

                    if (width > 120) {
                        // Рисуем иконку
                        if (trashIcon != null) {
                            val iconLeft = itemView.left + 40f
                            val iconTop = itemView.top + (itemView.height - iconSize) / 2
                            c.drawBitmap(trashIcon!!, iconLeft, iconTop, null)
                        }

                        // Рисуем текст
                        val text = "Удалить"
                        val textWidth = textPaint.measureText(text)

                        if (width > textWidth + iconSize + 80) {
                            val textX = itemView.left + iconSize + 60f + textWidth / 2
                            val textY = itemView.top + (itemView.height - textPaint.ascent() - textPaint.descent()) / 2
                            c.drawText(text, textX, textY, textPaint)
                        }
                    }
                }
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}