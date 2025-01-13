package com.wa2c.android.storageimageviewer.domain.repository

import com.wa2c.android.storageimageviewer.common.utils.Log
import com.wa2c.android.storageimageviewer.common.utils.Utils
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.data.db.SafStorageEntity
import com.wa2c.android.storageimageviewer.data.db.StorageDao
import com.wa2c.android.storageimageviewer.data.file.FileHelper
import com.wa2c.android.storageimageviewer.data.mediastore.MediaStoreHelper
import com.wa2c.android.storageimageviewer.domain.IoDispatcher
import com.wa2c.android.storageimageviewer.domain.model.FileModel
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepository @Inject internal constructor(
    private val storageDao: StorageDao,
    private val mediaStoreHelper: MediaStoreHelper,
    private val fileHelper: FileHelper,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) {
    val storageListFlow = storageDao.getList().map {
        withContext(dispatcher) {
            val mediaStoreList = mediaStoreHelper.getStorageList().map { entity ->
                StorageModel(
                    id = entity.id,
                    name = entity.name,
                    uri = UriModel(entity.uri),
                    type = entity.storageType,
                    sortOrder = entity.sortOrder,
                )
            }
            val safList = it.map { entity ->
                StorageModel(
                    id = entity.id,
                    name = entity.name,
                    uri = UriModel(entity.uri),
                    type = StorageType.SAF,
                    sortOrder = entity.sortOrder,
                )
            }
            mediaStoreList + safList
        }
    }

    suspend fun getStorage(
        id: String,
    ): StorageModel? {
        return storageListFlow.firstOrNull()?.firstOrNull { it.id == id }
    }

    suspend fun setStorage(
        model: StorageModel,
    ) {
        withContext(dispatcher) {
            storageDao.insert(
                SafStorageEntity(
                    id = model.id,
                    name = model.name,
                    uri = model.uri.uri,
                    path = "",
                    sortOrder = model.sortOrder,
                    modifiedDate = Utils.currentTime,
                )
            )
        }
    }

    suspend fun getChildList(storage: StorageModel): List<FileModel> {
        return withContext(dispatcher) {
            fileHelper.getChildList(storage.uri.uri).map { child ->
                FileModel(
                    storage = storage,
                    path = child.uri.replaceFirst(storage.uri.uri, "") ,
                    isDirectory = child.isDirectory,
                    name = child.name,
                    mimeType = child.mimeType,
                    size = child.size,
                    dateModified = child.dateModified,
                )
            }
        }
    }

    suspend fun getChildList(file: FileModel): List<FileModel> {
        return withContext(dispatcher) {
            fileHelper.getChildList(file.uriText).map { child ->
                FileModel(
                    storage = file.storage,
                    path = child.uri.replaceFirst(file.uriText, "") ,
                    isDirectory = child.isDirectory,
                    name = child.name,
                    mimeType = child.mimeType,
                    size = child.size,
                    dateModified = child.dateModified,
                )
            }
        }
    }


    /**
     * Move order
     */
    suspend fun moveConnection(fromPosition: Int, toPosition: Int) {
        Log.d("moveConnection: fromPosition=$fromPosition, toPosition=$toPosition")
        withContext(dispatcher) {
            storageDao.move(fromPosition, toPosition)
        }
    }

}
