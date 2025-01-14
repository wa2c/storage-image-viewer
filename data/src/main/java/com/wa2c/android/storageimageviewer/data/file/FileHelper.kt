package com.wa2c.android.storageimageviewer.data.file

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.FileUtils
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.core.provider.DocumentsContractCompat
import androidx.documentfile.provider.DocumentFile
import com.wa2c.android.storageimageviewer.common.utils.Log
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.common.values.UriType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class FileHelper @Inject internal constructor(
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


    private fun getDocumentFile(uriText: String): DocumentFile? {
        val uriType = UriType.fromUriText(uriText) ?: return null
        val uri = uriText.toUri()
        return when (uriType) {
            UriType.Content -> {
                if (DocumentsContract.isTreeUri(uri)) {
                    DocumentFile.fromTreeUri(context, uri)
                } else {
                    DocumentFile.fromSingleUri(context, uri)
                }
            }
            UriType.File -> {
                DocumentFile.fromFile(uri.toFile())
            }
        }
    }


    private fun DocumentFile.toEntity(): FileEntity? {
        return FileEntity(
            uri = uri.toString(),
            name = name ?: return null,
            isDirectory = isDirectory,
            mimeType = type ?: "",
            size = length(),
            dateModified = lastModified()
        )
    }


    /**
     * Get tree files
     */
    fun getChildList(uriText: String): List<FileEntity> {
        val uriType = UriType.fromUriText(uriText) ?: return emptyList()
        val uri = uriText.toUri()
        val directory = when (uriType) {
            UriType.Content -> DocumentFile.fromTreeUri(context, uri) ?: return emptyList()
            UriType.File -> DocumentFile.fromFile(uri.toFile())
        }
        return directory.listFiles().mapNotNull { file ->
            if (!file.isDirectory && file.type?.startsWith("image/") != true) return@mapNotNull null
            val a = file.parentFile
            Log.d(a)
            file.toEntity()
        }
    }

    /**
     * Get parent file
     * NOTE: DocumentFile.parentFile is not working.
     */
    fun getParent(uriText: String): FileEntity? {
        val uriType = UriType.fromUriText(uriText) ?: return null
        when (uriType) {
            UriType.Content -> {
                val normalizedUri = uriText.removeSuffix(SEPARATOR)
                val lastSeparatorIndex = normalizedUri.lastIndexOf(SEPARATOR)
                val lastDelimiterIndex = normalizedUri.lastIndexOf('/')

                val uri = normalizedUri.substring(0, max(lastSeparatorIndex, lastDelimiterIndex)).toUri()
                val directory = DocumentFile.fromTreeUri(context, uri)?.takeIf { it.isDirectory } ?: return null
                return directory.toEntity()
            }
            UriType.File -> {
                val parentFile = uriText.toUri().toFile().parentFile ?: return null
                val directory = DocumentFile.fromFile(parentFile).takeIf { it.isDirectory } ?: return null
                return directory.toEntity()
            }
        }
    }

    companion object {
        private const val SEPARATOR = "%2F"
    }

}
