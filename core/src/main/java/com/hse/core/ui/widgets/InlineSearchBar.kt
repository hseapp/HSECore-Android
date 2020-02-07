package com.hse.core.ui.widgets

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.TextView
import com.hse.core.R
import com.hse.core.common.color
import com.hse.core.common.dip
import com.hse.core.common.onClick


class InlineSearchBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    SearchBar(context, attrs, defStyleAttr), TextView.OnEditorActionListener, TextWatcher {

    protected val searchIcon = ImageView(context).apply {
        setImageResource(R.drawable.ic_search_gray_action)
        setColorFilter(color(R.color.searchBarInlineIcon))
    }

    init {
        (editText.layoutParams as MarginLayoutParams).apply {
            topMargin = dip(8f)
            bottomMargin = dip(8f)
            leftMargin = dip(16f)
            rightMargin = dip(16f)
            height = dip(36f)
        }
        editText.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setPadding(dip(36f), 0, dip(48f), 0)
            setBackgroundResource(R.drawable.search_bar_inline_background)
        }

        (clearButton.layoutParams as MarginLayoutParams).apply {
            rightMargin = dip(24f)
        }
        (paramsButton.layoutParams as MarginLayoutParams).apply {
            rightMargin = dip(24f)
        }

        clearButton.setColorFilter(color(R.color.searchBarInlineIcon))
        paramsButton.setColorFilter(color(R.color.searchBarInlineIcon))

        addView(
            searchIcon,
            LayoutParams(
                dip(24f),
                dip(24f),
                Gravity.START or Gravity.CENTER_VERTICAL
            ).apply { marginStart = dip(24f) }
        )
    }

    fun setLeftIcon(res: Int, click: () -> Unit) {
        searchIcon.setImageResource(res)
        searchIcon.setColorFilter(color(R.color.searchBarInlineIcon))
        searchIcon.onClick { click() }
    }

}


