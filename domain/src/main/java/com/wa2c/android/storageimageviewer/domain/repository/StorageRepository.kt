package com.wa2c.android.storageimageviewer.domain.repository

import com.wa2c.android.storageimageviewer.common.value.StorageType
import com.wa2c.android.storageimageviewer.data.db.SafStorageEntity
import com.wa2c.android.storageimageviewer.data.db.StorageDao
import com.wa2c.android.storageimageviewer.domain.IoDispatcher
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepository @Inject internal constructor(
    private val storageDao: StorageDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) {

    val storageListFlow = storageDao.getList().map {
        it.map { entity ->
            StorageModel(
                id = entity.id,
                name = entity.name,
                uri = UriModel(entity.uri),
                type = StorageType.SAF,
                sortOrder = entity.sortOrder,
            )
        }
    }

    suspend fun setStorage(
        model: StorageModel,
    ) {
        withContext(dispatcher) {
            storageDao.insert(
                SafStorageEntity(
                    id = model.id,
                    name = model.name,
                    uri = model.uri.toString(),
                    sortOrder = model.sortOrder,
                    modifiedDate = System.currentTimeMillis(),
                )
            )
        }
    }

}
