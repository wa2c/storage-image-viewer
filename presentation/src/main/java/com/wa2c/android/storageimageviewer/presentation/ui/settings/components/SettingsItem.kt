package com.wa2c.android.storageimageviewer.presentation.ui.settings.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.focusItemStyle
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.DividerThin
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppSize
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTheme

/**
 * Settings item.
 */
@Composable
internal fun SettingsItem(
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .focusItemStyle()
            .fillMaxWidth()
            .heightIn(64.dp)
            .clickable(enabled = true, onClick = onClick)
            .padding(horizontal = AppSize.M, vertical = AppSize.S)
    ) {
        Text(
            text = text,
            modifier = Modifier
                .align(alignment = Alignment.CenterVertically)
        )
    }
    DividerThin()
}

/**
 * Preview
 */
@Preview(
    name = "Preview",
    group = "Group",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
private fun SettingsItemPreview() {
    AppTheme {
        SettingsItem(
            text = "Settings Item",
        ) {}
    }
}
