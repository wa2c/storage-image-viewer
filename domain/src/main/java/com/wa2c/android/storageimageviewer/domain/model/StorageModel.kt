package com.wa2c.android.storageimageviewer.domain.model

import com.wa2c.android.storageimageviewer.common.value.StorageType

data class StorageModel(
    val id: String,
    val uri: UriModel,
    val name: String,
    val type: StorageType,
)
