package com.wa2c.android.storageimageviewer.domain.model

import com.wa2c.android.storageimageviewer.common.values.StorageType

data class StorageModel(
    val id: String,
    val name: String,
    val uri: UriModel,
    val type: StorageType,
    val sortOrder: Int,
)
