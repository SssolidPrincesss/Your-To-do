package com.bountyapp.yourrtodo.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class ColorPaletteView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var bitmap: Bitmap? = null
    private var selectorX = 0f
    private var selectorY = 0f
    private var onColorSelectedListener: ((Int) -> Unit)? = null

    var currentColor: Int = Color.RED
        private set

    init {
        selectorPaint.style = Paint.Style.STROKE
        selectorPaint.strokeWidth = 4f
        selectorPaint.color = Color.WHITE
        selectorPaint.setShadowLayer(6f, 0f, 0f, Color.BLACK)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        createBitmap(w, h)
    }

    private fun createBitmap(w: Int, h: Int) {
        if (w == 0 || h == 0) return

        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap!!)

        for (x in 0 until w) {
            val hue = (x / w.toFloat()) * 360f
            for (y in 0 until h) {
                val saturation = 1f - (y / h.toFloat())
                val color = Color.HSVToColor(floatArrayOf(hue, saturation, 1f))
                canvas.drawPoint(x.toFloat(), y.toFloat(), Paint().apply { this.color = color })
            }
        }

        selectorX = (currentHue / 360f) * w
        selectorY = (1f - currentSaturation) * h
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, bitmapPaint)
            // Рисуем селектор
            canvas.drawCircle(selectorX, selectorY, 12f, selectorPaint)
            // Рисуем внутренний круг
            selectorPaint.style = Paint.Style.FILL
            selectorPaint.color = currentColor
            canvas.drawCircle(selectorX, selectorY, 8f, selectorPaint)
            selectorPaint.style = Paint.Style.STROKE
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                selectorX = event.x.coerceIn(0f, width.toFloat())
                selectorY = event.y.coerceIn(0f, height.toFloat())

                val hue = (selectorX / width) * 360f
                val saturation = 1f - (selectorY / height)

                currentColor = Color.HSVToColor(floatArrayOf(hue, saturation, 1f))
                onColorSelectedListener?.invoke(currentColor)
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setOnColorSelectedListener(listener: (Int) -> Unit) {
        this.onColorSelectedListener = listener
    }

    fun setColor(color: Int) {
        currentColor = color
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        currentHue = hsv[0]
        currentSaturation = hsv[1]
        selectorX = (currentHue / 360f) * width
        selectorY = (1f - currentSaturation) * height
        invalidate()
    }

    private var currentHue = 0f
    private var currentSaturation = 1f
}