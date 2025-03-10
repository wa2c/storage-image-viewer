package com.wa2c.android.storageimageviewer.domain.repository

import com.wa2c.android.storageimageviewer.common.utils.Log
import com.wa2c.android.storageimageviewer.common.utils.Utils
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.data.db.SafStorageEntity
import com.wa2c.android.storageimageviewer.data.db.StorageDao
import com.wa2c.android.storageimageviewer.data.file.FileHelper
import com.wa2c.android.storageimageviewer.domain.DefaultDispatcher
import com.wa2c.android.storageimageviewer.domain.model.FileModel
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepository @Inject internal constructor(
    private val storageDao: StorageDao,
    private val fileHelper: FileHelper,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
) {
    /** Storage list */
    val storageListFlow = storageDao.getList().map {
        withContext(dispatcher) {
            it.mapNotNull { entity ->
                val treeUri = fileHelper.getTreeUri(entity.uri)  ?: return@mapNotNull null
                StorageModel(
                    id = entity.id,
                    name = entity.name,
                    uri = UriModel(entity.uri),
                    rootUri = UriModel(treeUri),
                    type = StorageType.fromValue(entity.type),
                    sortOrder = entity.sortOrder,
                )
            }
        }
    }

    suspend fun getStorageFile(
        id: String,
    ): FileModel? {
        return withContext(dispatcher) {
            storageListFlow.firstOrNull()?.firstOrNull { it.id == id }?.let { storage ->
                FileModel(
                    storage = storage,
                    uri = storage.rootUri,
                    isDirectory = true,
                    name = storage.name,
                    mimeType = "",
                    size = 0,
                    dateModified = 0,
                )
            }
        }
    }

    suspend fun saveStorage(
        model: StorageModel,
    ) {
        withContext(dispatcher) {
            storageDao.insert(
                SafStorageEntity(
                    id = if (model.isNew) Utils.generateUUID() else model.id,
                    name = model.name,
                    uri = model.uri.uri,
                    type = model.type.value,
                    sortOrder = if (model.isNew) storageDao.getNextSortOrder() else model.sortOrder,
                    modifiedDate = Utils.currentTime,
                )
            )
        }
    }

    suspend fun deleteStorage(
        model: StorageModel
    ) {
        withContext(dispatcher) {
            storageDao.delete(model.id)
        }
    }

    suspend fun getChildren(file: FileModel): List<FileModel> {
        return withContext(dispatcher) {
            fileHelper.getChildList(file.uri.uri)
                .filter { it.isDirectory || it.mimeType.startsWith("image/")}
                .map { child ->
                    FileModel(
                        storage = file.storage,
                        uri = UriModel(child.uri),
                        isDirectory = child.isDirectory,
                        name = child.name,
                        mimeType = child.mimeType,
                        size = child.size,
                        dateModified = child.dateModified,
                    )
                }
        }
    }

    suspend fun getStorageType(uri: String): StorageType {
        return withContext(dispatcher) {
            fileHelper.getStorageType(uri)
        }
    }

    /**
     * Move order
     */
    suspend fun moveStorageOrder(fromPosition: Int, toPosition: Int) {
        Log.d("moveConnection: fromPosition=$fromPosition, toPosition=$toPosition")
        withContext(dispatcher) {
            storageDao.move(fromPosition, toPosition)
        }
    }

}
