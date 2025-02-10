package com.wa2c.android.storageimageviewer.presentation.ui.settings.components

import android.content.res.Configuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.dialog.DialogButton
import com.wa2c.android.storageimageviewer.presentation.ui.common.dialog.SingleChoiceDialog
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTheme

/**
 * Settings single choice item
 */
@Composable
internal fun <T> SettingsSingleChoiceItem(
    title: String,
    items: List<OptionItem<T>>,
    selectedItem: MutableState<T>,
) {
    val showDialog = remember { mutableStateOf(false) }

    SettingsItem(text = title) {
        showDialog.value = true
    }

    if (showDialog.value) {
        SingleChoiceDialog(
            items = items.map { it.label },
            selectedIndex = items.indexOfFirst { it.value == selectedItem.value },
            title = { Text(title) },
            dismissButton = DialogButton(label = stringResource(id = R.string.common_close_label)) {
                showDialog.value = false
            },
            onDismiss = { showDialog.value = false }
        ) { index, _ ->
            selectedItem.value = items[index].value
            showDialog.value = false
        }
    }
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
private fun SettingsSingleChoiceItemPreview() {
    AppTheme {
        SettingsSingleChoiceItem(
            title = "Single Choice Item",
            items = listOf(OptionItem("1", "Item1"), OptionItem("2","Item2"), OptionItem("3","Item3")),
            selectedItem = remember { mutableStateOf("Item1") },
        )
    }
}
