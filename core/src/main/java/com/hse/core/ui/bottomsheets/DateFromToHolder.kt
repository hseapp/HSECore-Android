/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.ui.bottomsheets

import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.hse.core.R
import com.hse.core.common.onClick
import com.hse.core.common.string
import com.whiteelephant.monthpicker.MonthPickerDialog
import java.text.SimpleDateFormat
import java.util.*

class DateFromToHolder(parent: ViewGroup) : BaseBottomSheetHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.bottom_sheet_item_date_from_to,
        parent,
        false
    ), BottomSheetHolders.TYPE_DATE_FROM_TO
) {
    val dateFromTitle = itemView.findViewById<TextView>(R.id.date_from_title)
    val dateToTitle = itemView.findViewById<TextView>(R.id.date_to_title)
    val dateFromText = itemView.findViewById<TextView>(R.id.date_from_text_view)
    val dateToText = itemView.findViewById<TextView>(R.id.date_to_text_view)
    var listener: ((Item.DateFromTo?, Int, Date?, Date?) -> Unit)? = null
    var item: Item.DateFromTo? = null
    private var dateFrom: Date? = null
    private var dateTo: Date? = null
    private val format = SimpleDateFormat("LLLL yyyy")

    fun setDateFrom(date: Date?) {
        dateFrom = date
        if (date == null) {
            dateFromText.text = string(R.string.from_current_time)
            return
        }
        try {
            dateFromText.text = format.format(date)
        } catch (ignored: Exception) {
        }
    }

    fun setDateTo(date: Date?) {
        val currentTime = Calendar.getInstance()
        if (date == null || with(Calendar.getInstance().apply { time = date }) {
                get(Calendar.MONTH) == currentTime.get(Calendar.MONTH) && get(Calendar.YEAR) == currentTime.get(
                    Calendar.YEAR
                )
            }) {
            dateTo = null
            dateToText.text = string(R.string.to_current_time)
            return
        }
        dateTo = date
        try {
            dateToText.text = format.format(date)
        } catch (ignored: Exception) {
        }
    }

    private fun View.pickDate(date: Date?, listener: (Date?) -> Unit) {
        fun pick() {
            val calendar = Calendar.getInstance().apply { time = date ?: Date() }
            MonthPickerDialog.Builder(
                itemView.context,
                MonthPickerDialog.OnDateSetListener { selectedMonth, selectedYear ->
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.MONTH, selectedMonth)
                        set(Calendar.YEAR, selectedYear)
                    }
                    listener(cal.time)
                    checkDates()
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)
            )
                .setMinYear(Calendar.getInstance().get(Calendar.YEAR) - 5)
                .setMaxYear(Calendar.getInstance().get(Calendar.YEAR) + 5)
                .build()
                .show()
        }
        if (date == null) {
            pick()
            return
        }

        AlertDialog.Builder(itemView.context)
            .setItems(
                arrayOf(
                    string(R.string.select_new_date),
                    string(R.string.drop_date)
                )
            ) { _, which ->
                when (which) {
                    0 -> pick()
                    1 -> {
                        listener(null)
                        checkDates()
                    }
                }
            }
            .show()
    }

    init {
        itemView.findViewById<View>(R.id.date_from).onClick {
            it?.pickDate(dateFrom) {
                setDateFrom(it)
            }
        }
        itemView.findViewById<View>(R.id.date_to).onClick {
            it?.pickDate(dateTo) {
                setDateTo(it)
            }
        }

        setDateFrom(dateFrom)
        setDateTo(dateTo)
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