package com.bountyapp.yourrtodo.dialogs

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bountyapp.yourrtodo.R
import com.bountyapp.yourrtodo.views.BrightnessSliderView
import com.bountyapp.yourrtodo.views.ColorPaletteView

class ColorPickerDialogFragment : DialogFragment() {

    private lateinit var colorPalette: ColorPaletteView
    private lateinit var brightnessSlider: BrightnessSliderView
    private lateinit var colorPreview: View
    private lateinit var etHex: EditText
    private lateinit var tvRgb: TextView
    private lateinit var btnCancel: Button
    private lateinit var btnSelect: Button
    private lateinit var seekSaturation: SeekBar

    private var currentColor: Int = Color.RED
    private var listener: ((String) -> Unit)? = null
    private var isUpdating = false // для предотвращения цикличности

    companion object {
        private const val ARG_COLOR = "arg_color"

        fun newInstance(initialColor: String, listener: (String) -> Unit): ColorPickerDialogFragment {
            val fragment = ColorPickerDialogFragment()
            fragment.listener = listener
            val args = Bundle()
            args.putString(ARG_COLOR, initialColor)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_color_picker_advanced, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        colorPalette = view.findViewById(R.id.color_palette)
        brightnessSlider = view.findViewById(R.id.brightness_slider)
        colorPreview = view.findViewById(R.id.color_preview)
        etHex = view.findViewById(R.id.et_hex)
        tvRgb = view.findViewById(R.id.tv_rgb)
        btnCancel = view.findViewById(R.id.btn_cancel)
        btnSelect = view.findViewById(R.id.btn_select)
        seekSaturation = view.findViewById(R.id.seek_saturation)

        val initialColorStr = arguments?.getString(ARG_COLOR) ?: "#FFC107"
        currentColor = Color.parseColor(initialColorStr)

        // Установить начальный цвет в компоненты
        colorPalette.setColor(currentColor)
        brightnessSlider.baseColor = currentColor
        brightnessSlider.setBrightness(getBrightness(currentColor))
        updatePreview(currentColor)

        // Слушатели
        colorPalette.setOnColorSelectedListener { color ->
            currentColor = adjustBrightness(color, brightnessSlider.brightness)
            brightnessSlider.baseColor = currentColor
            updatePreview(currentColor)
        }

        brightnessSlider.setOnBrightnessChangedListener { brightness ->
            val baseHueSat = getHueAndSaturation(currentColor)
            currentColor = Color.HSVToColor(floatArrayOf(baseHueSat.first, baseHueSat.second, brightness))
            colorPalette.setColor(currentColor) // обновить палитру (она может показывать текущий оттенок)
            updatePreview(currentColor)
        }

        // Обработка HEX ввода
        etHex.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                val hex = s.toString().trim()
                if (hex.length == 7 && hex[0] == '#') {
                    try {
                        val color = Color.parseColor(hex)
                        currentColor = color
                        colorPalette.setColor(color)
                        brightnessSlider.baseColor = color
                        brightnessSlider.setBrightness(getBrightness(color))
                        updatePreview(color)
                    } catch (e: Exception) { /* ignore */ }
                }
            }
        })

        // Кнопки
        btnCancel.setOnClickListener { dismiss() }
        btnSelect.setOnClickListener {
            val hex = String.format("#%06X", 0xFFFFFF and currentColor)
            listener?.invoke(hex)
            dismiss()
        }
    }

    private fun updatePreview(color: Int) {
        isUpdating = true
        colorPreview.setBackgroundColor(color)
        etHex.setText(String.format("#%06X", 0xFFFFFF and color))
        tvRgb.text = "RGB(${Color.red(color)}, ${Color.green(color)}, ${Color.blue(color)})"
        isUpdating = false
    }

    private fun getBrightness(color: Int): Float {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        return hsv[2]
    }

    private fun adjustBrightness(color: Int, brightness: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[2] = brightness
        return Color.HSVToColor(hsv)
    }

    private fun getHueAndSaturation(color: Int): Pair<Float, Float> {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        return Pair(hsv[0], hsv[1])
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setTitle("Выберите цвет")
        return dialog
    }
}