package com.wa2c.android.storageimageviewer.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TreeData(
    val dir: FileModel? = null,
    val fileList: List<FileModel> = emptyList(),
): Parcelable
