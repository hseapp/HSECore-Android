package com.hse.core.di

import com.hse.core.ui.BaseActivity

interface BaseAppComponent {
    fun inject(baseActivity: BaseActivity)
}