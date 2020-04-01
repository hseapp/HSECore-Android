/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.ui

import android.app.Activity
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.hse.core.R
import com.hse.core.common.activity
import com.hse.core.common.animateTranslationZ
import com.hse.core.common.dp
import com.hse.core.navigation.CommonViewParamHolder
import com.hse.core.ui.BaseFragment.Builder.Companion.COMMON_VIEWS
import com.hse.core.viewmodels.BaseViewModel


abstract class BaseFragment<T : BaseViewModel> : Fragment() {
    var isRootFragment = false
    val commonViews = HashMap<Int, CommonViewParamHolder>()
    open var flags = 0
    lateinit var viewModel: T
    private var appBarOffset = 0
    private val appBarOffsetThreshold = dp(48f)

    abstract fun provideViewModel(): T
    abstract fun getFragmentTag(): String

    open fun createView(
        view: View,
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) {
    }

    open fun canFinish() = true
    open fun onFragmentStackSelected() = true

    private var postponedFinish = false

    fun finish() {
        if (isHidden) postponedFinish = true
        else activity()?.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = provideViewModel()
        arguments?.getParcelableArrayList<CommonViewParamHolder>(COMMON_VIEWS)?.forEach {
            commonViews[it.id] = it
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && postponedFinish) activity()?.onBackPressed()
    }

    fun enableAppBarShadowHandling(
        recyclerView: RecyclerView?,
        appBarLayout: AppBarLayout?
    ) {
        if (appBarLayout == null || recyclerView == null) return

        val layoutManager = recyclerView.layoutManager
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, top ->
            appBarOffset = top
        })

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                checkAppBarShadow(appBarLayout, layoutManager as? LinearLayoutManager)
            }
        })
    }

    private var isAppBarAnimating = false
    private fun checkOffset(appBarLayout: AppBarLayout?, childY: Float) {
        if (!isAppBarAnimating && (appBarOffset <= -appBarOffsetThreshold || childY < 0)) {
            isAppBarAnimating = true
            appBarLayout?.animateTranslationZ(dp(4f)) {
                isAppBarAnimating = false
            }
        }
    }

    protected open fun checkAppBarShadow(
        appBarLayout: AppBarLayout?,
        layoutManager: LinearLayoutManager?
    ) {
        if (layoutManager is LinearLayoutManager) {
            val firstVisible = layoutManager.findFirstVisibleItemPosition()
            val child = layoutManager.getChildAt(0) ?: return
            val childY = child.y

            if (childY == 0f && firstVisible == 0) {
                appBarLayout?.animateTranslationZ(0f)
            } else {
                checkOffset(appBarLayout, childY)
            }
        }
    }

    protected fun setToolbarBackIcon(toolbar: Toolbar?) {
        if (toolbar == null) return
        toolbar.contentInsetStartWithNavigation = 0
        toolbar.setContentInsetsAbsolute(0, 0)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_gray_action)
        toolbar.setNavigationOnClickListener { activity()?.onBackPressed() }
    }

    open class Builder(private val fragment: Class<out BaseFragment<*>>) {
        val arguments = Bundle()
        private val commonViews = ArrayList<CommonViewParamHolder>()

        fun putCommonView(view: View?): Builder {
            if (view != null) {
                val viewLocation = IntArray(2)
                view.getLocationInWindow(viewLocation)
                val rectangle = Rect()
                val window = (view.context as? Activity)?.window ?: return this
                window.decorView.getWindowVisibleDisplayFrame(rectangle)
                val relativeLeft = viewLocation[0]
                val relativeTop = viewLocation[1] - rectangle.top

                commonViews.add(
                    CommonViewParamHolder(
                        view.id,
                        view.width,
                        view.height,
                        relativeLeft.toFloat(),
                        relativeTop.toFloat()
                    )
                )
            }
            return this
        }

        fun go(ctx: BaseActivity?, rootTag: String? = null) {
            arguments.putParcelableArrayList(COMMON_VIEWS, commonViews)
            val fragment = fragment.newInstance().apply {
                arguments = this@Builder.arguments
            }
            if (rootTag == null) ctx?.navigation?.addFragment(fragment)
            else ctx?.navigation?.addFragment(fragment, rootTag)
        }

        companion object {
            const val COMMON_VIEWS = "common_views"
        }
    }
}

