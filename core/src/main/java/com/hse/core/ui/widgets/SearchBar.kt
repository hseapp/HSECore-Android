/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.ui.widgets

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.StringRes
import com.hse.core.R
import com.hse.core.common.*

open class SearchBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    FrameLayout(context, attrs, defStyleAttr), TextView.OnEditorActionListener, TextWatcher {
    var listener: SearchBarListener? = null
    var paramsButtonVisible = false
        set(value) {
            field = value
            if (value) paramsButton.setVisible()
        }

    private var runnable = Runnable {
        listener?.onSearch(editText.text.toString())
    }

    protected val editText = EditText(context).apply {
        setPadding(paddingLeft, paddingTop, dip(48f), paddingBottom)
        hint = context.getString(R.string.search)
        background = null
        maxLines = 1
        filters = arrayOf<InputFilter>(LengthFilter(200))
        inputType = EditorInfo.TYPE_CLASS_TEXT
        imeOptions = EditorInfo.IME_ACTION_SEARCH or EditorInfo.IME_FLAG_NO_EXTRACT_UI
        addTextChangedListener(this@SearchBar)
        setOnEditorActionListener(this@SearchBar)
    }

    protected val clearButton = ImageButton(context).apply {
        setImageResource(R.drawable.ic_clear_24)
        setBackgroundResource(getSelectableItemBackgroundBorderless())
        onClick {
            editText.text.clear()
            hideKeyboard(true)
            fadeOut(100)
        }
        setGone()
    }

    protected val paramsButton = ImageButton(context).apply {
        setImageResource(R.drawable.ic_parameters_24)
        setBackgroundResource(getSelectableItemBackgroundBorderless())
        onClick {
            listener?.onParamsClicked()
        }
        setGone()
    }

    init {
        addView(editText)
        addView(
            clearButton,
            LayoutParams(
                dip(24f),
                dip(24f),
                Gravity.END or Gravity.CENTER_VERTICAL
            ).apply { marginEnd = dip(16f) }
        )
        addView(
            paramsButton,
            LayoutParams(
                dip(24f),
                dip(24f),
                Gravity.END or Gravity.CENTER_VERTICAL
            ).apply { marginEnd = dip(16f) }
        )
    }

    fun setFakeMode(onClick: OnClickListener) {
        editText.isFocusable = false
        editText.isEnabled = false
        // don't do like that
        addView(View(context).apply {
            setOnClickListener(onClick)
        }, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    fun getText(): CharSequence = editText.text
    fun showKeyboard() = editText.showKeyboard()
    fun setHint(@StringRes res: Int) = editText.setHint(res)

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            listener?.onSearch(v?.text?.toString())
            return true
        }
        return false
    }

    override fun afterTextChanged(s: Editable?) {

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, 300)
        if (s != null && s.isNotEmpty()) {
            clearButton.fadeIn(100)
            if (paramsButtonVisible) paramsButton.fadeOut(100)
        } else {
            clearButton.fadeOut(100)
            if (paramsButtonVisible) paramsButton.fadeIn(100)
        }
    }

    interface SearchBarListener {
        fun onSearch(q: String?)
        fun onParamsClicked()
    }
}