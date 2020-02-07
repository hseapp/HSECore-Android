package com.hse.core.enums

enum class JobType(var obj: Any? = null) {
    LOAD_INIT_CACHE,
    LOAD_INIT,
    LOAD_NEXT,
    CLEAR;
}