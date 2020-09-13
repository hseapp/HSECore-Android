/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.ui.bottomsheets

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import com.hse.core.R
import com.innovattic.rangeseekbar.RangeSeekBar

class SliderHolder(parent: ViewGroup) : BaseBottomSheetHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.bottom_sheet_item_slider,
        parent,
        false
    ), BottomSheetHolders.TYPE_SLIDER
) {
    val title = itemView.findViewById<TextView>(R.id.title)
    val range = itemView.findViewById<TextView>(R.id.range)
    val slider = itemView.findViewById<SeekBar>(R.id.slider)
}