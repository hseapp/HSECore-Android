/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.ui.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hse.core.adapters.PaginatedRecyclerAdapter
import com.hse.core.datasource.PaginatedDataSource
import java.util.concurrent.atomic.AtomicBoolean

class PaginatedRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    RecyclerView(context, attrs, defStyleAttr) {

    private val lock = AtomicBoolean(false)

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            if (dataSource == null) return
            val manager = (layoutManager as? LinearLayoutManager) ?: return
            val adapter = getPaginatedAdapter() ?: return
            val totalItemsCount = adapter.itemCount
            val lastVisibleItemPosition = manager.findLastVisibleItemPosition()
            if (totalItemsCount - lastVisibleItemPosition > paginationThreshold || totalItemsCount == 0 || lock.get()) return
            lock()
            dataSource?.loadNext()
        }
    }
    private var dataSource: PaginatedDataSource<*, *>? = null
    private var paginationThreshold = 5
    private val unlocker = Runnable { lock.set(false) }

    private fun lock() {
        removeCallbacks(unlocker)
        lock.set(true)
        postDelayed(unlocker, 500)
    }

    fun init(
        adapter: PaginatedRecyclerAdapter<*>?,
        dataSource: PaginatedDataSource<*, *>?,
        layoutManager: LinearLayoutManager,
        paginationThreshold: Int = 5
    ) {
        this.paginationThreshold = paginationThreshold
        this.dataSource = dataSource
        setAdapter(adapter)
        setLayoutManager(layoutManager)
        addOnScrollListener(scrollListener)
    }

    private fun getPaginatedAdapter() = adapter as? PaginatedRecyclerAdapter<*>?
}