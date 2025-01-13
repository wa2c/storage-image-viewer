package com.wa2c.android.storageimageviewer.data.file

/**
 * File Entity
 */
data class FileEntity(
    /** URI */
    val uri: String,
    /** Name */
    val name: String,
    /** True if directory */
    val isDirectory: Boolean,
    /** Mime Type */
    val mimeType: String,
    /** Size */
    val size: Long,
    /** Date Modified */
    val dateModified: Long,
)
