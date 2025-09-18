package com.wa2c.android.storageimageviewer.domain.repository

import com.wa2c.android.storageimageviewer.data.kvs.AppPreferencesDataStore
import com.wa2c.android.storageimageviewer.domain.model.SettingsModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject internal constructor(
    private val dataStore: AppPreferencesDataStore,
) {

    // Global

    /** UI Theme */
    val uiTheme = SettingsModel(_data = dataStore.uiThemeFlow, invoke = dataStore::setUiTheme)

    // Tree

    /** Tree sort type */
    val treeSortType = SettingsModel(_data = dataStore.treeSortTypeFlow, invoke = dataStore::setTreeSortType)

    /** Tree sort descending */
    val treeSortDescending = SettingsModel(_data = dataStore.treeSortDescendingFlow, invoke = dataStore::setTreeSortDescending)

    /** Tree sort ignore case */
    val treeSortIgnoreCase = SettingsModel(_data = dataStore.treeSortIgnoreCaseFlow, invoke = dataStore::setTreeSortIgnoreCase)

    /** Tree sort number */
    val treeSortNumber = SettingsModel(_data = dataStore.treeSortNumberFlow, invoke = dataStore::setTreeSortNumber)

    /** Tree folder sort type */
    val treeFolderSortType = SettingsModel(_data = dataStore.treeFolderSortTypeFlow, invoke = dataStore::setTreeFolderSortType)

    /** Tree folder sort descending */
    val treeFolderSortDescending = SettingsModel(_data = dataStore.treeFolderSortDescendingFlow, invoke = dataStore::setTreeFolderSortDescending)

    /** Tree folder sort ignore case */
    val treeFolderSortIgnoreCase = SettingsModel(_data = dataStore.treeFolderSortIgnoreCaseFlow, invoke = dataStore::setTreeFolderSortIgnoreCase)

    /** Tree folder sort number */
    val treeFolderSortNumber = SettingsModel(_data = dataStore.treeFolderSortNumberFlow, invoke = dataStore::setTreeFolderSortNumber)

    /** Tree mix folder */
    val treeMixFolder = SettingsModel(_data = dataStore.treeMixFolderFlow, invoke = dataStore::setTreeMixFolder)

    /** Tree view type */
    val treeViewType = SettingsModel(_data = dataStore.treeViewTypeFlow, invoke = dataStore::setTreeViewType)

    /** View show page */
    val viewShowPage = SettingsModel(_data = dataStore.viewShowPageFlow, invoke = dataStore::setViewShowPage)

    /** View show animation */
    val viewShowAnimation = SettingsModel(_data = dataStore.viewShowAnimationFlow, invoke = dataStore::setViewShowAnimation)

    /** View volume scroll */
    val viewVolumeScroll = SettingsModel(_data = dataStore.viewVolumeScrollFlow, invoke = dataStore::setViewVolumeScroll)

    /** Tree view overlay */
    val viewShowOverlay = SettingsModel(_data = dataStore.viewShowOverlayFlow, invoke = dataStore::setViewShowOverlay)

}
