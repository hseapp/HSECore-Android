/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.utils

import android.app.Activity
import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import javax.inject.Inject

enum class KeyboardEvent { SHOWN, HIDDEN }

interface KeyboardInterceptor {
    fun subscribe(): ReceiveChannel<KeyboardEvent>
    fun publishEvent(event: KeyboardEvent)
}

class KeyboardInterceptorImpl @Inject constructor() :
    KeyboardInterceptor {
    private val channel = BroadcastChannel<KeyboardEvent>(Channel.BUFFERED)
    override fun subscribe() = channel.openSubscription()

    override fun publishEvent(event: KeyboardEvent) {
        channel.offer(event)
    }
}

class KeyboardListener(activity: Activity, val keyboardInterceptor: KeyboardInterceptor) {
    private val contentView = activity.findViewById<View>(android.R.id.content) as ViewGroup
    private var contentHeight = 0
    private val listener = ViewTreeObserver.OnGlobalLayoutListener {
        val rect = Rect()
        contentView.getWindowVisibleDisplayFrame(rect)
        val keyboardHeight = contentHeight - rect.height()
        if (keyboardHeight > contentHeight / 5f) {
            keyboardInterceptor.publishEvent(KeyboardEvent.SHOWN)
        } else {
            keyboardInterceptor.publishEvent(KeyboardEvent.HIDDEN)
        }
    }

    init {
        val displayMetrics =
            DisplayMetrics().apply { activity.windowManager.defaultDisplay.getMetrics(this) }
        contentHeight = displayMetrics.heightPixels
        contentView.viewTreeObserver.addOnGlobalLayoutListener(listener)
    }

    fun clean() {
        contentView.viewTreeObserver.removeOnGlobalLayoutListener(listener)
    }
}