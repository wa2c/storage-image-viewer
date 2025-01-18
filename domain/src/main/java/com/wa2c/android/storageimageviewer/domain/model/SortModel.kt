package com.wa2c.android.storageimageviewer.domain.model

import com.wa2c.android.storageimageviewer.common.values.SortType

data class SortModel(
    val type: SortType = SortType.Name,
    val isDescending: Boolean = false,
    val isIgnoreCase: Boolean = false,
    val isNumberSort: Boolean = false,
    val isFolderMixed: Boolean = false,
)
