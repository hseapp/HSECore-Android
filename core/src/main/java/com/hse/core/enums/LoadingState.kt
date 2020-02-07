package com.hse.core.enums

enum class LoadingState(var obj: Any? = null) {
    IDLE,
    LOADING,
    LOADING_MORE,
    ERROR;
}