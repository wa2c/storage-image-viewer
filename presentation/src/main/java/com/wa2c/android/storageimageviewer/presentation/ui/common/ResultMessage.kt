package com.wa2c.android.storageimageviewer.presentation.ui.common

import androidx.compose.material3.SnackbarHostState
import com.wa2c.android.storageimageviewer.common.result.AppException
import com.wa2c.android.storageimageviewer.common.result.AppResult
import kotlinx.coroutines.CancellationException

suspend fun SnackbarHostState.showMessage(result: Result<AppResult>) {
    result.onSuccess {
//        val message = when (it) {
//            is AppResult.Success -> {
//                null
//            }
//        }
//        message?.let { showSnackbar(it) }
    }.onFailure {
        val message = when (it) {
            is AppException -> {
                when (it) {
                    is AppException.StorageNotFoundException -> {
                        "Storage not found: ${it.id}"
                    }

                    is AppException.StorageSelectCancelledException -> {
                        "Storage select cancelled"
                    }
                }
            }
            is CancellationException -> {
                "Loading cancelled"
            }
            else -> {
                it.message
            }
        } ?: "Error"
        showSnackbar(message)
    }
}
