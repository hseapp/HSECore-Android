/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.viewmodels

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hse.core.enums.LoadingState

abstract class BaseViewModel : ViewModel() {
    abstract val loadingState: MutableLiveData<LoadingState>?
    var params: ViewModelParams? = null
        set(value) {
            field = value
            for (p in paramsReadyListeners) p.invoke(value)
            paramsReadyListeners.clear()
        }

    private var paramsReadyListeners = arrayListOf<(params: ViewModelParams?) -> Unit>()

    fun doOnParamsReady(unit: (params: ViewModelParams?) -> Unit) {
        paramsReadyListeners.add(unit)
    }

    open fun onSaveInstanceState(bundle: Bundle?) = Unit
    open fun onViewStateRestored(bundle: Bundle?) = Unit
}

open class ViewModelParams