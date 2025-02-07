package com.wa2c.android.storageimageviewer.domain.model

import android.os.Parcelable
import com.wa2c.android.storageimageviewer.common.values.StorageType
import kotlinx.parcelize.Parcelize

@Parcelize
data class StorageModel(
    val id: String,
    val name: String,
    val uri: UriModel,
    val rootUri: UriModel,
    val type: StorageType,
    val sortOrder: Int,
): Parcelable {

    val isNew: Boolean
        get() = id.isEmpty()

    companion object {
        val Empty = StorageModel(
            id = "",
            name = "",
            uri = UriModel(""),
            rootUri = UriModel(""),
            type = StorageType.SAF,
            sortOrder = 0,
        )
    }

}
