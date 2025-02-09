package com.wa2c.android.storageimageviewer.presentation.ui.common

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import com.wa2c.android.storageimageviewer.common.result.AppException
import com.wa2c.android.storageimageviewer.common.result.AppResult
import com.wa2c.android.storageimageviewer.presentation.R
import kotlinx.coroutines.CancellationException
import java.io.IOException

suspend fun SnackbarHostState.showMessage(context: Context, result: Result<AppResult>) {
    result.onSuccess {
        // Do Nothing
    }.onFailure {
        val message = when (it) {
            is AppException -> {
                when (it) {
                    is AppException.StorageNotFoundException -> {
                        context.getString(R.string.error_storage_not_found)
                    }
                    is AppException.StorageSelectCancelledException -> {
                        context.getString(R.string.error_storage_select_canceled)
                    }
                    is AppException.StorageEditException -> {
                        context.getString(R.string.error_storage_edit_failed)
                    }
                    is AppException.StorageFileNotFoundException -> {
                        context.getString(R.string.error_storage_file_not_found)
                    }
                }
            }
            is SecurityException -> {
                context.getString(R.string.error_storage_access_denied)
            }
            is IOException -> {
                context.getString(R.string.error_storage_io_error)
            }
            is CancellationException -> {
                context.getString(R.string.error_storage_loading_canceled)
            }
            else -> {
                context.getString(R.string.error_generic)
            }
        }
        showSnackbar(message)
    }
}
