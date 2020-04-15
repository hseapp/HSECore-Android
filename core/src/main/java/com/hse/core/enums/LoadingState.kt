/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.enums

enum class LoadingState(var obj: Any? = null) {
    IDLE,
    LOADING,
    LOADING_MORE,
    ERROR,
    DONE;
}