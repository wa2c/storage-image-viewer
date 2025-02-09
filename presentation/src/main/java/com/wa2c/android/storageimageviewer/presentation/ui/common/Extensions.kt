package com.wa2c.android.storageimageviewer.presentation.ui.common

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.wa2c.android.storageimageviewer.common.utils.Log
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppColor

object Extensions {

    fun UriModel.toUri(): Uri {
        return  uri.toUri()
    }


    fun Modifier.enabledStyle(enabled: Boolean): Modifier {
        return this.applyIf(enabled) { alpha(0.5f) }
    }

    fun Modifier.applyIf(
        applied: Boolean,
        modifier: Modifier.() -> Modifier,
    ): Modifier {
        return if (applied) this.modifier() else this
    }

    fun <T> Modifier.applyNotNull(
        value: T?,
        modifier: Modifier.(T) -> Modifier,
    ): Modifier {
        return if (value != null) this.modifier(value) else this
    }

    fun Modifier.focusItemStyle(
        focused: Boolean
    ): Modifier {
        return this.applyIf(focused) {
            this
                .background(color = AppColor.PrimaryBackground)
                .border(width = 2.dp, color = AppColor.Primary, shape = RoundedCornerShape(8.dp))
        }
    }

    fun Uri.toDisplayTreePath(context: Context): String? {
        return try {
            val treeUri = if (DocumentsContract.isDocumentUri(context, this)) {
                this
            } else {
                val documentId = DocumentsContract.getTreeDocumentId(this) ?: return null
                DocumentsContract.buildDocumentUriUsingTree(this, documentId) ?: return null
            }
            val path = DocumentsContract.findDocumentPath(context.contentResolver, treeUri) ?: return null
            path.path.lastOrNull()?.substringAfter(':')
        } catch (e: Exception) {
            Log.w(e)
            null
        }
    }

}
