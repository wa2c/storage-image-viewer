package com.wa2c.android.storageimageviewer.common.result

sealed class AppException(e: Exception?): Exception(e) {
    class StorageNotFoundException(val id: String?): AppException(null)
    class StorageSelectCancelledException: AppException(null)
}
