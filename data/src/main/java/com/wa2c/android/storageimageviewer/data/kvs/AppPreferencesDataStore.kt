package com.wa2c.android.storageimageviewer.data.kvs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.wa2c.android.storageimageviewer.common.values.TreeSortType
import com.wa2c.android.storageimageviewer.common.values.TreeViewType
import com.wa2c.android.storageimageviewer.common.values.UiTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Preference repository
 */
@Singleton
class AppPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /** DataStore */
    private val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("App") },
        migrations = listOf(SharedPreferencesMigration(context, "App"))
    )

    /** UI Theme */
    val uiThemeFlow: Flow<UiTheme> =  dataStore.data.map { UiTheme.findByKeyOrDefault(it[PREFKEY_UI_THEME]) }

    /** UI Theme */
    suspend fun setUiTheme(value: UiTheme) = dataStore.setValue(PREFKEY_UI_THEME, value.key)

    /** Tree view type */
    val treeViewTypeFlow: Flow<TreeViewType> = dataStore.data.map { TreeViewType.findByValueOrDefault(it[PREFKEY_TREE_VIEW_TYPE]) }
    suspend fun setTreeViewType(type: TreeViewType) = dataStore.setValue(PREFKEY_TREE_VIEW_TYPE, type.value)

    /** Tree sort type */
    val treeSortTypeFlow: Flow<TreeSortType> = dataStore.data.map { TreeSortType.findByValueOrDefault(it[PREFKEY_TREE_SORT_TYPE]) }
    suspend fun setTreeSortType(type: TreeSortType) = dataStore.setValue(PREFKEY_TREE_SORT_TYPE, type.value)

    /** Tree sort descending */
    val treeSortDescendingFlow: Flow<Boolean> = dataStore.data.map { it[PREFKEY_TREE_SORT_DESCENDING] ?: false }
    suspend fun setTreeSortDescending(value: Boolean) = dataStore.setValue(PREFKEY_TREE_SORT_DESCENDING, value)

    /** Tree sort ignore case */
    val treeSortIgnoreCaseFlow: Flow<Boolean> = dataStore.data.map { it[PREFKEY_TREE_SORT_IGNORE_CASE] ?: false }
    suspend fun setTreeSortIgnoreCase(value: Boolean) = dataStore.setValue(PREFKEY_TREE_SORT_IGNORE_CASE, value)

    /** Tree sort number */
    val treeSortNumberFlow: Flow<Boolean> = dataStore.data.map { it[PREFKEY_TREE_SORT_NUMBER] ?: false }
    suspend fun setTreeSortNumber(value: Boolean) = dataStore.setValue(PREFKEY_TREE_SORT_NUMBER, value)

    /** Tree mix folder */
    val treeMixFolderFlow: Flow<Boolean> = dataStore.data.map { it[PREFKEY_TREE_SORT_MIX_FOLDER] ?: false }
    suspend fun setTreeMixFolder(value: Boolean) = dataStore.setValue(PREFKEY_TREE_SORT_MIX_FOLDER, value)


    /** View show page */
    val viewShowPageFlow: Flow<Boolean> = dataStore.data.map { it[PREFKEY_VIEW_SHOW_PAGE] ?: true }
    suspend fun setViewShowPageFlow(value: Boolean) = dataStore.setValue(PREFKEY_VIEW_SHOW_PAGE, value)

    /** View show overlay */
    val viewShowOverlayFlow: Flow<Boolean> = dataStore.data.map { it[PREFKEY_VIEW_SHOW_OVERLAY] ?: false }
    suspend fun setViewShowOverlayFlow(value: Boolean) = dataStore.setValue(PREFKEY_VIEW_SHOW_OVERLAY, value)

    companion object {

        private val PREFKEY_UI_THEME = stringPreferencesKey("prefkey_ui_theme")

        private val PREFKEY_TREE_VIEW_TYPE = stringPreferencesKey("prefkey_tree_view_type")
        private val PREFKEY_TREE_SORT_TYPE = stringPreferencesKey("prefkey_tree_sort_type")
        private val PREFKEY_TREE_SORT_DESCENDING = booleanPreferencesKey("prefkey_tree_sort_descending")
        private val PREFKEY_TREE_SORT_IGNORE_CASE = booleanPreferencesKey("prefkey_tree_sort_ignore_case")
        private val PREFKEY_TREE_SORT_NUMBER = booleanPreferencesKey("prefkey_tree_sort_number")
        private val PREFKEY_TREE_SORT_MIX_FOLDER = booleanPreferencesKey("prefkey_tree_mix_folder")
        private val PREFKEY_VIEW_SHOW_PAGE = booleanPreferencesKey("prefkey_view_show_page")
        private val PREFKEY_VIEW_SHOW_OVERLAY = booleanPreferencesKey("prefkey_view_show_overlay")

        /**
         * Get first value.
         */
        fun <T> Flow<T>.getFirst(): T {
            return runBlocking { this@getFirst.first() }
        }

        private suspend fun <T> DataStore<Preferences>.getValue(key: Preferences.Key<T>): T? {
            return data.map { preferences -> preferences[key] }.firstOrNull()
        }

        private suspend fun <T> DataStore<Preferences>.setValue(key: Preferences.Key<T>, value: T?) {
            edit { edit -> if (value != null) edit[key] = value else edit.remove(key)  }
        }

    }

}
