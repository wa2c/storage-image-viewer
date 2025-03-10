package com.wa2c.android.storageimageviewer.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UriModel(
    val uri: String,
): Parcelable {
    val isInvalidUri: Boolean
        get() = uri.isEmpty()
}
