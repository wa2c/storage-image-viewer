package com.wa2c.android.storageimageviewer.domain.model

/**
 * App Messages
 */
sealed class AppMessage {
    data object Success : AppMessage()
    sealed class Error(cause: Throwable?): AppMessage() {
        data class Generic(val cause: Throwable? = null): Error(cause)
        data class StorageNotFound(val cause: Throwable? = null): Error(cause)
        data class StorageSelectCancelled(val cause: Throwable? = null): Error(cause)
        data class StorageEdit(val cause: Throwable? = null): Error(cause)
        data class StorageFileNotFound(val cause: Throwable? = null): Error(cause)
        data class Security(val cause: Throwable? = null): Error(cause)
        data class IO(val cause: Throwable? = null): Error(cause)
        data class Cancellation(val cause: Throwable? = null): Error(cause)
    }
}
