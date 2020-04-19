/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.ui.bottomsheets

import android.text.Editable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.hse.core.R
import com.hse.core.ui.widgets.BorderedEditText
import com.hse.core.utils.DateInputMaskTextWatcher
import java.text.SimpleDateFormat
import java.util.*

class DateFromToHolder(parent: ViewGroup) : BaseBottomSheetHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.bottom_sheet_item_date_from_to,
        parent,
        false
    ), BottomSheetHolders.TYPE_DATE_FROM_TO
) {
    val dateFromText = itemView.findViewById<BorderedEditText>(R.id.date_from)
    val dateToText = itemView.findViewById<BorderedEditText>(R.id.date_to)
    var listener: ((Item.DateFromTo?, Int, Date?, Date?) -> Unit)? = null
    var item: Item.DateFromTo? = null
    private var dateFrom: Date? = null
    private var dateTo: Date? = null
    private val format = SimpleDateFormat("dd.MM.yyyy")

    fun setDateFrom(date: Date?) {
        if (date == null) dateFromText.setText(" ")
        try {
            dateFromText.setText(format.format(date))
        } catch (e: Exception) {
        }
    }

    fun setDateTo(date: Date?) {
        if (date == null) dateToText.setText(" ")
        try {
            dateToText.setText(format.format(date))
        } catch (e: Exception) {
        }
    }

    init {
        DateInputMaskTextWatcher(dateFromText)
        DateInputMaskTextWatcher(dateToText)
        dateFromText.addTextChangedListener {
            dateFrom = formatDate(it)
            checkDates()
        }
        dateToText.addTextChangedListener {
            dateTo = formatDate(it)
            checkDates()
        }
    }

    private fun formatDate(text: Editable?) = try {
        format.parse(text.toString())
    } catch (ignored: Exception) {
        null
    }

    private fun checkDates() {
        listener?.invoke(item, layoutPosition, dateFrom, dateTo)
    }
}