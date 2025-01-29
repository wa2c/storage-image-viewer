package com.wa2c.android.storageimageviewer.domain.model

import android.os.Parcelable
import com.wa2c.android.storageimageviewer.common.values.TreeSortType
import kotlinx.parcelize.Parcelize

@Parcelize
data class TreeSortModel(
    val type: TreeSortType = TreeSortType.Name,
    val isDescending: Boolean = false,
    val isIgnoreCase: Boolean = false,
    val isNumberSort: Boolean = false,
    val isFolderMixed: Boolean = false,
): Parcelable
