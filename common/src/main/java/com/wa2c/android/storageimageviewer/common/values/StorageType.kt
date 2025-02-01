package com.wa2c.android.storageimageviewer.common.values

/**
 * Storage type
 */
enum class StorageType(
    val value: String,
) {
    /** Device storage */
    Device("DEVICE"),
    /** SD storage */
    SD("SD"),
    /** USB storage */
    USB("USB"),
    /** Download */
    Download("DOWNLOAD"),
    /** Storage Access Framework storage */
    SAF("SAF"),
    ;

    companion object {
        fun fromValue(value: String): StorageType {
            return entries.firstOrNull { it.value == value } ?: SAF
        }
    }
}
