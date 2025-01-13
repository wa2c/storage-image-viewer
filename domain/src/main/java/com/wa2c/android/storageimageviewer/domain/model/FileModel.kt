package com.wa2c.android.storageimageviewer.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FileModel(
    val storage: StorageModel,
    val path: String,
    val isDirectory: Boolean,
    val name: String,
    val mimeType: String,
    val size: Long,
    val dateModified: Long,
): Parcelable {

    val uriText: String
        get() = storage.uri.uri.trimEnd('/') + '/' + path.trimStart('/')

}
