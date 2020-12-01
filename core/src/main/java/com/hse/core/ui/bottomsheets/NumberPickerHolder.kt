package com.hse.core.ui.bottomsheets

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.hse.core.R
import it.sephiroth.android.library.numberpicker.NumberPicker
import kotlinx.android.synthetic.main.bottom_sheet_item_number_picker.view.*

class NumberPickerHolder(parent: ViewGroup) : BaseBottomSheetHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.bottom_sheet_item_switch,
        parent,
        false
    ), BottomSheetHolders.TYPE_NUMBER_PICKER
) {
    val linearLayout = itemView as LinearLayout
    val prefixTv: TextView = itemView.prefixText
    val suffixTv: TextView = itemView.suffixText
    val numberPicker: NumberPicker = itemView.numberPicker
}