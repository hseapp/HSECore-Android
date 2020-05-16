/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.ui.bottomsheets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import com.hse.core.R

class SimpleSwitchHolder(parent: ViewGroup) : BaseBottomSheetHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.bottom_sheet_item_switch,
        parent,
        false
    ), BottomSheetHolders.TYPE_SIMPLE_CHECKBOX
) {
    val switch = itemView as SwitchCompat

}