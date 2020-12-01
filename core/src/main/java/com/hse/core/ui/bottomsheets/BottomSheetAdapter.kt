/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.ui.bottomsheets

import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import com.hse.core.R
import com.hse.core.common.onClick
import com.hse.core.ui.bottomsheets.BottomSheetHolders.TYPE_DATE_FROM_TO
import com.hse.core.ui.bottomsheets.BottomSheetHolders.TYPE_HORIZONTAL_CHIPS
import com.hse.core.ui.bottomsheets.BottomSheetHolders.TYPE_NUMBER_PICKER
import com.hse.core.ui.bottomsheets.BottomSheetHolders.TYPE_RANGE_PICKER
import com.hse.core.ui.bottomsheets.BottomSheetHolders.TYPE_SIMPLE_CHECKBOX
import com.hse.core.ui.bottomsheets.BottomSheetHolders.TYPE_SIMPLE_ITEM
import com.hse.core.ui.bottomsheets.BottomSheetHolders.TYPE_SIMPLE_SWITCH
import com.hse.core.ui.bottomsheets.BottomSheetHolders.TYPE_SIMPLE_TITLED_ITEM
import com.hse.core.ui.bottomsheets.BottomSheetHolders.TYPE_SLIDER
import com.innovattic.rangeseekbar.RangeSeekBar
import it.sephiroth.android.library.numberpicker.doOnProgressChanged

open class BottomSheetAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val data = ArrayList<Item>()
    private val originalData = ArrayList<Item>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SIMPLE_TITLED_ITEM -> SimpleTitledItemHolder(parent)
            TYPE_HORIZONTAL_CHIPS -> HorizontalChipsHolder(parent)
            TYPE_RANGE_PICKER -> RangePickerHolder(parent)
            TYPE_DATE_FROM_TO -> DateFromToHolder(parent)
            TYPE_SIMPLE_CHECKBOX -> SimpleCheckboxHolder(parent)
            TYPE_SIMPLE_ITEM -> SimpleItemHolder(parent)
            TYPE_SIMPLE_SWITCH -> SimpleSwitchHolder(parent)
            TYPE_SLIDER -> SliderHolder(parent)
            TYPE_NUMBER_PICKER -> NumberPickerHolder(parent)
            else -> throw Exception("No view found")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SimpleTitledItemHolder -> {
                val item = data[position] as? Item.SimpleTitledItem ?: return
                holder.title.text = item.title
                holder.text.text = item.text
                holder.itemView.onClick { item.onClick.invoke(item, position) }
            }
            is HorizontalChipsHolder -> {
                val item = data[position] as? Item.HorizontalChips ?: return
                holder.recycler.adapter = item.adapter
                holder.title.text = item.title
            }
            is RangePickerHolder -> {
                val item = data[position] as? Item.RangePicker ?: return
                holder.title.text = item.title
                holder.picker.apply {
                    max = item.max - item.min
                    setMinThumbValue(item.currentMin - item.min)
                    setMaxThumbValue(item.currentMax - item.min)
                    holder.range.text = String.format(
                        item.rangeText,
                        item.currentMin,
                        item.currentMax
                    )

                    seekBarChangeListener = object : RangeSeekBar.SeekBarChangeListener {
                        override fun onStartedSeeking() {

                        }

                        override fun onStoppedSeeking() {

                        }

                        override fun onValueChanged(minThumbValue: Int, maxThumbValue: Int) {
                            item.listener.invoke(
                                item,
                                position,
                                minThumbValue + item.min,
                                maxThumbValue + item.min
                            )
                            holder.range.text = String.format(
                                item.rangeText,
                                minThumbValue + item.min,
                                maxThumbValue + item.min
                            )
                        }
                    }
                }
            }
            is DateFromToHolder -> {
                val item = data[position] as? Item.DateFromTo ?: return
                holder.dateFromTitle.text = item.titleFrom
                holder.dateToTitle.text = item.titleTo
                holder.listener = item.listener
                holder.setDateFrom(item.currentTimeFrom)
                holder.setDateTo(item.currentTimeTo)
            }
            is SimpleCheckboxHolder -> {
                val item = data[position] as? Item.SimpleCheckbox ?: return
                holder.text.text = item.text
                holder.checkbox.setImageResource(if (item.selected) R.drawable.ic_done_circle_blue_24 else R.drawable.ic_checkbox_unchecked_24)
                holder.itemView.onClick {
                    item.selected = !item.selected
                    item.listener(item, position, item.selected)
                    notifyItemChanged(position)
                }
            }
            is SimpleItemHolder -> {
                val item = data[position] as? Item.SimpleItem ?: return
                holder.text.text = item.text
            }
            is SimpleSwitchHolder -> {
                val item = data[position] as? Item.SimpleSwitch ?: return
                holder.switch.text = item.text
                holder.switch.setOnCheckedChangeListener(null)
                holder.switch.isChecked = item.selected
                holder.switch.setOnCheckedChangeListener { view, isChecked ->
                    item.selected = isChecked
                    item.listener(item, position, isChecked)
                }
            }
            is SliderHolder -> {
                val item = data[position] as? Item.Slider ?: return
                holder.title.text = item.title
                holder.slider.apply {
                    max = item.max
                    progress = item.currentMax
                    holder.range.text = String.format(
                        item.rangeText,
                        item.currentMax
                    )

                    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            if (fromUser) {
                                item.listener.invoke(
                                    item,
                                    position,
                                    progress
                                )
                                holder.range.text = String.format(
                                    item.rangeText,
                                    progress
                                )
                            }
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {

                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {

                        }
                    })
                }
            }
            is NumberPickerHolder -> {
                val item = data[position] as? Item.NumberPickerItem ?: return
                holder.prefixTv.text = item.prefix
                holder.suffixTv.text = item.suffix
                holder.numberPicker.progress = item.initProgress
                holder.numberPicker.maxValue = item.maxValue
                holder.numberPicker.minValue = item.minValue
                holder.numberPicker.stepSize = item.stepSize
                holder.numberPicker.doOnProgressChanged { numberPicker, progress, formUser ->
                    item.listener(progress)
                }
            }
        }
    }


    fun addItem(item: Item) {
        data.add(item)
        originalData.add(item)
    }

    fun filter(s: String?, predicate: (Item) -> Boolean) {
        data.clear()
        if (s.isNullOrEmpty()) {
            data.addAll(originalData)
        } else {
            originalData.forEach {
                if (predicate(it)) data.add(it)
            }
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int) = data[position].type
    override fun getItemCount() = data.size
}