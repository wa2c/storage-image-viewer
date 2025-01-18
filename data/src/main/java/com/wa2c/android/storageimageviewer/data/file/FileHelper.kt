package com.wa2c.android.storageimageviewer.data.file

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.wa2c.android.storageimageviewer.common.utils.Log
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.common.values.UriType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    suspend fun getChildList(uriText: String): List<FileEntity> {
        return withContext(Dispatchers.IO) {
            val uriType = UriType.fromUriText(uriText) ?: return@withContext emptyList()
            val uri = uriText.toUri()
            when (uriType) {
                UriType.Content -> {
                    // Content provider
                    val treeUri = DocumentFile.fromTreeUri(context, uri)?.uri ?: return@withContext emptyList()
                    val childUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, DocumentsContract.getDocumentId(treeUri))
                    context.contentResolver.query(childUri, null, null, null, null)?.use { cursor ->
                        generateSequence { if (cursor.moveToNext()) cursor else null }.mapNotNull {
                            val mimeType = cursor.getStringValue(DocumentsContract.Document.COLUMN_MIME_TYPE) ?: return@mapNotNull null
                            val documentId = cursor.getStringValue(DocumentsContract.Document.COLUMN_DOCUMENT_ID) ?: return@mapNotNull null
                            val fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
                            FileEntity(
                                uri = fileUri.toString(),
                                name = cursor.getStringValue(DocumentsContract.Document.COLUMN_DISPLAY_NAME) ?: "",
                                isDirectory = mimeType == DocumentsContract.Document.MIME_TYPE_DIR,
                                mimeType = mimeType,
                                size = cursor.getLongValue(DocumentsContract.Document.COLUMN_SIZE) ?: 0,
                                dateModified = cursor.getLongValue(DocumentsContract.Document.COLUMN_LAST_MODIFIED) ?: 0,
                            )
                        }.toList()
                    }
                }
                UriType.File -> {
                    // Local file
                    uri.toFile().listFiles()?.map { file ->
                        FileEntity(
                            uri = file.toUri().toString(),
                            name = file.name,
                            isDirectory = file.isDirectory,
                            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension) ?: "",
                            size = file.length(),
                            dateModified = file.lastModified(),
                        )
                    } ?: emptyList()
                }
            } ?: emptyList()
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
                val lastSeparatorIndex = normalizedUri.lastIndexOf(SEPARATOR) + SEPARATOR.length
                val lastDelimiterIndex = normalizedUri.lastIndexOf('/')

                val uri = normalizedUri.substring(0, max(lastSeparatorIndex, lastDelimiterIndex + 1)).toUri()
                val directory = DocumentFile.fromTreeUri(context, uri) ?: return null
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

        private fun Cursor.getStringValue(name: String): String? {
            return getStringOrNull(getColumnIndex(name))
        }

        private fun Cursor.getLongValue(name: String): Long? {
            return getLongOrNull(getColumnIndex(name))
        }

    }

}
