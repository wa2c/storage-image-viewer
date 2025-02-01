package com.wa2c.android.storageimageviewer.presentation.ui.common.components

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.core.graphics.drawable.toBitmap
import com.wa2c.android.storageimageviewer.common.utils.Log
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.toUri
import com.wa2c.android.storageimageviewer.presentation.ui.common.ValueResource.drawableResId
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppSize

@Composable
fun StorageIcon(
    storage: StorageModel,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
    ) {
        val context = LocalContext.current

        if (storage.type == StorageType.SAF || storage.type == StorageType.Download) {
            try {
                storage.uri.toUri().authority?.let { authority ->
                    val packages: List<PackageInfo> = context.packageManager.getInstalledPackages(
                        PackageManager.GET_PROVIDERS)
                    packages.firstOrNull { pack ->
                        pack.providers?.firstOrNull { provider ->
                            provider.authority?.let { authority == it } ?: false
                        } != null
                    }?.applicationInfo?.loadIcon(context.packageManager)
                }
            } catch (e: Exception) {
                Log.w(e)
                null
            }?.let { drawable ->
                // App Icon
                Image(
                    bitmap = drawable.toBitmap().asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(AppSize.IconMiddle),
                )
            } ?: let {
                Icon(
                    imageVector = ImageVector.vectorResource(storage.type.drawableResId()),
                    contentDescription = null,
                    modifier = Modifier
                        .size(AppSize.IconMiddle),
                )
            }
        } else {
            Icon(
                imageVector = ImageVector.vectorResource(storage.type.drawableResId()),
                contentDescription = null,
                modifier = Modifier
                    .size(AppSize.IconMiddle),
            )
        }
    }
}
