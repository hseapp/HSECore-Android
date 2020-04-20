/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.ui.bottomsheets

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hse.core.R
import com.hse.core.common.color
import com.hse.core.common.dip
import com.hse.core.common.drawable
import com.hse.core.common.onClick

class HorizontalChipsHolder(parent: ViewGroup) : BaseBottomSheetHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.bottom_sheet_item_chip,
        parent,
        false
    ), BottomSheetHolders.TYPE_HORIZONTAL_CHIPS
) {
    val title = itemView.findViewById<TextView>(R.id.title)
    val recycler = itemView.findViewById<RecyclerView>(R.id.recycler_view)

    init {
        recycler.layoutManager =
            LinearLayoutManager(parent.context, LinearLayoutManager.HORIZONTAL, false)
        recycler.addItemDecoration(object : RecyclerView.ItemDecoration() {
            val padding = dip(8f)
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.right = padding
            }
        })
        recycler.isNestedScrollingEnabled = false
    }
}

data class HorizontalChipItem(
    val title: String,
    val drawableLeft: Drawable? = null,
    val drawableRight: Drawable? = null,
    val tintColor: Int? = null,
    var isChecked: Boolean = false,
    val isCheckable: Boolean = true,
    val isSpecial: Boolean = false,
    val onClick: (HorizontalChipItem, Boolean) -> Unit
)

class HorizontalChipAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return Holder(parent)
    }

    val items = ArrayList<HorizontalChipItem>()

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        (holder as Holder).text.apply {
            text = item.title

            val leftDrawable = item.drawableLeft?.mutate()

            when {
                item.isSpecial -> {
                    setTextColor(color(R.color.blue))
                }
                item.isCheckable -> {
                    if (item.isChecked) {
                        setTextColor(0xffffffff.toInt())
                        if (item.tintColor != null) leftDrawable?.setTint(0xffffffff.toInt())
                    } else {
                        setTextColor(color(R.color.textPrimary))
                        if (item.tintColor != null) leftDrawable?.setTint(item.tintColor)
                    }
                }
                else -> {
                    setTextColor(color(R.color.textPrimary))
                }
            }

            setCompoundDrawablesWithIntrinsicBounds(
                leftDrawable,
                null,
                item.drawableRight,
                null
            )

            val background  = when {
                item.isSpecial -> R.drawable.bottom_sheet_chip_special
                item.isChecked -> R.drawable.bottom_sheet_chip_selected
                else -> R.drawable.bottom_sheet_chip_normal
            }
            val backgroundDrawable = drawable(background)?.mutate()
            if (item.isChecked && item.tintColor != null) backgroundDrawable?.setTint(item.tintColor)
            setBackground(backgroundDrawable)
        }
    }

    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    private inner class Holder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.bottom_sheet_item_chip_item,
            parent,
            false
        )
    ) {
        val text = itemView as TextView

        init {
            itemView.onClick {
                val item = items[layoutPosition]
                if (!item.isSpecial) {
                    item.isChecked = !item.isChecked
                    notifyItemChanged(layoutPosition)
                }
                item.onClick(item, item.isChecked)
            }
        }
    }
}