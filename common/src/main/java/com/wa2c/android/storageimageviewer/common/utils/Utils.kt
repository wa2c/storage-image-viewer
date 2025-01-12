package com.wa2c.android.storageimageviewer.common.utils

import java.util.UUID

object Utils {

    val currentTime: Long
        get() = System.currentTimeMillis()

    /**
     * Generate UUID
     */
    fun generateUUID(): String {
        return UUID.randomUUID().toString()
    }
}
