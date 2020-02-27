/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core

import android.app.Application
import android.content.Context
import com.hse.core.di.BaseAppComponent

abstract class BaseApplication : Application() {

    init {
        appContext = this
    }

    companion object {
        lateinit var appComponent: BaseAppComponent
        lateinit var appContext: Context
    }

}