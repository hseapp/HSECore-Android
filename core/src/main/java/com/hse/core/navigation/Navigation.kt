/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.navigation

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.hse.core.R
import com.hse.core.ui.BaseFragment
import com.hse.core.ui.BaseFragment.Builder.Companion.ARG_REQUEST_CODE
import kotlinx.android.parcel.Parcelize

interface NavigationCallback {
    fun getRootFragment(rootTag: String): BaseFragment<*>
    fun onStackChanged(newRootTag: String)
    fun onTopFragmentChanged(fragment: BaseFragment<*>?, rootTag: String)
}

class Navigation(
    savedInstanceState: Bundle?,
    @IdRes private val container: Int,
    private val fragmentManager: FragmentManager,
    private val listener: NavigationCallback
) {
    private var currentRoot: String = "DEFAULT"
    private var fragmentsMap = HashMap<String, ArrayList<BaseFragment<*>>>()

    init {
        savedInstanceState?.let {
            val hashMap = it.getSerializable("map") as? HashMap<String, Array<String?>>?
            hashMap?.forEach { entry ->
                val key = entry.key
                val array = entry.value
                val arrayList = ArrayList<BaseFragment<*>>()
                array.forEachIndexed { index, tag ->
                    val fragment = fragmentManager.findFragmentByTag(tag) as BaseFragment<*>
                    fragment.isRootFragment = index == 0
                    arrayList.add(fragment)
                }
                fragmentsMap[key] = arrayList
            }
            currentRoot = it.getString("current_root", "DEFAULT")
            listener.onStackChanged(currentRoot)
        }
    }

    fun onSaveInstanceState(bundle: Bundle) {
        val hashMap = HashMap<String, Array<String?>>()
        fragmentsMap.forEach { entry ->
            val key = entry.key
            val list = entry.value
            val array = arrayOfNulls<String>(list.size)
            list.forEachIndexed { index, fragment ->
                array[index] = fragment.computeFragmentTag()
            }
            hashMap[key] = array
        }
        bundle.putSerializable("map", hashMap)
        bundle.putString("current_root", currentRoot)
    }

    fun onBackPressed(): Boolean {
        val list = fragmentsMap[currentRoot]
        if (list.isNullOrEmpty() || list.size == 1) return false
        val topFragment = list[list.size - 1]
        if (!topFragment.canFinish()) return true
        val resultCode = topFragment.resultCode
        val resultData = topFragment.resultData
        val requestCode = topFragment.requestCode
        removeFragment(topFragment, list)
        val currentFragment = showTopFragmentInStack(currentRoot)
        if (requestCode >= 0) {
            currentFragment?.onActivityResult(requestCode, resultCode, resultData)
        }
        return true
    }

    fun getCurrentTopFragment(rootTag: String = currentRoot): BaseFragment<*>? {
        val list = getRootListOrCreate(rootTag)
        if (list.isEmpty()) return null
        return list[list.size - 1]
    }

    fun switchStack(rootTag: String) {
        if (currentRoot == rootTag) {
            val list = getRootListOrCreate(rootTag)
            if (list.isNotEmpty()) {
                val topFragment = list[list.size - 1]
                if (topFragment.onFragmentStackSelected()) {
                    dropStack(rootTag)
                }
                return
            }
        }
        hideAllInStack(currentRoot)
        currentRoot = rootTag
        listener.onStackChanged(currentRoot)
        val list = getRootListOrCreate(rootTag)
        if (list.isEmpty()) {
            val rootFragment = listener.getRootFragment(rootTag)
            rootFragment.isRootFragment = true
            addFragment(rootFragment)
            return
        }
        showTopFragmentInStack(rootTag, false)
    }

    fun addFragment(fragment: BaseFragment<*>, rootTag: String = currentRoot) {
        if (currentRoot != rootTag) {
            switchStack(rootTag)
            addFragment(fragment, rootTag)
            return
        }
        hideTopFragmentInStack(rootTag)
        val list = getRootListOrCreate(rootTag)
        list.add(fragment)
        val t = fragmentManager.beginTransaction()
        if (!fragment.isRootFragment) {
            if (fragment.arguments?.getInt(ARG_REQUEST_CODE) != -1) t.animatePopUp() else t.animateFade()
        }
        t.add(container, fragment, fragment.computeFragmentTag()).commitAllowingStateLoss()
        val currentTopFragment = getCurrentTopFragment()
        listener.onTopFragmentChanged(currentTopFragment, currentRoot)
        checkStatusBar(currentTopFragment)
    }

    private fun getRootListOrCreate(rootTag: String): ArrayList<BaseFragment<*>> {
        var list = fragmentsMap[rootTag]
        if (list == null) {
            list = ArrayList()
            fragmentsMap[rootTag] = list
        }
        return list
    }

    private fun removeFragment(fragment: BaseFragment<*>, list: ArrayList<BaseFragment<*>>) {
        list.remove(fragment)
        fragmentManager.beginTransaction()
            .animateFade()
            .remove(fragment)
            .commitAllowingStateLoss()
    }

    private fun removeFragment(fragment: BaseFragment<*>) {
        fragmentsMap.forEach T@{
            it.value.forEach { f ->
                if (f == fragment) {
                    removeFragment(f, it.value)
                    return@T
                }
            }
        }
    }

    fun dropStack(rootTag: String = currentRoot) {
        val list = getRootListOrCreate(rootTag)
        val transaction = fragmentManager.beginTransaction()
        val rootFragment = list[0]
        list.forEachIndexed { index, fragment ->
            if (index > 0) {
                transaction.remove(fragment)
            }
        }
        list.clear()
        list.add(rootFragment)
        transaction.commitAllowingStateLoss()
        showTopFragmentInStack(rootTag)
    }

    private fun hideTopFragmentInStack(rootTag: String) {
        val list = getRootListOrCreate(rootTag)
        if (list.isEmpty()) return
        val fragment = list[list.size - 1]
        fragmentManager.beginTransaction()
            .animateFade()
            .hide(fragment)
            .commitAllowingStateLoss()
    }

    private fun hideAllInStack(rootTag: String) {
        val list = getRootListOrCreate(rootTag)
        val transaction = fragmentManager.beginTransaction()
        for (f in list) transaction.hide(f)
        transaction.commitAllowingStateLoss()
    }

    private fun showTopFragmentInStack(rootTag: String, animate: Boolean = true): BaseFragment<*>? {
        val fragment = getCurrentTopFragment(rootTag) ?: return null
        val t = fragmentManager.beginTransaction()
        if (animate) t.animateFade()
        t.show(fragment).commitAllowingStateLoss()
        listener.onTopFragmentChanged(fragment, currentRoot)
        checkStatusBar(fragment)
        return fragment
    }


    private fun checkStatusBar(currentFragment: BaseFragment<*>?) {
        if (currentFragment == null) return
//        currentFragment.lifecycleScope.launchWhenCreated {
//            if (currentFragment.hasTransparentStatusBar) {
//                currentFragment.activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
////                activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
//            } else {
//                currentFragment.activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
////                currentFragment.activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
//            }
//        }
    }

    private fun FragmentTransaction.animateFade(): FragmentTransaction {
        setCustomAnimations(
            R.animator.fragment_fade_in,
            R.animator.fragment_fade_out,
            R.animator.fragment_fade_in,
            R.animator.fragment_fade_out
        )
        return this
    }

    private fun FragmentTransaction.animatePopUp(): FragmentTransaction {
        setCustomAnimations(
            R.animator.fragment_pop_up,
            R.animator.fragment_fade_out,
            R.animator.fragment_pop_up,
            R.animator.fragment_fade_out
        )
        return this
    }
}

@Parcelize
data class CommonViewParamHolder(
    val id: Int,
    val width: Int,
    val height: Int,
    val x: Float,
    val y: Float
) : Parcelable