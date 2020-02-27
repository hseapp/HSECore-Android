/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.enums

enum class JobType(var obj: Any? = null) {
    LOAD_INIT_CACHE,
    LOAD_INIT,
    LOAD_NEXT,
    CLEAR;
}