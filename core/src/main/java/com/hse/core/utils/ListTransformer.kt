package com.hse.core.utils

interface ListTransformer<T> {
    fun transform(obj:T) : List<T>?
}