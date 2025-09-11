package com.wa2c.android.storageimageviewer.domain.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first

data class SettingsModel<T>(
    private val _data: Flow<T>,
    val invoke: suspend (T) -> Unit,
) {
    val data: Flow<T> = _data.distinctUntilChanged()

    suspend fun get(): T {
        return data.first()
    }

    suspend fun set(value: T) {
        invoke(value)
    }
}
