package com.wa2c.android.storageimageviewer.domain.model

data class UriModel(
    val uri: String,
) {
    val isInvalidUri: Boolean
        get() = uri.isEmpty()
}
