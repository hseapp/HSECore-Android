package com.hse.core.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.hse.core.R
import com.hse.core.common.*
import com.hse.core.utils.Fonts

class EmptyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val image = ImageView(context).apply {
        setGone()
    }
    private val title = TextView(context).apply {
        setTextColor(color(R.color.textPrimary))
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        typeface = Fonts.futuraMedium
        gravity = Gravity.CENTER
        setGone()
    }
    private val subtitle = TextView(context).apply {
        setTextColor(color(R.color.textSecondary))
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        gravity = Gravity.CENTER
        setLineSpacing(1f, 1.1f)
        setGone()
    }
    private val button = HseButton(context).apply {
        setType(HseButton.Type.TRANSPARENT_WITH_BORDER)
        setGone()
    }


    init {
        orientation = VERTICAL
        setPadding(dip(36f), dip(24f), dip(36f), dip(24f))
        gravity = Gravity.CENTER
        addView(image, dip(120f), dip(120f))
        (image.layoutParams as LayoutParams).bottomMargin = dip(24f)
        addView(title)
        (title.layoutParams as LayoutParams).bottomMargin = dip(8f)
        addView(subtitle)
        (subtitle.layoutParams as LayoutParams).bottomMargin = dip(16f)
        addView(button, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    fun setImage(@DrawableRes res: Int) {
        image.setImageResource(res)
        image.setVisible()
    }

    fun setTitle(string: String) {
        title.text = string
        title.setVisible()
    }

    fun setSubtitle(string: String) {
        subtitle.text = string
        subtitle.setVisible()
    }

    fun setButton(text: String, action: (() -> Unit)?) {
        button.text = text
        button.onClick { action?.invoke() }
        button.setVisible()
    }
}