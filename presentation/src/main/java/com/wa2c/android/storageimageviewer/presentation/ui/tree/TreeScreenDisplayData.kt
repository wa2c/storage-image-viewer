package com.wa2c.android.storageimageviewer.presentation.ui.tree

import com.wa2c.android.storageimageviewer.common.values.TreeViewType
import com.wa2c.android.storageimageviewer.domain.model.TreeSortModel

data class TreeScreenDisplayData(
    val sort: TreeSortModel = TreeSortModel(),
    val viewType: TreeViewType = TreeViewType.ListSmall,
    val isViewerMode: Boolean = false,
)
