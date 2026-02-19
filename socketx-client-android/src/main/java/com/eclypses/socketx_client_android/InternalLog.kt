package com.eclypses.socketx_client_android

import android.util.Log

internal object InternalLog {
    fun d(tag: String, message: String) {
        runCatching { Log.d(tag, message) }
    }

    fun w(tag: String, message: String) {
        runCatching { Log.w(tag, message) }
    }

    fun e(tag: String, message: String) {
        runCatching { Log.e(tag, message) }
    }
}
