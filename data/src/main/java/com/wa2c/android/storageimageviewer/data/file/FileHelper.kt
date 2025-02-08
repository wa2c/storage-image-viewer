package com.wa2c.android.storageimageviewer.data.file

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.provider.DocumentsContract
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.wa2c.android.storageimageviewer.common.utils.Log
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.common.values.StorageType.Device
import com.wa2c.android.storageimageviewer.common.values.StorageType.Download
import com.wa2c.android.storageimageviewer.common.values.StorageType.SAF
import com.wa2c.android.storageimageviewer.common.values.StorageType.SD
import com.wa2c.android.storageimageviewer.common.values.StorageType.USB
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

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

    @SuppressLint("DiscouragedPrivateApi")
    private fun StorageVolume.mountedPath(): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                directory ?.canonicalPath
            } else {
                val getPath = StorageVolume::class.java.getDeclaredMethod("getPath")
                (getPath.invoke(this) as? String)
            }
        } catch (e: Exception) {
            Log.e(e)
            null
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

    private fun getTreeUri(storageUri: Uri): Uri? {
        return DocumentFile.fromTreeUri(context, storageUri)?.uri
    }

    fun getStorageTreeUri(uriText: String): String? {
        return getTreeUri(uriText.toUri())?.toString()
    }

    suspend fun getStorageType(uriText: String): StorageType {
        return withContext(Dispatchers.IO) {
            val uri = uriText.toUri()
            if (uri.authority == ANDROID_STORAGE_AUTHORITY) {
                val volumeName = DocumentsContract.getTreeDocumentId(uri)
                    .split(':').firstOrNull() ?: return@withContext SAF
                if (volumeName == ANDROID_STORAGE_PRIMARY) {
                    Device
                } else {
                    val storageVolume = getStorageList()
                        .firstOrNull { it.uuid == volumeName } ?: return@withContext SAF
                    if (storageVolume.isPrimary) return@withContext Device

                    // by description
                    val description = storageVolume.getDescription(context)
                    if (description.contains("usb",true)) USB
                    else if (description.contains("sd", true)) SD
                    // by path
                    val path = storageVolume.mountedPath() ?: return@withContext SAF
                    if (path.startsWith("/storage")) SD
                    else if (path.startsWith("/mnt")) USB
                    else SAF
                }
            } else if (uri.authority == DOWNLOAD_STORAGE_AUTHORITY) {
                Download
            } else {
                SAF
            }
        }
    }

    /**
     * Get tree files
     */
    suspend fun getChildList(uriText: String): List<FileEntity> {
        return withContext(Dispatchers.IO) {
            val treeUri = getTreeUri(uriText.toUri()) ?: return@withContext emptyList()
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

    companion object {
        private const val ANDROID_STORAGE_PRIMARY = "primary"
        private const val ANDROID_STORAGE_AUTHORITY = "com.android.externalstorage.documents"
        private const val DOWNLOAD_STORAGE_AUTHORITY = "com.android.providers.downloads.documents"

        private fun Cursor.getStringValue(name: String): String? {
            return getStringOrNull(getColumnIndex(name))
        }

        private fun Cursor.getLongValue(name: String): Long? {
            return getLongOrNull(getColumnIndex(name))
        }

    }

}
