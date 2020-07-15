/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.ui.widgets

import android.app.Dialog
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hse.core.R
import com.hse.core.common.color
import com.hse.core.common.dip


abstract class BottomSheet(val context: Context) {

    abstract fun getView(): View
    abstract fun getBottomView(): View?

    open fun onDismiss() {}

    protected var dialog: SheetDialog? = null
        private set

    protected var defaultState = -1
    protected var peekHeight = dip(600f)
    protected var bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {

        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_COLLAPSED) dismiss()
        }
    }

    fun show() {
        val bottomView = getBottomView()
        val dialogView = getDecoratedView()

        dialog = SheetDialog(context, R.style.BottomSheet)
        dialog?.setContentView(dialogView)
        dialog?.setOnDismissListener { onDismiss() }
        dialog?.setOnCancelListener { onDismiss() }

        val containerLayout =
            dialog?.findViewById<FrameLayout>(com.google.android.material.R.id.container)

        dialog?.setOnShowListener {
            if (it is BottomSheetDialog) {
                BottomSheetBehavior.from(it.findViewById<ViewGroup>(com.google.android.material.R.id.design_bottom_sheet)!!)
                    .apply {
                        peekHeight = this@BottomSheet.peekHeight
                        isHideable = false //this@BottomSheet.isHidable
                        if (defaultState >= 0) state = defaultState
                        setBottomSheetCallback(bottomSheetCallback)
                    }
            }
        }

        dialog?.show()

        if (bottomView != null) {
            containerLayout?.addView(
                bottomView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM
                )
            )

            bottomView.post {
                dialogView.setPadding(0, 0, 0, bottomView.height)
            }
        }
    }

    fun dismiss() {
        dialog?.dismiss()
    }

    private fun getDecoratedView(): View {
        val layout =
            LayoutInflater.from(context).inflate(R.layout.bottom_sheet, null, false) as ViewGroup
        layout.findViewById<ViewGroup>(R.id.content).addView(getView())
        return layout
    }

    inner class SheetDialog(context: Context, style: Int) : BottomSheetDialog(context, style) {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setWhiteNavigationBar(this)
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        private fun setWhiteNavigationBar(dialog: Dialog) {
            val content = findViewById<View>(android.R.id.content)
            content?.setOnApplyWindowInsetsListener { v, insets ->
                dialog.window?.apply {
                    val point = Point()
                    windowManager.defaultDisplay.getSize(point)
                    val dimDrawable = GradientDrawable()
                    val navigationBarDrawable = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        setColor(color(R.color.windowBackground))
                    }
                    val layers = arrayOf<Drawable>(dimDrawable, navigationBarDrawable)
                    val windowBackground = LayerDrawable(layers).apply {
                        setLayerInsetTop(1, point.y - insets.systemWindowInsetBottom)
                    }
                    setBackgroundDrawable(windowBackground)
                }
                insets
            }


        }
    }
}
