package com.wa2c.android.storageimageviewer.presentation.ui.common

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.wa2c.android.storageimageviewer.common.utils.Log
import com.wa2c.android.storageimageviewer.domain.model.UriModel

object Extensions {

    fun UriModel.toUri(): Uri {
        return  uri.toUri()
    }


    @Composable
    fun Modifier.enabledStyle(enabled: Boolean): Modifier {
        return this.applyIf(enabled) { alpha(0.5f) }
    }

    @Composable
    fun Modifier.applyIf(
        applied: Boolean,
        modifier: @Composable Modifier.() -> Modifier,
    ): Modifier {
        return if (applied) this.modifier() else this
    }

    @Composable
    fun <T> Modifier.applyNotNull(
        value: T?,
        modifier: Modifier.(T) -> Modifier,
    ): Modifier {
        return if (value != null) this.modifier(value) else this
    }

    @Composable
    fun Modifier.focusItemStyle(
        shape: Shape = RoundedCornerShape(8.dp)
    ): Modifier {
        var isFocused by remember { mutableStateOf(false) }
        return this
            .onFocusChanged { isFocused = it.isFocused }
            .applyIf(isFocused) {
                this
                    .clip(shape)
                    .background(color = MaterialTheme.colorScheme.primaryContainer.copy(0.25f))
                    .border(width = 2.dp, color = MaterialTheme.colorScheme.primary, shape = shape)
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
