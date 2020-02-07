package com.hse.core.ui.bottomsheets

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.hse.core.R

class SimpleCheckboxHolder(parent: ViewGroup) : BaseBottomSheetHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.bottom_sheet_item_checkbox,
        parent,
        false
    ), BottomSheetHolders.TYPE_SIMPLE_CHECKBOX
) {
    val text = itemView.findViewById<TextView>(R.id.text)
    val checkbox = itemView.findViewById<ImageView>(R.id.checkbox)
}