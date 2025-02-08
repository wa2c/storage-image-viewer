package com.wa2c.android.storageimageviewer.presentation.ui.tree.model

import com.wa2c.android.storageimageviewer.domain.model.FileModel

data class TreeScreenItemData(
    val routeList: List<FileModel> = emptyList(),
    val fileList: List<FileModel> = emptyList(),
) {
    val imageFileList: List<FileModel> = fileList.filter { !it.isDirectory }

    val currentFolder: FileModel?
       get() = routeList.lastOrNull()

    val isRoot: Boolean
        get() = routeList.size < 2

    fun getImageIndex(imageFile: FileModel?): Int {
        return imageFile?.let { imageFileList.indexOf(it) } ?: -1
    }

    fun getImageFile(index: Int): FileModel? {
        return imageFileList.getOrNull(index)
    }

    companion object {
        val List<FileModel>.dummyDigits: String
            get() = "0".repeat(this.size.toString().length)

    }
}
