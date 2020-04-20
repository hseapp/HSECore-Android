/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.viewmodels

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.hse.core.datasource.PaginatedDataSource

abstract class PaginatedViewModel<T> : BaseViewModel() {
    val listModelState = MutableLiveData<State?>()

    abstract fun observe(
        lifecycleOwner: LifecycleOwner,
        observer: Observer<ArrayList<T>>
    )

    abstract fun getDataSource(): PaginatedDataSource<T, *>?

    init {
        listModelState.value = State()
    }
}

data class State(
    val canRefresh: Boolean = true
)