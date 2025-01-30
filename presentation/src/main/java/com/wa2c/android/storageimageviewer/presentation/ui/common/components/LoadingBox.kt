package com.wa2c.android.storageimageviewer.presentation.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppColor

@Composable
fun LoadingBox(isLoading: Boolean) {
    if (!isLoading) return
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColor.LoadingBackground)
            .clickable(
                enabled = true,
                indication = null,
                interactionSource = interactionSource,
                onClick = {}
            ),
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.Center)
        )
    }
}
