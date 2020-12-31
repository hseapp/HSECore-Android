package com.hse.core.common

import android.os.Parcel

fun <T> Parcel.writeListSafe(list: List<T>?) {
    val size = list?.size ?: -1
    writeInt(size)
    list?.forEach {
        writeValue(it)
    }
}

inline fun <reified T> Parcel.readListSafe(): List<T>? {
    val size = readInt()
    if (size < 0) return null
    val list = ArrayList<T>()
    for (i in 0 until size) {
        list.add(readValue(T::class.javaClass.classLoader) as T)
    }
    return list
}