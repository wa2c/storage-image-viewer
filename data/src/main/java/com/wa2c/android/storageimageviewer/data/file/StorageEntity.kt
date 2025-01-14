package com.wa2c.android.storageimageviewer.data.file

import com.wa2c.android.storageimageviewer.common.values.StorageType

data class StorageEntity(
    /** ID */
    val id: String,
    /** Name */
    val name: String,
    /** URI */
    val uri: String,
    /** Path */
    val path: String,
    /** Sort Order */
    val sortOrder: Int,
    /** Storage Type */
    val storageType: StorageType,
)
