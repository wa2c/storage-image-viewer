package com.wa2c.android.storageimageviewer.common.utils

import timber.log.Timber

object Log {

    /**
     * Initialize log
     */
    fun init(isDebug: Boolean) {
        // Set logger
        if (isDebug) {
            Timber.plant(Timber.DebugTree())
        }
    }

    /** Output the verbose message */
    fun v(obj: Any?, vararg args: Any?) = run {
        if (obj is Throwable) { Timber.asTree().v(obj) }
        Timber.asTree().v(obj.toString(), *args)
    }
    /** Output the debug message */
    fun d(obj: Any?, vararg args: Any?) = run {
        if (obj is Throwable) { Timber.asTree().d(obj) }
        Timber.asTree().d(obj.toString(), *args)
    }
    /** Output the info message */
    fun i(obj: Any?, vararg args: Any?) = run {
        if (obj is Throwable) { Timber.asTree().i(obj) }
        Timber.asTree().i(obj.toString(), *args)
    }
    /** Output the warning message */
    fun w(obj: Any?, vararg args: Any?) = run {
        if (obj is Throwable) { Timber.asTree().w(obj) }
        Timber.asTree().w(obj.toString(), *args)
    }
    /** Output the error message */
    fun e(obj: Any?, vararg args: Any?) = run {
        if (obj is Throwable) { Timber.asTree().e(obj) }
        Timber.asTree().e(obj.toString(), *args)
    }

}
