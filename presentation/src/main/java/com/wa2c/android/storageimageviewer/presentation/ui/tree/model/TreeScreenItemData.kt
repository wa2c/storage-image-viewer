package com.wa2c.android.storageimageviewer.presentation.ui.tree.model

import com.wa2c.android.storageimageviewer.domain.model.FileModel

data class TreeScreenItemData(
    val dir: FileModel? = null,
    val fileList: List<FileModel> = emptyList(),
) {
    val imageFileList: List<FileModel> = fileList.filter { !it.isDirectory }

    val isRoot: Boolean
        get() = dir?.isRoot ?: true

    companion object {
        val List<FileModel>.dummyDigits: String
            get() = "0".repeat(this.size.toString().length)

    }
}
