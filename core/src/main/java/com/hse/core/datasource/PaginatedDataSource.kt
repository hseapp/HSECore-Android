/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.datasource

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.hse.core.common.plusAssign
import com.hse.core.enums.JobType
import com.hse.core.enums.LoadingState
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach

abstract class PaginatedDataSource<T, J : PaginationResult<T>> {
    private lateinit var coroutineScope: CoroutineScope
    private var actor: SendChannel<JobType>? = null
    private var loadingState: MutableLiveData<LoadingState>? = null
    private var externalExceptionHandler: CoroutineExceptionHandler? = null
    private var currentState = LoadingState.IDLE
    private var lastJobResult: J? = null
    private val liveData: MutableLiveData<ArrayList<T>> by lazy { MutableLiveData<ArrayList<T>>() }
    protected var hasMore = true
    private var needClear = false

    protected abstract suspend fun load(job: JobType, lastJobResult: J?): J?
    protected abstract suspend fun doAfter(lastJob: JobType, lastJobResult: J?)

    fun init(
        scope: CoroutineScope,
        loadingState: MutableLiveData<LoadingState>? = null,
        exceptionHandler: CoroutineExceptionHandler? = null
    ) {
        this.coroutineScope = scope
        this.loadingState = loadingState
        this.externalExceptionHandler = exceptionHandler
        recreateActor()
    }

    private fun recreateActor() {
        actor = coroutineScope.actor(Dispatchers.IO, UNLIMITED) {
            consumeEach {
                try {
                    when (it) {
                        JobType.LOAD_INIT -> doReset(it.obj as Boolean)
                        JobType.LOAD_NEXT -> doLoadNext()
                        JobType.CLEAR -> doClear()
                    }
                } catch (e: Throwable) {
                    val context = coroutineContext
                    if (e !is CancellationException) {
                        coroutineScope.launch(Dispatchers.Main) {
                            setState(LoadingState.ERROR)
                            externalExceptionHandler?.handleException(context, e)
                        }
                    }
                }
            }
        }
    }

    fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<ArrayList<T>>) =
        liveData.observe(lifecycleOwner, observer)

    fun isEmpty() = liveData.value?.isEmpty()

    fun reset(
        loadInitCache: Boolean,
        cancelCurrent: Boolean = false
    ): Boolean {
        if (currentState == LoadingState.LOADING) {
            if (cancelCurrent) {
                coroutineScope.coroutineContext.cancelChildren()
                recreateActor()
            } else return false
        }
        return actor?.offer(JobType.LOAD_INIT.apply { obj = loadInitCache }) ?: false
    }

    fun clear() {
        actor?.offer(JobType.CLEAR)
    }

    fun loadNext(): Boolean {
        if (!hasMore || currentState != LoadingState.IDLE) return false
        return actor?.offer(JobType.LOAD_NEXT) ?: false
    }

    private suspend fun doClear() {
        withContext(Dispatchers.Main) {
            val value = liveData.value
            value?.clear()
            liveData.value = value?: arrayListOf()
        }
        needClear = false
    }

    private suspend fun doReset(loadInitCache: Boolean) {
        hasMore = true
        if (loadInitCache) {
            val jobResult = load(JobType.LOAD_INIT_CACHE, null)
            publishData(true, jobResult?.list)
        }
        setState(LoadingState.LOADING)
        val data = load(JobType.LOAD_INIT, null)
        lastJobResult = data
        publishData(true, data?.list, LoadingState.IDLE)
        doAfter(JobType.LOAD_INIT, data)
    }

    private suspend fun doLoadNext() {
        setState(LoadingState.LOADING_MORE)
        val data = load(JobType.LOAD_NEXT, lastJobResult)
        lastJobResult = data
        publishData(false, data?.list, LoadingState.IDLE)
        doAfter(JobType.LOAD_NEXT, data)
    }

    private suspend fun publishData(
        clear: Boolean,
        list: List<T>?,
        newState: LoadingState? = null
    ) {
        if (list == null) return
        if (newState != null) currentState = newState
        withContext(Dispatchers.Main) {
            if (newState != null) loadingState?.value = newState
            if (clear) liveData.value?.clear()
            liveData += list
        }
    }

    private suspend fun setState(state: LoadingState) {
        currentState = state
        withContext(Dispatchers.Main) {
            loadingState?.value = currentState
        }
    }
}