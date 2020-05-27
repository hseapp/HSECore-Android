/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core

import android.app.Application
import android.content.Context
import com.hse.core.di.BaseAppComponent
import java.util.concurrent.ConcurrentHashMap

abstract class BaseApplication : Application() {

    init {
        appContext = this
    }

    companion object {
        lateinit var appComponent: BaseAppComponent
        lateinit var appContext: Context
        private val lifecycleObservers = ConcurrentHashMap<Int, LifecycleObserver>()
        internal var visibleActivitiesCount = 0

        internal fun onActivityResumed() {
            visibleActivitiesCount++
            if (visibleActivitiesCount == 1) {
                lifecycleObservers.forEach { it.value.onAppResumed() }
            }
        }

        internal fun onActivityPaused() {
            visibleActivitiesCount--
            if (visibleActivitiesCount == 0) {
                lifecycleObservers.forEach { it.value.onAppPaused() }
            }
        }

        internal fun addLifecycleObserver(hash: Int, observer: LifecycleObserver) {
            lifecycleObservers[hash] = observer
        }

        internal fun removeLifecycleObserver(hash: Int) {
            lifecycleObservers.remove(hash)
        }

        internal interface LifecycleObserver {
            fun onAppResumed()
            fun onAppPaused()
        }
    }

}