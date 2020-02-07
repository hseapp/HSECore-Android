package com.hse.core.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import com.hse.core.R
import com.hse.core.common.color
import com.hse.core.common.dip
import com.hse.core.utils.Fonts


class HseButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    TextView(context, attrs, defStyleAttr) {

    init {
        typeface = Fonts.futuraMedium
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        setPadding(dip(24f), 0, dip(24f), 0)
        gravity = Gravity.CENTER
        minHeight = dip(36f)
        minimumHeight = dip(36f)

        val a = getContext().obtainStyledAttributes(attrs, R.styleable.HseButton)
        val ordinal = a.getInt(R.styleable.HseButton_type, 0)
        val buttonType = Type.values()[ordinal]
        setType(buttonType)
    }

    fun setType(type: Type) {
        when (type) {
            Type.BLUE -> {
                setBackgroundResource(R.drawable.hse_button_selector_blue)
                setTextColor(color(R.color.buttonNormalText))
            }
            Type.TRANSPARENT -> {
                setBackgroundResource(R.drawable.hse_button_selector_transparent)
                setTextColor(color(R.color.buttonTransparentText))
            }
            Type.TRANSPARENT_WITH_BORDER -> {
                setBackgroundResource(R.drawable.hse_button_selector_transparent_w_border)
                setTextColor(color(R.color.buttonTransparentText))
            }
        }
    }

    enum class Type {
        BLUE, TRANSPARENT, TRANSPARENT_WITH_BORDER
    }

}