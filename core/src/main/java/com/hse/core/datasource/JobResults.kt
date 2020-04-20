/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.datasource

open class PaginationResult<T>(val list: List<T>?, val obj: Any? = null, val success: Boolean = true)
open class PaginationKeyed<T, K>(list: List<T>?, val key: K, obj: Any? = null, success: Boolean = true) :
    PaginationResult<T>(list, obj, success)
