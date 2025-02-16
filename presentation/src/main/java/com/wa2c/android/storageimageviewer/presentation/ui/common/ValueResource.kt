package com.wa2c.android.storageimageviewer.presentation.ui.common

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.wa2c.android.storageimageviewer.common.values.Language
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.common.values.UiTheme
import com.wa2c.android.storageimageviewer.presentation.R

object ValueResource {

    fun StorageType.drawableResId(): Int {
        return when (this) {
            StorageType.Device -> R.drawable.ic_storage_device
            StorageType.SD -> R.drawable.ic_storage_sd
            StorageType.USB -> R.drawable.ic_storage_usb
            StorageType.Download -> R.drawable.ic_storage_device
            StorageType.SAF -> R.drawable.ic_storage_saf
        }
    }


    val UiTheme.mode: Int
        get() = when(this) {
            UiTheme.DEFAULT -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            UiTheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            UiTheme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        }

    @Composable
    fun UiTheme.isDark() : Boolean {
        return if (this == UiTheme.DEFAULT) {
            isSystemInDarkTheme()
        } else {
            this == UiTheme.DARK
        }
    }

    val UiTheme.labelRes: Int
        get() = when (this) {
            UiTheme.DEFAULT -> R.string.enum_theme_default
            UiTheme.LIGHT -> R.string.enum_theme_light
            UiTheme.DARK -> R.string.enum_theme_dark
        }

    val Language.labelRes: Int
        @StringRes
        get() = when (this) {
            Language.ENGLISH -> R.string.enum_language_en
            Language.JAPANESE-> R.string.enum_language_ja
        }


}
