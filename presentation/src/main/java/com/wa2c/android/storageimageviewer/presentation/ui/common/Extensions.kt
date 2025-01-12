package com.wa2c.android.storageimageviewer.presentation.ui.common

import android.net.Uri
import androidx.core.net.toUri
import com.wa2c.android.storageimageviewer.domain.model.UriModel

object Extensions {

    fun Uri.toUriModel(): UriModel {
        return UriModel(
            uri = toString(),
        )
    }

    fun UriModel.toUri(): Uri {
        return uri.toUri()
    }

}
