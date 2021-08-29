/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.viewmodels

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hse.core.enums.LoadingState

abstract class BaseViewModel : ViewModel() {
    abstract val loadingState: MutableLiveData<LoadingState>?
    var params: ViewModelParams? = null

    private var paramsReadyListeners = arrayListOf<(params: ViewModelParams?) -> Unit>()

    fun doOnParamsReady(unit: (params: ViewModelParams?) -> Unit) {
        paramsReadyListeners.add(unit)
    }

    open fun onSaveInstanceState(bundle: Bundle?) = Unit
    @CallSuper
    open fun onViewStateRestored(bundle: Bundle?) {
        if (bundle == null) {
            for (p in paramsReadyListeners) p.invoke(params)
            paramsReadyListeners.clear()
        }
    }
}

open class ViewModelParams