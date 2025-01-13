package com.wa2c.android.storageimageviewer.data.mediastore

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import androidx.core.net.toUri
import com.wa2c.android.storageimageviewer.common.utils.Log
import com.wa2c.android.storageimageviewer.common.values.StorageType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.text.contains
import kotlin.text.lowercase

@Singleton
class MediaStoreHelper @Inject internal constructor(
    @ApplicationContext private val context: Context,
) {
    private val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

    /**
     * Get root storage item list.
     */
    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    fun getStorageList(): List<StorageEntity> {
        return try {
            val storageMax = storageManager.storageVolumes.size
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    // Version >= Android 11
                    storageManager.storageVolumes.mapIndexedNotNull { index, storageVolume ->
                        val uri = storageVolume.directory?.toUri()?.toString() ?: return@mapIndexedNotNull null
                        StorageEntity(
                            id = storageVolume.uuid ?: uri,
                            name = storageVolume.mediaStoreVolumeName ?: storageVolume.getDescription(context),
                            uri = uri,
                            path = "",
                            sortOrder = storageMax - index, // negative value
                            storageType = storageVolume.getStorageType()
                        )
                    }
                }
                else -> {
                    val getPath = StorageVolume::class.java.getDeclaredMethod("getPath")
                    storageManager.storageVolumes.mapIndexedNotNull { index, storageVolume ->
                        val uri = (getPath.invoke(storageVolume) as? String)?.let { File(it).toUri().toString() } ?: return@mapIndexedNotNull null
                        StorageEntity(
                            id = storageVolume.uuid ?: uri,
                            name = storageVolume.getDescription(context),
                            uri = uri,
                            path = "",
                            sortOrder = storageMax - index, // negative value
                            storageType = storageVolume.getStorageType()
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(e)
            null
        } ?: emptyList()
    }

    private fun StorageVolume.getStorageType(): StorageType {
        val description = getDescription(context).lowercase()
        return when {
            isPrimary -> StorageType.Device
            !isRemovable -> StorageType.Device
            isEmulated -> StorageType.External
            description.contains("sd") -> StorageType.SD
            description.contains("usb") -> StorageType.External
            else -> StorageType.External
        }
    }


}
