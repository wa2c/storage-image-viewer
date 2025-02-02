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

    /** Tree view type */
    val treeViewTypeFlow: Flow<TreeViewType> = dataStore.data.map { TreeViewType.findByValueOrDefault(it[PREFKEY_TREE_VIEW_TYPE]) }
    suspend fun setTreeViewType(type: TreeViewType) = dataStore.setValue(PREFKEY_TREE_VIEW_TYPE, type.value)

    /** Tree sort type */
    val treeSortTypeFlow: Flow<TreeSortType> = dataStore.data.map { TreeSortType.findByValueOrDefault(it[PREFKEY_TREE_SORT_TYPE]) }
    suspend fun setTreeSortType(type: TreeSortType) = dataStore.setValue(PREFKEY_TREE_SORT_TYPE, type.value)

    /** Tree sort descending */
    val treeSortDescendingFlow: Flow<Boolean> = dataStore.data.map { it[PREFKEY_TREE_SORT_DESCENDING] ?: false }
    suspend fun setTreeSortDescending(descending: Boolean) = dataStore.setValue(PREFKEY_TREE_SORT_DESCENDING, descending)

    /** Tree sort ignore case */
    val treeSortIgnoreCaseFlow: Flow<Boolean> = dataStore.data.map { it[PREFKEY_TREE_SORT_IGNORE_CASE] ?: false }
    suspend fun setTreeSortIgnoreCase(ignoreCase: Boolean) = dataStore.setValue(PREFKEY_TREE_SORT_IGNORE_CASE, ignoreCase)

    /** Tree sort number */
    val treeSortNumberFlow: Flow<Boolean> = dataStore.data.map { it[PREFKEY_TREE_SORT_NUMBER] ?: false }
    suspend fun setTreeSortNumber(number: Boolean) = dataStore.setValue(PREFKEY_TREE_SORT_NUMBER, number)

    /** Tree mix folder */
    val treeMixFolderFlow: Flow<Boolean> = dataStore.data.map { it[PREFKEY_TREE_SORT_MIX_FOLDER] ?: false }
    suspend fun setTreeMixFolder(mixFolder: Boolean) = dataStore.setValue(PREFKEY_TREE_SORT_MIX_FOLDER, mixFolder)



    companion object {

        private val PREFKEY_TREE_VIEW_TYPE = stringPreferencesKey("prefkey_tree_view_type")
        private val PREFKEY_TREE_SORT_TYPE = stringPreferencesKey("prefkey_tree_sort_type")
        private val PREFKEY_TREE_SORT_DESCENDING = booleanPreferencesKey("prefkey_tree_sort_descending")
        private val PREFKEY_TREE_SORT_IGNORE_CASE = booleanPreferencesKey("prefkey_tree_sort_ignore_case")
        private val PREFKEY_TREE_SORT_NUMBER = booleanPreferencesKey("prefkey_tree_sort_number")
        private val PREFKEY_TREE_SORT_MIX_FOLDER = booleanPreferencesKey("prefkey_tree_mix_folder")

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
