package com.filepickersample.utils

import android.util.Log
import com.filepickersample.BuildConfig

object MediaLog {
    private var className: String? = null
    private var methodName: String? = null
    private var lineNumber = 0
    private val isDebuggable = BuildConfig.DEBUG

    private fun createLog(log: String): String {
        val buffer = StringBuffer()
        buffer.append("[")
        buffer.append(methodName)
        buffer.append(":")
        buffer.append(lineNumber)
        buffer.append("]")
        buffer.append(log)
        return buffer.toString()
    }

    private fun getMethodNames(sElements: Array<StackTraceElement>) {
        className = sElements[1].fileName
        methodName = sElements[1].methodName
        lineNumber = sElements[1].lineNumber
    }

    fun e(message: String) {
        if (!isDebuggable) return
        // Throwable instance must be created before any methods
        getMethodNames(Throwable().stackTrace)
        Log.e(className, createLog(message))
    }

    fun e(tag: String, message: String) {
        if (!isDebuggable) return
        getMethodNames(Throwable().stackTrace)
        Log.e(tag, createLog(message))
    }

    fun i(message: String) {
        if (!isDebuggable) return
        getMethodNames(Throwable().stackTrace)
        Log.i(className, createLog(message))
    }

    fun d(message: String) {
        if (!isDebuggable) return
        getMethodNames(Throwable().stackTrace)
        Log.d(className, createLog(message))
    }

    fun d(tag: String, message: String) {
        if (!isDebuggable) return
        getMethodNames(Throwable().stackTrace)
        Log.d(tag, createLog(message))
    }

    fun v(message: String) {
        if (!isDebuggable) return
        getMethodNames(Throwable().stackTrace)
        Log.v(className, createLog(message))
    }

    fun w(message: String) {
        if (!isDebuggable) return
        getMethodNames(Throwable().stackTrace)
        Log.w(className, createLog(message))
    }

    fun w(tag: String, message: String) {
        if (!isDebuggable) return
        getMethodNames(Throwable().stackTrace)
        Log.w(tag, createLog(message))
    }

    fun wtf(message: String) {
        if (!isDebuggable) return
        getMethodNames(Throwable().stackTrace)
        Log.wtf(className, createLog(message))
    }
}