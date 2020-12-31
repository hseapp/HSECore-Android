/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.ui.bottomsheets

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.hse.core.R

class HeaderHolder(parent: ViewGroup) : BaseBottomSheetHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.bottom_sheet_item_header,
        parent,
        false
    ), BottomSheetHolders.TYPE_HEADER
) {
    val title = itemView as TextView
}