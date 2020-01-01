package com.juniperphoton.myersplash.extension

import java.io.Closeable

fun <T : Closeable, R : Closeable> using(t: T, r: R, block: (T, R) -> Unit) {
    t.use {
        r.use {
            block(t, r)
        }
    }
}

fun <T : Closeable, R : Closeable> T.useWith(r: R, block: (T, R) -> Unit) {
    use {
        r.use {
            block(this, r)
        }
    }
}