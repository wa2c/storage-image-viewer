package com.wa2c.android.storageimageviewer.domain.model

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class TreeDataModel(
    val dir: FileModel? = null,
    val fileList: List<FileModel> = emptyList(),
): Parcelable {
    @IgnoredOnParcel
    val imageFileList: List<FileModel> = fileList.filter { !it.isDirectory }

    companion object {
        val List<FileModel>.dummyDigits: String
            get() = "0".repeat(this.size.toString().length)

    }
}
