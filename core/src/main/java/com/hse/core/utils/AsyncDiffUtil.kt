/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.utils

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import java.util.*

class AsyncDiffUtil<T>(
    private val itemCallback: DiffUtil.ItemCallback<T>,
    private val adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val listUpdateCallback = SimpleUpdateCallback(adapter)
    private var list: List<T>? = null
    private var readOnlyList: List<T> = emptyList()
    private var expectedListSize = 0
    private var doOnSubmitted: (() -> Unit)? = null

    fun list() = readOnlyList
    fun expectedListSize() = expectedListSize

    fun submitList(
        list: List<T>?,
        restoreState: Boolean = false,
        onSubmitted: (() -> Unit)? = null
    ) {
        val newList = if (list == null) null else ArrayList(list)
        expectedListSize = newList?.size ?: 0
        doOnSubmitted = onSubmitted
        if (restoreState) {
            this.list = newList
            readOnlyList = Collections.unmodifiableList(newList)
            adapter.notifyDataSetChanged()
            onSubmitted?.invoke()
        } else {
            if (newList == null || newList.isEmpty()) {
                actor.offer(Operation.Clear)
            } else {
                actor.offer(Operation.Update(newList))
            }
        }
    }

    @ExperimentalCoroutinesApi
    @ObsoleteCoroutinesApi
    private val actor = scope.actor<Operation>(Dispatchers.Default, CONFLATED) {
        consumeEach {
            if (!isActive) return@actor
            val oldList = list
            when (it) {
                is Operation.Clear -> {
                    if (oldList != null) clear(oldList.size)
                }
                is Operation.Update -> {
                    withContext(Dispatchers.Default) {
                        if (oldList == null) {
                            insert(it.newList as List<T>)
                        } else if (oldList != it.newList) {
                            val callback =
                                diffUtilCallback(oldList, it.newList as List<T>, itemCallback)
                            val result = DiffUtil.calculateDiff(callback)
                            if (coroutineContext.isActive) latch(it.newList, result)
                        }
                    }
                }
            }
            withContext(Dispatchers.Main) { doOnSubmitted?.invoke() }
        }
    }

    private suspend fun clear(count: Int) {
        withContext(Dispatchers.Main) {
            list = null
            readOnlyList = emptyList()
            listUpdateCallback.onRemoved(0, count)
        }
    }

    private suspend fun insert(newList: List<T>) {
        withContext(Dispatchers.Main) {
            list = newList
            readOnlyList = Collections.unmodifiableList(newList)
            listUpdateCallback.onInserted(0, newList.size)
        }
    }

    private suspend fun latch(newList: List<T>, result: DiffUtil.DiffResult) {
        withContext(Dispatchers.Main) {
            list = newList
            readOnlyList = Collections.unmodifiableList(newList)
            result.dispatchUpdatesTo(listUpdateCallback)
        }
    }

    private fun diffUtilCallback(
        oldList: List<T>,
        newList: List<T>,
        callback: DiffUtil.ItemCallback<T>
    ) =
        object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = oldList[oldItemPosition]
                val newItem = newList[newItemPosition]
                return callback.areItemsTheSame(oldItem, newItem)
            }

            override fun getOldListSize(): Int = oldList.size

            override fun getNewListSize(): Int = newList.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = oldList[oldItemPosition]
                val newItem = newList[newItemPosition]
                return callback.areContentsTheSame(oldItem, newItem)
            }
        }

    internal class SimpleUpdateCallback(private val adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>) :
        ListUpdateCallback {
        override fun onChanged(position: Int, count: Int, payload: Any?) {
            adapter.notifyItemRangeChanged(position, count, payload)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            adapter.notifyItemMoved(fromPosition, toPosition)
        }

        override fun onInserted(position: Int, count: Int) {
            adapter.notifyItemRangeInserted(position, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            adapter.notifyItemRangeRemoved(position, count)
        }
    }

    sealed class Operation {
        object Clear : Operation()
        data class Update(val newList: List<*>) : Operation()
    }
}