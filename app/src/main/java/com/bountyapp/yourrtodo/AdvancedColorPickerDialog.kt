package com.bountyapp.yourrtodo

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.bountyapp.yourrtodo.model.ColorOption
import com.bountyapp.yourrtodo.views.BrightnessSliderView
import com.bountyapp.yourrtodo.views.ColorPaletteView

class AdvancedColorPickerDialog(
    private val onColorSelected: (ColorOption) -> Unit
) : DialogFragment() {

    private var currentColor = Color.RED

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_color_picker_advanced, null)

        setupViews(view)

        builder.setView(view)
        // НЕ ДОБАВЛЯЕМ setPositiveButton и setNegativeButton здесь!

        return builder.create()
    }

    private fun setupViews(view: View) {
        val colorPalette = view.findViewById<ColorPaletteView>(R.id.color_palette)
        val brightnessSlider = view.findViewById<BrightnessSliderView>(R.id.brightness_slider)
        val colorPreview = view.findViewById<View>(R.id.color_preview)
        val etHex = view.findViewById<EditText>(R.id.et_hex)
        val tvRgb = view.findViewById<TextView>(R.id.tv_rgb)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)
        val btnSelect = view.findViewById<Button>(R.id.btn_select)

        // Обработка выбора цвета из палитры
        colorPalette.setOnColorSelectedListener { color ->
            currentColor = color
            brightnessSlider.baseColor = color
            updatePreview(color, brightnessSlider.brightness, colorPreview, etHex, tvRgb)
        }

        // Обработка изменения яркости
        brightnessSlider.setOnBrightnessChangedListener { brightness ->
            val hsv = FloatArray(3)
            Color.colorToHSV(colorPalette.currentColor, hsv)
            hsv[2] = brightness
            currentColor = Color.HSVToColor(hsv)
            updatePreview(currentColor, brightness, colorPreview, etHex, tvRgb)
        }

        // Обработка ручного ввода HEX
        etHex.setOnEditorActionListener { _, _, _ ->
            try {
                val hex = etHex.text.toString()
                if (hex.startsWith("#") && hex.length == 7) {
                    currentColor = Color.parseColor(hex)

                    val hsv = FloatArray(3)
                    Color.colorToHSV(currentColor, hsv)

                    colorPalette.setColor(currentColor)
                    brightnessSlider.setBrightness(hsv[2])
                    brightnessSlider.baseColor = Color.HSVToColor(floatArrayOf(hsv[0], hsv[1], 1f))

                    updatePreview(currentColor, hsv[2], colorPreview, etHex, tvRgb)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Неверный формат HEX", Toast.LENGTH_SHORT).show()
            }
            true
        }

        // Кнопка отмены - ИСПОЛЬЗУЕМ ТОЛЬКО ЭТИ
        btnCancel.setOnClickListener {
            dialog?.dismiss()
        }

        // Кнопка выбора - ИСПОЛЬЗУЕМ ТОЛЬКО ЭТИ
        btnSelect.setOnClickListener {
            val hexColor = String.format("#%06X", 0xFFFFFF and currentColor)
            val colorOption = ColorOption(hexColor, "Кастомный", true)
            onColorSelected(colorOption)
            dialog?.dismiss()
        }

        // Устанавливаем начальный цвет
        colorPalette.setColor(Color.RED)
        brightnessSlider.baseColor = Color.RED
        updatePreview(Color.RED, 1f, colorPreview, etHex, tvRgb)
    }

    private fun updatePreview(
        color: Int,
        brightness: Float,
        colorPreview: View,
        etHex: EditText,
        tvRgb: TextView
    ) {
        colorPreview.setBackgroundColor(color)

        val hexColor = String.format("#%06X", 0xFFFFFF and color)
        etHex.setText(hexColor)

        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        tvRgb.text = "RGB($red, $green, $blue)"
    }
}