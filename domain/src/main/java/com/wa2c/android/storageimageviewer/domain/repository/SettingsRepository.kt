package com.wa2c.android.storageimageviewer.domain.repository

import com.wa2c.android.storageimageviewer.common.values.UiTheme
import com.wa2c.android.storageimageviewer.data.kvs.AppPreferencesDataStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject internal constructor(
    private val appPreferences: AppPreferencesDataStore,
) {

    /** UI Theme */
    val uiThemeFlow = appPreferences.uiThemeFlow

    /** UI Theme */
    suspend fun setUiTheme(value: UiTheme) = appPreferences.setUiTheme(value)


}
