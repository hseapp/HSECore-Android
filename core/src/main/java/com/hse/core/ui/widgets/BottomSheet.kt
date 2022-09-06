/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.ui.widgets

import android.app.Dialog
import android.content.Context
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.core.view.updateLayoutParams
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hse.core.R
import com.hse.core.common.color
import com.hse.core.common.dip
import android.view.Display

import android.view.WindowManager
import com.google.android.material.textview.MaterialTextView


abstract class BottomSheet(val context: Context) {

    abstract fun getView(): View
    abstract fun getBottomView(): View?

    open fun onDismiss() {}

    protected var dialog: SheetDialog? = null
        private set

    protected lateinit var handleLayout: ViewGroup
    protected lateinit var handle: View
    private var actionBar: FrameLayout? = null
    private var actionBarCallback: ActionBarCallback? = null
    private var isActionBarShown = false
    private var positiveText: String? = null
    private var negativeText: String? = null
    private var title: String? = null

    protected var isHidable = true
    protected var isSkipCollapsed = false
    protected var defaultState = BottomSheetBehavior.STATE_EXPANDED
    protected var peekHeight = dip(100f)
    protected var onUpdateLayoutParams: UpdateLayoutParams? = null
    protected var bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {

        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) dismiss()
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
            // dirty hack
            dialogView.post {
                if (it is BottomSheetDialog) {
                    BottomSheetBehavior.from(it.findViewById<ViewGroup>(com.google.android.material.R.id.design_bottom_sheet)!!)
                        .apply {
                            skipCollapsed = isSkipCollapsed
                            peekHeight = this@BottomSheet.peekHeight
                            isHideable = this@BottomSheet.isHidable
                            if (defaultState >= 0) state = defaultState
                            setBottomSheetCallback(bottomSheetCallback)
                        }
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
        // layout.findViewById<FrameLayout>(R.id.content).layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val positive = layout.findViewById<MaterialTextView>(R.id.doneBtn)
        val negative = layout.findViewById<MaterialTextView>(R.id.cancelBtn)

        actionBar = layout.findViewById(R.id.action_bar)

        if (isActionBarShown) actionBar?.visibility = View.VISIBLE

        title?.let {
            setTitle(it)
        }

        negativeText?.let {
            negative?.text = it
        }

        positiveText?.let {
            positive?.text = it
        }

        positive?.setOnClickListener {
            actionBarCallback?.onPositive() ?: dismiss()
        }

        negative?.setOnClickListener {
            actionBarCallback?.onNegative() ?: dismiss()
        }
        //layout.findViewById<FrameLayout>(R.id.content).minimumHeight = height
        handleLayout = layout.findViewById(R.id.header)
        handle = layout.findViewById(R.id.handle)
        layout.findViewById<FrameLayout>(R.id.content).addView(getView())
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

    fun showActionBar(isShown: Boolean, negativeText: String? = null, positiveText: String? = null) {
        isActionBarShown = isShown

        if (isShown) {
            this.positiveText = positiveText
            this.negativeText = negativeText
            if (positiveText != null) actionBar?.findViewById<MaterialTextView>(R.id.doneBtn)?.text = positiveText
            if (negativeText != null) actionBar?.findViewById<MaterialTextView>(R.id.cancelBtn)?.text = negativeText
            actionBar?.visibility = View.VISIBLE
        } else {
            actionBar?.visibility = View.GONE
        }
    }

    fun setTitle(text: String) {
        title = text
        actionBar?.findViewById<MaterialTextView>(R.id.title)?.let {
            it.visibility = View.VISIBLE
            it.text = text
        }
    }

    fun setActionBarCallback(callback: ActionBarCallback?) {
        actionBarCallback = callback
    }

    interface ActionBarCallback {
        fun onPositive()
        fun onNegative()
    }

    fun interface UpdateLayoutParams {
        fun onUpdate(params: ViewGroup.LayoutParams)
    }
}
