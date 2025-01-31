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
import com.wa2c.android.storageimageviewer.common.values.StorageType.Device
import com.wa2c.android.storageimageviewer.common.values.StorageType.Download
import com.wa2c.android.storageimageviewer.common.values.StorageType.External
import com.wa2c.android.storageimageviewer.common.values.StorageType.SAF
import com.wa2c.android.storageimageviewer.common.values.StorageType.SD
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
     * Get root storage volume list
     */
    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    private fun getStorageList(): List<StorageVolume> {
        return try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    storageManager.storageVolumes
                }
                else -> {
                    storageManager.storageVolumes
                }
            }
        } catch (e: Exception) {
            Log.e(e)
            null
        } ?: emptyList()
    }

    fun getStorageType(uriText: String): StorageType {
        val uri = uriText.toUri()
        return if (uri.authority == ANDROID_STORAGE_AUTHORITY) {
            val volumeName = DocumentsContract.getTreeDocumentId(uri).split(':').firstOrNull()
            if (volumeName == ANDROID_STORAGE_PRIMARY) {
                Device
            } else if (volumeName == DOWNLOAD_STORAGE_AUTHORITY) {
                Download
            } else {
                val storageVolume = getStorageList().firstOrNull { it.uuid == volumeName } ?: return SD
                val description = storageVolume.getDescription(context)
                if (description.contains("usb")) External
                else if (description.contains("sd")) SD
                else External
            }
        } else {
            SAF
        }
    }

    private fun DocumentFile.toEntity(): FileEntity? {
        return FileEntity(
            uri = uri.toString(),
            name = name ?: return null,
            isDirectory = isDirectory,
            mimeType = type ?: "",
            size = length(),
            dateModified = lastModified(),
        )
    }


    /**
     * Get tree files
     */
    suspend fun getChildList(uriText: String): List<FileEntity> {
        return withContext(Dispatchers.IO) {
            val uri = uriText.toUri()
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
            } ?: emptyList()
        }
    }

    /**
     * Get parent file
     * NOTE: DocumentFile.parentFile is not working.
     */
    fun getParent(uriText: String): FileEntity? {
        val normalizedUri = uriText.removeSuffix(SEPARATOR)
        val lastSeparatorIndex = normalizedUri.lastIndexOf(SEPARATOR) + SEPARATOR.length
        val lastDelimiterIndex = normalizedUri.lastIndexOf('/')

        val uri = normalizedUri.substring(0, max(lastSeparatorIndex, lastDelimiterIndex + 1)).toUri()
        val directory = DocumentFile.fromTreeUri(context, uri) ?: return null
        return directory.toEntity()
    }

    companion object {
        private const val SEPARATOR = "%2F"

        private const val ANDROID_STORAGE_AUTHORITY = "com.android.externalstorage.documents"
        private const val ANDROID_STORAGE_PRIMARY = "primary"
        private const val DOWNLOAD_STORAGE_AUTHORITY = "com.android.providers.downloads.documents"

        private fun Cursor.getStringValue(name: String): String? {
            return getStringOrNull(getColumnIndex(name))
        }

        private fun Cursor.getLongValue(name: String): Long? {
            return getLongOrNull(getColumnIndex(name))
        }

    }

}
