package com.wa2c.android.storageimageviewer.domain.model

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class TreeData(
    val dir: FileModel? = null,
    val fileList: List<FileModel> = emptyList(),
): Parcelable {
    @IgnoredOnParcel
    val imageFileList: List<FileModel> = fileList.filter { !it.isDirectory }

    @IgnoredOnParcel
    val dummyDigit = "0".repeat(imageFileList.size.toString().length)
}
