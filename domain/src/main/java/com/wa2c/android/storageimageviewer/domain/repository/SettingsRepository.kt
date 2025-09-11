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

    /** Tree mix folder */
    val treeMixFolder = SettingsModel(_data = dataStore.treeMixFolderFlow, invoke = dataStore::setTreeMixFolder)

    /** Tree view type */
    val treeViewType = SettingsModel(_data = dataStore.treeViewTypeFlow, invoke = dataStore::setTreeViewType)

    /** View show page */
    val viewShowPage = SettingsModel(_data = dataStore.viewShowPageFlow, invoke = dataStore::setViewShowPage)

    /** View volume scroll */
    val viewVolumeScroll = SettingsModel(_data = dataStore.viewVolumeScrollFlow, invoke = dataStore::setViewVolumeScroll)

    /** Tree view overlay */
    val viewShowOverlay = SettingsModel(_data = dataStore.viewShowOverlayFlow, invoke = dataStore::setViewShowOverlay)

}
