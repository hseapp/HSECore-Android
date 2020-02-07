package com.hse.core.ui.widgets

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hse.core.R
import com.hse.core.common.dip

abstract class BottomSheet(val context: Context) {

    abstract fun getView(): View
    abstract fun getBottomView(): View?

    open fun onDismiss() {}

    protected var dialog: BottomSheetDialog? = null
        private set

    protected var defaultState = -1
    protected var peekHeight = dip(600f)
    protected var isHidable = true
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
        dialog = BottomSheetDialog(context, R.style.BottomSheet)
        dialog?.setContentView(dialogView)
        dialog?.setOnDismissListener { onDismiss() }
        dialog?.setOnCancelListener { onDismiss() }

        val containerLayout =
            dialog?.findViewById<FrameLayout>(com.google.android.material.R.id.container)

        dialog?.setOnShowListener {
            if (it is BottomSheetDialog) {
                BottomSheetBehavior.from(it.findViewById<ViewGroup>(com.google.android.material.R.id.design_bottom_sheet))
                    .apply {
                        peekHeight = this@BottomSheet.peekHeight
                        isHideable = this@BottomSheet.isHidable
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
}