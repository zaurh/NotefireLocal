package com.zaurh.notefirelocal.core

/** Current time in epoch millis; replaced in tests. */
fun interface Clock {
    fun now(): Long
}
