package com.juniperphoton.myersplash

import androidx.annotation.UiThread

open class LiveDataEvent<T>(private val data: T) {
    private var handled = false

    @UiThread
    fun peek() = data

    @UiThread
    fun pop(): T? = if (!handled) {
        handled = true
        data
    } else {
        null
    }

    @UiThread
    inline fun consume(block: (T) -> Unit) {
        pop()?.let {
            block(it)
        }
    }
}

class LongLiveDataEvent(val value: Long) : LiveDataEvent<Long>(value)

fun <T> T.toLiveDataEvent(): LiveDataEvent<T> {
    return LiveDataEvent(this)
}

val <T> T.liveDataEvent: LiveDataEvent<T>
    get() = LiveDataEvent(this)