/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.hsecore

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.dbrain.flow.Flow
import com.hse.core.ui.widgets.BottomSheet

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun showBottomSheet(v: View) {
        object : BottomSheet(this) {
            override fun getView(): View {
                return Button(this@MainActivity)
            }

            override fun getBottomView() = null
        }.show()
    }
}
