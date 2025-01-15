package com.wa2c.android.storageimageviewer.presentation.ui.common

import androidx.compose.material3.SnackbarHostState
import com.wa2c.android.storageimageviewer.common.result.AppException
import com.wa2c.android.storageimageviewer.common.result.AppResult

suspend fun SnackbarHostState.showMessage(result: Result<AppResult>) {
    result.onSuccess {
        val message = when (it) {
            is AppResult.Success -> {
                "Success"
            }
        }
        showSnackbar(message)
    }.onFailure {
        val message = when (it) {
            is AppException -> {
                when (it) {
                    is AppException.StorageNotFoundException -> {
                        "Storage not found: ${it.id}"
                    }
                }
            }
            else -> {
                ""
            }
        }
        showSnackbar(
            message
        )
    }
}
