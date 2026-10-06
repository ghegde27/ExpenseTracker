package com.example.fitnesstracker.logging

import android.util.Log
import com.example.fitnesstracker.BuildConfig
import javax.inject.Inject

class AppLogger @Inject constructor() {
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.d(tag, message)
    }

    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.i(tag, message)
    }

    fun w(tag: String, message: String) {
        Log.w(tag, message)
    }

    fun e(tag: String, message: String, error: Throwable? = null) {
        Log.e(tag, message, error)
    }
}
