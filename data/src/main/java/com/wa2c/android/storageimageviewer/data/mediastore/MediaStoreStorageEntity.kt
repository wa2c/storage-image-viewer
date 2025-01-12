package com.wa2c.android.storageimageviewer.data.mediastore

import com.wa2c.android.storageimageviewer.common.values.StorageType

data class MediaStoreStorageEntity(
    /** ID */
    val id: String,
    /** Name */
    val name: String,
    /** URI */
    val uri: String,
    /** Sort Order */
    val sortOrder: Int,
    /** Storage Type */
    val storageType: StorageType,
)
