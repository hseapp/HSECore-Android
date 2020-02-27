/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.di

import com.hse.core.ui.BaseActivity

interface BaseAppComponent {
    fun inject(baseActivity: BaseActivity)
}