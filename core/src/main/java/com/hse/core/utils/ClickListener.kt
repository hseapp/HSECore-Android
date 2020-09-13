/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.utils

import android.view.View

class ClickListener(val onClick: (View?) -> Unit) : View.OnClickListener {

    override fun onClick(v: View?) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > THRESHOLD) {
            lastClickTime = currentTime
            onClick.invoke(v)
        }
    }

    companion object {
        private var lastClickTime = 0L
        private const val THRESHOLD = 200
    }
}