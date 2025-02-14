package com.wa2c.android.storageimageviewer.presentation.ui.tree.model

import com.wa2c.android.storageimageviewer.common.values.TreeSortType
import com.wa2c.android.storageimageviewer.common.values.TreeViewType

data class TreeScreenOption(
    val sort: TreeSortModel = TreeSortModel(),
    val treeOption: TreeScreenTreeOption = TreeScreenTreeOption(),
    val viewerOption: TreeScreenViewerOption = TreeScreenViewerOption(),
    val isViewerMode: Boolean = false,
)

data class TreeSortModel(
    val type: TreeSortType = TreeSortType.Name,
    val isDescending: Boolean = false,
    val isIgnoreCase: Boolean = false,
    val isNumberSort: Boolean = false,
    val isFolderMixed: Boolean = false,
)

data class TreeScreenTreeOption(
    val viewType: TreeViewType = TreeViewType.ListSmall,
)

data class TreeScreenViewerOption(
    val showOverlay: Boolean = false,
    val showPage: Boolean = true,
    val volumeScroll: Boolean = false,
)
