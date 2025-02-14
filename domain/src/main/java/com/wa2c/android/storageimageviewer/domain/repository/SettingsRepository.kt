package com.wa2c.android.storageimageviewer.domain.repository

import com.wa2c.android.storageimageviewer.common.values.TreeSortType
import com.wa2c.android.storageimageviewer.common.values.TreeViewType
import com.wa2c.android.storageimageviewer.common.values.UiTheme
import com.wa2c.android.storageimageviewer.data.kvs.AppPreferencesDataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject internal constructor(
    private val dataStore: AppPreferencesDataStore,
) {

    // Global

    /** UI Theme */
    val uiThemeFlow = dataStore.uiThemeFlow

    /** UI Theme */
    suspend fun setUiTheme(value: UiTheme) = dataStore.setUiTheme(value)

    // Tree

    /** Tree sort type */
    val treeSortTypeFlow: Flow<TreeSortType> = dataStore.treeSortTypeFlow
    suspend fun setTreeSortType(type: TreeSortType) = dataStore.setTreeSortType(type)

    /** Tree sort descending */
    val treeSortDescendingFlow: Flow<Boolean> = dataStore.treeSortDescendingFlow
    suspend fun setTreeSortDescending(value: Boolean) = dataStore.setTreeSortDescending(value)

    /** Tree sort ignore case */
    val treeSortIgnoreCaseFlow: Flow<Boolean> = dataStore.treeSortIgnoreCaseFlow
    suspend fun setTreeSortIgnoreCase(value: Boolean) = dataStore.setTreeSortIgnoreCase(value)

    /** Tree sort number */
    val treeSortNumberFlow: Flow<Boolean> = dataStore.treeSortNumberFlow
    suspend fun setTreeSortNumber(value: Boolean) = dataStore.setTreeSortNumber(value)

    /** Tree mix folder */
    val treeMixFolderFlow: Flow<Boolean> = dataStore.treeMixFolderFlow
    suspend fun setTreeMixFolder(value: Boolean) = dataStore.setTreeMixFolder(value)

    /** Tree view type */
    val treeViewTypeFlow = dataStore.treeViewTypeFlow
    suspend fun setTreeViewType(type: TreeViewType) = dataStore.setTreeViewType(type)

    /** View show page */
    val viewShowPageFlow: Flow<Boolean> = dataStore.viewShowPageFlow
    suspend fun setViewShowPageFlow(value: Boolean) = dataStore.setViewShowPageFlow(value)

    /** View volume scroll */
    val viewVolumeScrollFlow: Flow<Boolean> = dataStore.viewVolumeScrollFlow
    suspend fun setViewVolumeScrollFlow(value: Boolean) = dataStore.setViewVolumeScrollFlow(value)

    /** Tree view overlay */
    val viewShowOverlayFlow: Flow<Boolean> = dataStore.viewShowOverlayFlow
    suspend fun setViewShowOverlayFlow(value: Boolean) = dataStore.setViewShowOverlayFlow(value)






}
