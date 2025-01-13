package com.wa2c.android.storageimageviewer.domain.model

import android.os.Parcelable
import com.wa2c.android.storageimageviewer.common.values.UriType
import kotlinx.parcelize.Parcelize

@Parcelize
data class UriModel(
    val uri: String,
): Parcelable {
    val isInvalidUri: Boolean
        get() = uri.isEmpty()

    val uriType: UriType
        get() = if (uri.startsWith("content://")) {
            UriType.Content
        } else {
            UriType.File
        }
}
