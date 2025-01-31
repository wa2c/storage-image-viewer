package com.wa2c.android.storageimageviewer.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FileModel(
    val storage: StorageModel,
    val uri: UriModel,
    val isDirectory: Boolean,
    val name: String,
    val mimeType: String,
    val size: Long,
    val dateModified: Long,
): Parcelable {
    val isRoot: Boolean
        get() = uri.uri.substringAfterLast('/').indexOf("%2F") < 0
}
