package com.wa2c.android.storageimageviewer.presentation.common

import android.net.Uri
import com.wa2c.android.storageimageviewer.domain.model.UriModel

object Extensions {

    fun Uri.toUriModel(): UriModel {
        return UriModel(
            uri = toString(),
        )
    }

}
