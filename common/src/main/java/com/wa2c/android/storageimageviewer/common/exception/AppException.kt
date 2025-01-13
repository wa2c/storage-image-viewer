package com.wa2c.android.storageimageviewer.common.exception

sealed class AppException(e: Exception?): Exception(e) {
    class StorageNotFoundException(val id: String?): AppException(null)

}
