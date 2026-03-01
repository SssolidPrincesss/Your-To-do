package com.bountyapp.yourrtodo.views

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView, который корректно разворачивается внутри ScrollView
 */
class ExpandedRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        // Разрешаем измерение без ограничений по высоте
        val expandSpec = MeasureSpec.makeMeasureSpec(
            MeasureSpec.getSize(heightSpec),
            MeasureSpec.UNSPECIFIED
        )
        super.onMeasure(widthSpec, expandSpec)
    }
}