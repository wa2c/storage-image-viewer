package com.wa2c.android.storageimageviewer.presentation.ui.tree.model

import com.wa2c.android.storageimageviewer.common.values.TreeFolderOrder
import com.wa2c.android.storageimageviewer.common.values.TreeSortType
import com.wa2c.android.storageimageviewer.common.values.TreeViewType

data class TreeScreenOption(
    val fileSort: TreeSortModel = TreeSortModel(),
    val folderSort: TreeSortModel = TreeSortModel(),
    val treeOption: TreeScreenTreeOption = TreeScreenTreeOption(),
    val viewerOption: TreeScreenViewerOption = TreeScreenViewerOption(),
    val isViewerMode: Boolean = false,
)

data class TreeSortModel(
    val type: TreeSortType = TreeSortType.Name,
    val isDescending: Boolean = false,
    val isIgnoreCase: Boolean = false,
    val isNumberSort: Boolean = false,
)

data class TreeScreenTreeOption(
    val folderOrder: TreeFolderOrder = TreeFolderOrder.DEFAULT,
    val viewType: TreeViewType = TreeViewType.DEFAULT,
)

data class TreeScreenViewerOption(
    val showPage: Boolean = true,
    val showAnimation: Boolean = true,
    val volumeScroll: Boolean = false,
    val showOverlay: Boolean = false,
)
