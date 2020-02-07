package com.hse.core.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hse.core.R
import com.hse.core.utils.AsyncDiffUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


abstract class PaginatedRecyclerAdapter<T>(
    itemCallback: DiffUtil.ItemCallback<T>,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val asyncDiffUtil = AsyncDiffUtil(itemCallback, this, coroutineScope)
    private var isLoading = false
    private var canRestoreState = true
    var recyclerView: RecyclerView? = null

    fun submitList(
        list: List<T>?,
        restoreState: Boolean = false,
        onSubmitted: (() -> Unit)? = null
    ) {
        asyncDiffUtil.submitList(list, restoreState && canRestoreState, onSubmitted)
        if (restoreState) canRestoreState = false
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    fun setIsLoading(isLoading: Boolean) {
        this.isLoading = isLoading
        val itemAnimator = recyclerView?.itemAnimator
        recyclerView?.itemAnimator = null
        if (isLoading) {
            notifyItemInserted(itemCount)
        } else {
            notifyItemRemoved(itemCount)
        }
        recyclerView?.itemAnimator = itemAnimator
    }

    open fun getItemAt(pos: Int): T? {
        val list = asyncDiffUtil.list()
        if (pos < 0 || pos >= list.size) return null
        return list[pos]
    }

    fun isEmpty() = asyncDiffUtil.list().isEmpty()

    override fun getItemCount(): Int {
        return asyncDiffUtil.list().size + if (isLoading) 1 else 0
    }

    fun getRealItemCount(): Int = asyncDiffUtil.expectedListSize()

    override fun getItemViewType(position: Int): Int {
        if (isLoading && position == itemCount - 1) {
            return ITEM_TYPE_FOOTER
        }
        return ITEM_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TYPE_FOOTER -> LoadingFooterHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.list_footer,
                    parent,
                    false
                )
            )
            else -> throw Exception("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    private class LoadingFooterHolder(item: View) : RecyclerView.ViewHolder(item)

    companion object {
        const val ITEM_TYPE_ITEM = 1
        const val ITEM_TYPE_FOOTER = -1000
        const val ITEM_TYPE_EMPTY = -2000
        const val ITEM_TYPE_PLACEHOLDER = 0
    }
}