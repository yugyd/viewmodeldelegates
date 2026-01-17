package com.yugyd.viewmodeldelegates

interface ViewModelLogger {
    fun log(message: String)
    fun throwIfDebug(error: Throwable)
}
