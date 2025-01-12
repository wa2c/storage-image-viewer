package com.wa2c.android.storageimageviewer.presentation.common

import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.presentation.R

object ValueResource {

    fun StorageType.drawableResId(): Int {
        return when (this) {
            StorageType.Device -> R.drawable.ic_storage_device
            StorageType.SD -> R.drawable.ic_storage_sd
            StorageType.External -> R.drawable.ic_storage_usb
            StorageType.SAF -> R.drawable.ic_storage_saf
        }
    }

}
