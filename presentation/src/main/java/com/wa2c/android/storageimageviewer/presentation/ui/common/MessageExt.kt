package com.wa2c.android.storageimageviewer.presentation.ui.common

import android.content.Context
import com.wa2c.android.storageimageviewer.domain.model.AppMessage
import com.wa2c.android.storageimageviewer.presentation.R
import kotlinx.coroutines.CancellationException
import java.io.IOException

object MessageExt {

    fun AppMessage.getMessage(context: Context): String {
        return when (this) {
            is AppMessage.Error.StorageNotFound -> context.getString(R.string.error_storage_not_found)
            is AppMessage.Error.StorageSelectCancelled -> context.getString(R.string.error_storage_select_canceled)
            is AppMessage.Error.StorageEdit -> context.getString(R.string.error_storage_edit_failed) + "\n" + (this.cause?.message ?: "")
            is AppMessage.Error.StorageFileNotFound -> context.getString(R.string.error_storage_file_not_found)
            is AppMessage.Error.Security -> context.getString(R.string.error_storage_access_denied) + "\n" + (this.cause?.message ?: "")
            is AppMessage.Error.IO -> context.getString(R.string.error_storage_io_error) + "\n" + (this.cause?.message ?: "")
            is AppMessage.Error.Cancellation -> context.getString(R.string.error_storage_loading_canceled)
            is AppMessage.Error.Generic -> context.getString(R.string.error_generic) + "\n" + (this.cause?.message ?: "")
            is AppMessage.Success -> ""
        }
    }

    fun Throwable.toAppMessage(): AppMessage {
        return when (this) {
            is SecurityException -> AppMessage.Error.Security(this)
            is IOException -> AppMessage.Error.IO(this)
            is CancellationException -> AppMessage.Error.Generic(this)
            else -> AppMessage.Error.Generic(this)
        }
    }

}
