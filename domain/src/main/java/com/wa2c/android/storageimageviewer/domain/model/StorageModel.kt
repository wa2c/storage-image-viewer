package com.wa2c.android.storageimageviewer.domain.model

import com.wa2c.android.storageimageviewer.common.value.StorageType

data class StorageModel(
    val id: String,
    val name: String,
    val uri: UriModel,
    val type: StorageType,
    val sortOrder: Int,
)
