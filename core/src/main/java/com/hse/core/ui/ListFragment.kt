/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.hse.core.R
import com.hse.core.adapters.PaginatedRecyclerAdapter
import com.hse.core.common.*
import com.hse.core.enums.LoadingState
import com.hse.core.ui.widgets.EmptyView
import com.hse.core.ui.widgets.PaginatedRecyclerView
import com.hse.core.utils.KeyboardEvent
import com.hse.core.viewmodels.PaginatedViewModel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import java.util.*
import kotlin.collections.ArrayList

abstract class ListFragment<E, T : PaginatedViewModel<E>> : BaseFragment<T>() {
    var recyclerView: PaginatedRecyclerView? = null
    var swipeRefresh: SwipeRefreshLayout? = null
    var progressBar: ProgressBar? = null

    private val overlayViews = Collections.synchronizedList(ArrayList<View>())
    private var currentState = LoadingState.IDLE
    private var keyboardEventsChannel: ReceiveChannel<KeyboardEvent>? = null

    var adapter: PaginatedRecyclerAdapter<E>? = null

    abstract fun provideAdapter(): PaginatedRecyclerAdapter<E>?

    open fun getLayoutManager(): RecyclerView.LayoutManager = LinearLayoutManager(context)

    open fun getPaginationThreshold() = 5

    open fun getLayout() = R.layout.list_fragment

    open fun getOverlayViewsMargin() = 0

    open fun onDataReceived(list: List<E>) {}

