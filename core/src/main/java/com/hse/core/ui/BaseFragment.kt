/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntRange
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
import com.hse.core.ui.BaseFragment.Builder.Companion.ARG_COMMON_VIEWS
import com.hse.core.ui.BaseFragment.Builder.Companion.ARG_FLAGS
import com.hse.core.ui.BaseFragment.Builder.Companion.ARG_IS_ROOT
import com.hse.core.ui.BaseFragment.Builder.Companion.ARG_RANDOM_KEY
import com.hse.core.ui.BaseFragment.Builder.Companion.ARG_REQUEST_CODE
import com.hse.core.viewmodels.BaseViewModel


abstract class BaseFragment<T : BaseViewModel> : Fragment() {
    open var flags: Int? = null
        get() {
            if (field == null) {
                field = arguments?.getInt(ARG_FLAGS) ?: 0
            }
            return field
        }
    var isRootFragment: Boolean = false
        get() {
            if (!field) {
                field = arguments?.getBoolean(ARG_IS_ROOT) ?: false
            }
            return field
        }
    open val hasTransparentStatusBar = false
    val commonViews = HashMap<Int, CommonViewParamHolder>()
    lateinit var viewModel: T

    internal var requestCode = -1
    internal var resultData: Intent? = null
    internal var resultCode = Activity.RESULT_CANCELED

    private var appBarOffset = 0
    private val appBarOffsetThreshold = dp(48f)

    private var doOnReady: (() -> Unit)? = null

    var mainLayout: ViewGroup? = null
    var toolbar: Toolbar? = null
    var appBarLayout: AppBarLayout? = null

    abstract fun provideViewModel(): T
    abstract fun getFragmentTag(): String
    abstract fun provideView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?

    open fun createView(
        view: View,
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = Unit

    open fun canFinish() = true
    open fun onFragmentStackSelected() = true
    open fun onNewIntent(intent: Intent?) {}
    open fun onFilePicked(intent: Intent?) {}

    internal fun computeFragmentTag() = getFragmentTag() + (arguments?.getInt(ARG_RANDOM_KEY) ?: (0..Integer.MAX_VALUE).random().toString())
    private var postponedFinish = false

    fun finish() {
        if (isHidden) postponedFinish = true
        else activity()?.onBackPressed()
    }

    fun setResult(result: Int, data: Intent? = null) {
        this.resultCode = result
        this.resultData = data
    }

    fun doWhenReady(task: () -> Unit) {
        if (::viewModel.isInitialized) task()
        else doOnReady = task
    }

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = provideView(inflater, container, savedInstanceState)
//        if (hasTransparentStatusBar) {
//            view?.setOnApplyWindowInsetsListener { v, insets ->
//                (toolbar?.layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin = insets.systemWindowInsetTop
//                insets
//            }
//        }
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = provideViewModel()
        arguments?.getParcelableArrayList<CommonViewParamHolder>(ARG_COMMON_VIEWS)?.forEach {
            commonViews[it.id] = it
        }
        requestCode = arguments?.getInt(ARG_REQUEST_CODE) ?: -1
        doOnReady?.invoke()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && postponedFinish) activity()?.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == Activity.RESULT_OK) {
            onFilePicked(data)
        }
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

    fun pickFile(type: String = "*/*") {
        val mediaIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        mediaIntent.addCategory(Intent.CATEGORY_OPENABLE)
        mediaIntent.type = type
        activity?.startActivityForResult(mediaIntent, REQUEST_CODE_PICK_FILE)
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
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24)
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

        fun go(
            ctx: BaseActivity?,
            rootTag: String? = null,
            @IntRange(from = -1, to = Int.MAX_VALUE.toLong()) requestCode: Int = -1
        ) {
            arguments.putInt(ARG_RANDOM_KEY, (0..Integer.MAX_VALUE).random())
            arguments.putParcelableArrayList(ARG_COMMON_VIEWS, commonViews)
            arguments.putInt(ARG_REQUEST_CODE, requestCode)
            val fragment = fragment.newInstance().apply {
                arguments = this@Builder.arguments
            }
            if (rootTag == null) ctx?.navigation?.addFragment(fragment)
            else ctx?.navigation?.addFragment(fragment, rootTag)
        }

        fun setFlags(flags: Int) {
            arguments.putInt(ARG_FLAGS, flags)
        }

        fun setIsRootFragment() {
            arguments.putBoolean(ARG_IS_ROOT, true)
        }

        companion object {
            const val ARG_RANDOM_KEY = "f_random_key"
            const val ARG_COMMON_VIEWS = "f_common_views"
            const val ARG_REQUEST_CODE = "f_request_code"
            const val ARG_FLAGS = "f_flags"
            const val ARG_IS_ROOT = "f_is_root"
        }
    }

    companion object {
        const val REQUEST_CODE_PICK_FILE = 2142
    }
}

