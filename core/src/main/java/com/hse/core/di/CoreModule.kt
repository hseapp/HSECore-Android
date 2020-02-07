package com.hse.core.di

import androidx.lifecycle.ViewModelProvider
import com.hse.core.common.BaseViewModelFactory
import com.hse.core.utils.KeyboardInterceptor
import com.hse.core.utils.KeyboardInterceptorImpl
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class CoreModule {
    @Binds
    internal abstract fun bindViewModelFactory(factoryBase: BaseViewModelFactory): ViewModelProvider.Factory

    @Singleton
    @Binds
    abstract fun keyboardInterceptor(keyboardInterceptorImpl: KeyboardInterceptorImpl): KeyboardInterceptor
}