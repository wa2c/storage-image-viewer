package com.wa2c.android.storageimageviewer.presentation.ui.tree.model

import com.wa2c.android.storageimageviewer.common.values.TreeViewType
import com.wa2c.android.storageimageviewer.domain.model.TreeSortModel

data class TreeScreenDisplayData(
    val sort: TreeSortModel = TreeSortModel(),
    val viewType: TreeViewType = TreeViewType.ListSmall,
    val showOverlay: Boolean = false,
    val showPage: Boolean = true,
    val isViewerMode: Boolean = false,
)
