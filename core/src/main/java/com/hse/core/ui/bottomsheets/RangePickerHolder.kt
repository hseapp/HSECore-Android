/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.ui.bottomsheets

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.hse.core.R
import com.innovattic.rangeseekbar.RangeSeekBar

class RangePickerHolder(parent: ViewGroup) : BaseBottomSheetHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.bottom_sheet_item_range_picker,
        parent,
        false
    ), BottomSheetHolders.TYPE_RANGE_PICKER
) {
    val title = itemView.findViewById<TextView>(R.id.title)
    val range = itemView.findViewById<TextView>(R.id.range)
    val picker = itemView.findViewById<RangeSeekBar>(R.id.range_picker)
}