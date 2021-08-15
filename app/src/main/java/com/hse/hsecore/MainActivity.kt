/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.hsecore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hse.core.common.dip
import com.hse.core.ui.bottomsheets.BottomSheetAdapter
import com.hse.core.ui.widgets.BottomSheet
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_settings.view.*
import kotlin.contracts.ExperimentalContracts

class MainActivity : AppCompatActivity() {

    @ExperimentalContracts
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_footer)

//        val titles = arrayListOf("Title 1", "Title 2", "Title 3")
//        spinner?.adapter = ArrayAdapter(this, R.layout.spinner_item, titles)
    }

    fun showBottomSheet(v: View) {
        object : BottomSheet(this) {

            private lateinit var recyclerView: RecyclerView

            init {
                peekHeight = dip(600f)
            }

            override fun getView(): View {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.dialog_settings, null, false)
                recyclerView = view.settingsList
                recyclerView.layoutManager = LinearLayoutManager(context)
                val adapter = BottomSheetAdapter()

                recyclerView.adapter = adapter
                return view
            }

            override fun getBottomView() = null
        }.show()
    }
}
