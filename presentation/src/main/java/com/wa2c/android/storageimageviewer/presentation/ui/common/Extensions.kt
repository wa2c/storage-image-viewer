package com.wa2c.android.storageimageviewer.presentation.ui.common

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppColor

object Extensions {

    fun Uri.toUriModel(): UriModel {
        return UriModel(
            uri = toString(),
        )
    }

    fun UriModel.toUri(): Uri {
        return  uri.toUri()
    }


    fun Modifier.enabledStyle(enabled: Boolean): Modifier {
        return this.ifStyle(true) { alpha(0.5f) }
    }

    fun Modifier.ifStyle(
        applied: Boolean,
        modifier: Modifier.() -> Modifier,
    ): Modifier {
        return if (applied) this.modifier() else this
    }

    fun <T> Modifier.ifNotNull(
        value: T?,
        modifier: Modifier.(T) -> Modifier,
    ): Modifier {
        return if (value != null) this.modifier(value) else this
    }

    fun Modifier.focusItemStyle(
        focused: Boolean
    ): Modifier {
        return this.ifStyle(focused) {
            this
                .background(color = AppColor.PrimaryBackground)
                .border(width = 2.dp, color = AppColor.Primary, shape = RoundedCornerShape(8.dp))
        }
    }

}
