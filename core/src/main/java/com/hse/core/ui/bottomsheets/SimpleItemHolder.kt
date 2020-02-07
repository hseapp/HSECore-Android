package com.hse.core.ui.bottomsheets

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.hse.core.R

class SimpleItemHolder(parent: ViewGroup) : BaseBottomSheetHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.bottom_sheet_item_simple,
        parent,
        false
    ), BottomSheetHolders.TYPE_SIMPLE_ITEM
) {
    val text = itemView.findViewById<TextView>(R.id.text)
}