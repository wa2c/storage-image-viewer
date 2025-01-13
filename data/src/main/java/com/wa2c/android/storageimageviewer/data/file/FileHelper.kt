package com.wa2c.android.storageimageviewer.data.file

import android.content.ContentResolver
import android.content.Context
import android.provider.DocumentsContract
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileHelper @Inject internal constructor(
    @ApplicationContext private val context: Context,
) {
    /**
     * Get tree files
     */
    fun getChildList(uriText: String): List<FileEntity> {
        val uri = uriText.toUri()
        val directory = if (uri.scheme == ContentResolver.SCHEME_FILE) {
            DocumentFile.fromFile(uri.toFile())
        } else if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            if (DocumentsContract.isTreeUri(uri)) {
                DocumentFile.fromTreeUri(context, uri)
            } else {
                return emptyList()
            }
        } else {
            return emptyList()
        } ?: return emptyList()
        return directory.listFiles().mapNotNull { file ->
            if (!file.isDirectory && file.type?.startsWith("image/") != true) return@mapNotNull null
            FileEntity(
                uri = file.uri.toString(),
                name = file.name ?: return@mapNotNull null,
                isDirectory = file.isDirectory,
                mimeType = file.type ?: "",
                size = file.length(),
                dateModified = file.lastModified()
            )
        }
    }

}
