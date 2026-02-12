package com.bountyapp.yourrtodo.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class BrightnessSliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var selectorY = 0f

    var baseColor: Int = Color.RED
        set(value) {
            field = value
            updateGradient()
            invalidate()
        }

    var brightness: Float = 1f
        private set

    private var onBrightnessChangedListener: ((Float) -> Unit)? = null

    init {
        selectorPaint.style = Paint.Style.FILL
        selectorPaint.color = Color.WHITE
        selectorPaint.setShadowLayer(8f, 0f, 0f, Color.BLACK)

        selectorY = height.toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        selectorY = height * (1f - brightness)
        updateGradient()
    }

    private fun updateGradient() {
        val hsv = FloatArray(3)
        Color.colorToHSV(baseColor, hsv)

        val colors = intArrayOf(
            Color.HSVToColor(floatArrayOf(hsv[0], hsv[1], 0f)),
            Color.HSVToColor(floatArrayOf(hsv[0], hsv[1], 0.5f)),
            Color.HSVToColor(floatArrayOf(hsv[0], hsv[1], 1f))
        )

        val positions = floatArrayOf(0f, 0.5f, 1f)

        // ИСПРАВЛЕНО: правильные параметры конструктора
        val shader = LinearGradient(
            0f, height.toFloat(),  // x0, y0 - начало градиента (низ)
            0f, 0f,                // x1, y1 - конец градиента (верх)
            colors,               // массив цветов
            positions,           // массив позиций
            Shader.TileMode.CLAMP // режим заливки
        )

        gradientPaint.shader = shader
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Рисуем градиент
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), gradientPaint)

        // Рисуем селектор (круглый ползунок)
        canvas.drawCircle(width / 2f, selectorY, width / 3f, selectorPaint)

        // Рисуем обводку селектора
        selectorPaint.style = Paint.Style.STROKE
        selectorPaint.strokeWidth = 2f
        selectorPaint.color = Color.BLACK
        canvas.drawCircle(width / 2f, selectorY, width / 3f, selectorPaint)

        // Возвращаем стиль обратно на FILL
        selectorPaint.style = Paint.Style.FILL
        selectorPaint.color = Color.WHITE
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                selectorY = event.y.coerceIn(0f, height.toFloat())
                brightness = 1f - (selectorY / height)
                onBrightnessChangedListener?.invoke(brightness)
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setOnBrightnessChangedListener(listener: (Float) -> Unit) {
        this.onBrightnessChangedListener = listener
    }

    fun setBrightness(value: Float) {
        brightness = value.coerceIn(0f, 1f)
        selectorY = height * (1f - brightness)
        invalidate()
    }
}