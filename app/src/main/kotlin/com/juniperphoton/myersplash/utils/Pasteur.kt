package com.juniperphoton.myersplash.utils

import android.util.Log

@Suppress("unused", "MemberVisibilityCanPrivate")
object Pasteur {
    private const val DEFAULT_TAG = "MYERSPLASH"

    private var debugMode: Boolean = false

    fun init(debug: Boolean) {
        debugMode = debug
    }

    fun d(tag: String?, string: String) {
        debug(tag, string)
    }

    fun d(tag: String, block: () -> String) {
        if (debugMode) {
            Log.d(tag, block())
        }
    }

    fun debug(tag: String?, string: String) {
        if (debugMode) {
            Log.d(tag ?: DEFAULT_TAG, string)
        }
    }

    fun i(tag: String?, string: String) {
        info(tag, string)
    }

    fun i(tag: String, block: () -> String) {
        if (debugMode) {
            Log.i(tag, block())
        }
    }

    fun info(tag: String?, string: String) {
        if (debugMode) {
            Log.i(tag ?: DEFAULT_TAG, string)
        }
    }

    fun w(tag: String?, string: String) {
        warn(tag, string)
    }

    fun w(tag: String, block: () -> String) {
        if (debugMode) {
            Log.w(tag, block())
        }
    }

    fun warn(tag: String?, string: String) {
        if (debugMode) {
            Log.w(tag ?: DEFAULT_TAG, string)
        }
    }

    fun e(tag: String?, string: String) {
        error(tag, string)
    }

    fun e(tag: String, block: () -> String) {
        if (debugMode) {
            Log.e(tag, block())
        }
    }

    fun error(tag: String?, string: String) {
        if (debugMode) {
            Log.e(tag ?: DEFAULT_TAG, string)
        }
    }
}