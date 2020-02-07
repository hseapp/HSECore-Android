package com.hse.core.ui.bottomsheets

import com.hse.core.ui.bottomsheets.BottomSheetHolders.TYPE_DATE_FROM_TO
import com.hse.core.ui.bottomsheets.BottomSheetHolders.TYPE_HORIZONTAL_CHIPS
import com.hse.core.ui.bottomsheets.BottomSheetHolders.TYPE_RANGE_PICKER
import com.hse.core.ui.bottomsheets.BottomSheetHolders.TYPE_SIMPLE_CHECKBOX
import com.hse.core.ui.bottomsheets.BottomSheetHolders.TYPE_SIMPLE_ITEM
import com.hse.core.ui.bottomsheets.BottomSheetHolders.TYPE_SIMPLE_TITLED_ITEM
import java.util.*

object BottomSheetHolders {
    const val TYPE_SIMPLE_TITLED_ITEM = 1
    const val TYPE_HORIZONTAL_CHIPS = 2
    const val TYPE_RANGE_PICKER = 3
    const val TYPE_DATE_FROM_TO = 4
    const val TYPE_SIMPLE_CHECKBOX = 5
    const val TYPE_SIMPLE_ITEM = 6
}

sealed class Item(val type: Int, var tag: String? = null) {
    data class SimpleTitledItem(
        val title: String,
        var text: String,
        val onClick: (SimpleTitledItem, Int) -> Unit
    ) : Item(TYPE_SIMPLE_TITLED_ITEM)

    data class HorizontalChips(
        val title: String
    ) : Item(TYPE_HORIZONTAL_CHIPS) {
        val adapter = HorizontalChipAdapter()

        fun <T> setItems(list: List<T>?, predicate: (T) -> HorizontalChipItem) {
            adapter.clear()
            list?.forEach { adapter.items.add(predicate(it)) }
            adapter.notifyDataSetChanged()
        }

        fun addItem(item: HorizontalChipItem) {
            adapter.items.add(item)
            adapter.notifyDataSetChanged()
        }

        fun remove(item: HorizontalChipItem) {
            val position = adapter.items.indexOf(item)
            adapter.items.remove(item)
            adapter.notifyItemRemoved(position)
        }
    }

    data class RangePicker(
        val title: String,
        val min: Int,
        val max: Int,
        val currentMin: Int,
        val currentMax: Int,
        val rangeText: String,
        val listener: (RangePicker, Int, Int, Int) -> Unit
    ) : Item(TYPE_RANGE_PICKER)

    data class DateFromTo(
        val titleFrom: String,
        val titleTo: String,
        val currentTimeFrom: Date?,
        val currentTimeTo: Date?,
        val listener: (DateFromTo?, Int, Date?, Date?) -> Unit
    ) : Item(TYPE_DATE_FROM_TO)

    data class SimpleCheckbox(
        val text: String,
        var selected: Boolean,
        val listener: (SimpleCheckbox, Int, Boolean) -> Unit
    ) : Item(TYPE_SIMPLE_CHECKBOX)

    data class SimpleItem(
        val text: String?
    ) : Item(TYPE_SIMPLE_ITEM)
}