    private val hideKeyboardScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState != RecyclerView.SCROLL_STATE_IDLE) recyclerView.hideKeyboard(true)
        }
    }

    open fun getErrorView(t: Throwable?): View {
        return EmptyView(requireContext()).apply {
            setImage(R.drawable.nointernetru)
            setTitle(string(R.string.error_occurred))
            setSubtitle(string(R.string.error_occurred_description))
            setButton(string(R.string.retry)) { reload() }
        }
    }

    open fun getEmptyView(): View {
        return EmptyView(requireContext()).apply {
            setImage(R.drawable.nodata)
            setTitle(string(R.string.empty_list))
            setSubtitle(string(R.string.empty_list_description))
        }
    }

    override fun onFragmentStackSelected(): Boolean {
        if (adapter == null || adapter?.isEmpty() == true) return true
        val manager = recyclerView?.layoutManager
        if (manager is LinearLayoutManager) {
            if (manager.findFirstVisibleItemPosition() == 0) return true
            recyclerView?.scrollToPosition(0)
            appBarLayout?.translationZ = 0f
            return false
        }
        return true
    }

    protected fun reload() {
        viewModel.getDataSource()?.reset(false)
    }

    private var lastKeyboardState = KeyboardEvent.HIDDEN
    override fun onAttach(context: Context) {
        super.onAttach(context)
        keyboardEventsChannel = activity()?.keyboardInterceptor?.subscribe()
        lifecycleScope.launchWhenCreated {
            keyboardEventsChannel?.consumeEach {
                if (lastKeyboardState != it) {
                    when (it) {
                        KeyboardEvent.SHOWN -> {
                            overlayViews.forEach {
                                it.alpha = 0f
                                it.fadeIn(delay = 100)
                            }
                        }
                        KeyboardEvent.HIDDEN -> {
                            overlayViews.forEach {
                                it.alpha = 0f
                                it.fadeIn(delay = 100)
                            }
                        }
                    }
                    lastKeyboardState = it
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        keyboardEventsChannel?.cancel()
    }

    override fun provideView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(getLayout(), null, false)
        if (mainLayout == null) mainLayout = view.findViewById(R.id.main_layout)
        if (appBarLayout == null) appBarLayout = view.findViewById(R.id.appbar)
        if (toolbar == null) toolbar = view.findViewById(R.id.toolbar)
        if (recyclerView == null) recyclerView = view.findViewById(R.id.recycler_view)
        if (swipeRefresh == null) swipeRefresh = view.findViewById(R.id.swipe_refresh_layout)
        if (progressBar == null) progressBar = view.findViewById(R.id.progress_bar)

        createView(view, inflater, container, savedInstanceState)

        swipeRefresh?.setProgressViewOffset(true, -dip(16f), dip(16f))

        adapter = provideAdapter()
        val layoutManager = getLayoutManager()
        if (layoutManager is LinearLayoutManager) {
            recyclerView?.init(
                adapter,
                viewModel.getDataSource(),
                layoutManager,
                getPaginationThreshold()
            )
        } else {
            recyclerView?.layoutManager = layoutManager
            recyclerView?.adapter = adapter
        }

        enableHideKeyboardOnScroll()
        recyclerView?.overScrollMode = View.OVER_SCROLL_NEVER
        swipeRefresh?.setOnRefreshListener { reload() }

        viewModel.getDataSource()?.observe(viewLifecycleOwner, Observer{
            onDataReceived(it)
            adapter?.submitList(it, savedInstanceState != null) { checkForEmpty() }
        })
        viewModel.loadingState?.observe(this, Observer{ setState(it) })
        viewModel.listModelState.observe(this, Observer {
            if (it != null) {
                swipeRefresh?.isEnabled = it.canRefresh
            }
        })
        if (!isRootFragment) {
            setToolbarBackIcon(toolbar)
        }
        return view
    }

    fun enableHideKeyboardOnScroll() {
        recyclerView?.addOnScrollListener(hideKeyboardScrollListener)
    }

    fun disableHideKeyboardOnScroll() {
        recyclerView?.removeOnScrollListener(hideKeyboardScrollListener)
    }

    fun showErrorView(t: Throwable?) {
        if (adapter?.isEmpty() == true) {
            removeAllOverlays(false)
            val errorView = getErrorView(t)
            errorView.setInvisible()
            overlayViews.add(errorView)
            mainLayout?.addView(
                errorView,
                CoordinatorLayout.LayoutParams(
                    CoordinatorLayout.LayoutParams.MATCH_PARENT,
                    CoordinatorLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    topMargin = getOverlayViewsMargin()
                }
            )
            errorView.fadeIn()
        } else {
            showToast(string(R.string.error_occurred))
        }
    }

    fun showEmptyView(emptyView: View = getEmptyView()) {
        removeAllOverlays(false)
        emptyView.setInvisible()
        overlayViews.add(emptyView)
        mainLayout?.addView(
            emptyView,
            CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.MATCH_PARENT,
                CoordinatorLayout.LayoutParams.MATCH_PARENT
            ).apply {
                topMargin = getOverlayViewsMargin()
            }
        )
        emptyView.fadeIn()
    }

    fun hideEmptyView(emptyView: View = getEmptyView()) {
        removeAllOverlays(false)
        emptyView.setInvisible()
        overlayViews.remove(emptyView)
        mainLayout?.removeView(
            emptyView
        )
    }

    fun removeAllOverlays(withAnimation: Boolean = true) {
        synchronized(this) {
            for (v in overlayViews) {
                if (withAnimation) {
                    v.fadeOut { mainLayout?.removeView(v) }
                } else mainLayout?.removeView(v)
            }
        }
    }

    fun checkForEmpty() {
        if (adapter?.getRealItemCount() == 0 && currentState == LoadingState.IDLE) showEmptyView()
        else hideEmptyView()
    }

    fun setState(state: LoadingState) {
        if (currentState == state) return
        currentState = state
        swipeRefresh?.isEnabled = true
        when (state) {
            LoadingState.IDLE -> {
                swipeRefresh?.isRefreshing = false
                recyclerView?.setVisible()
                progressBar?.setGone()
                adapter?.setIsLoading(false)
                removeAllOverlays()
            }
            LoadingState.LOADING -> {
                removeAllOverlays()
                if (adapter?.getRealItemCount() == 0 || state.obj == true) { //forceMainProgressBar
                    recyclerView?.setGone()
                    progressBar?.setVisible()
                    swipeRefresh?.isRefreshing = false
                    swipeRefresh?.isEnabled = false
                } else {
                    progressBar?.setGone()
                    swipeRefresh?.isRefreshing = true
                    recyclerView?.setVisible()
                }
                adapter?.setIsLoading(false)
            }
            LoadingState.LOADING_MORE -> {
                swipeRefresh?.isRefreshing = false
                progressBar?.setGone()
                recyclerView?.setVisible()
                adapter?.setIsLoading(true)
            }
            LoadingState.ERROR -> {
                swipeRefresh?.isRefreshing = false
                progressBar?.setGone()
                adapter?.setIsLoading(false)
                showErrorView(state.obj as? Throwable)
            }
        }
    }

}