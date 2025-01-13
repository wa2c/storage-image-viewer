package com.wa2c.android.storageimageviewer.domain.model

import android.os.Parcelable
import com.wa2c.android.storageimageviewer.common.values.StorageType
import kotlinx.parcelize.Parcelize

@Parcelize
data class StorageModel(
    val id: String,
    val name: String,
    val uri: UriModel,
    val type: StorageType,
    val sortOrder: Int,
): Parcelable {

    companion object {
        val Empty = StorageModel(
            id = "",
            name = "",
            uri = UriModel(""),
            type = StorageType.SAF,
            sortOrder = 0,
        )
    }

}
