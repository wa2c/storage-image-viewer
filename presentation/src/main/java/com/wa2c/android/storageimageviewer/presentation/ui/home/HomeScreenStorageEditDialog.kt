package com.wa2c.android.storageimageviewer.presentation.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.StorageIcon
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.getStorageAppName
import com.wa2c.android.storageimageviewer.presentation.ui.common.dialog.CommonDialog
import com.wa2c.android.storageimageviewer.presentation.ui.common.dialog.DialogButton
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppSize
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTheme
import com.wa2c.android.storageimageviewer.presentation.ui.tree.treeKeyControl


@Composable
fun HomeScreenStorageEditDialog(
    editStorage: State<StorageModel?>,
    onClickUri: (uri: UriModel) -> Unit,
    onEditName: (name: String) -> Unit,
    onClickSave: (storage: StorageModel) -> Unit,
    onClickDelete: (storage: StorageModel) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val storage = editStorage.value ?: return
    var visibleDeleteConfirm by remember { mutableStateOf(false) }

    CommonDialog(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StorageIcon(
                    storage = storage,
                    modifier = Modifier
                        .padding(end = AppSize.S)
                        .size(AppSize.IconMiddle)
                )
                val text = if (storage.isNew) R.string.home_storage_dialog_title_add
                else R.string.home_storage_dialog_title_edit
                Text(stringResource(text))
            }
        },
        confirmButtons = buildList {
            if (!storage.isNew) {
                add(
                    DialogButton(
                        label = stringResource(R.string.home_storage_dialog_remove_button),
                        onClick = { visibleDeleteConfirm = true }
                    ),
                )
            }
            add(
                DialogButton(
                    label = stringResource(R.string.home_storage_dialog_save_button),
                    enabled = storage.name.isNotEmpty() && !storage.uri.isInvalidUri,
                    onClick = { onClickSave(storage) },
                ),
            )
        },
        dismissButton = DialogButton(
            label = stringResource(R.string.common_cancel_label),
            onClick = onDismiss,
        ),
        onDismiss = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // App
            Text(
                text = getStorageAppName(context = context, storage = storage)
            )

            // URI
            Box {
                OutlinedTextField(
                    value = storage.uri.uri,
                    label = { Text(stringResource(R.string.home_storage_dialog_uri_label)) },
                    placeholder = { Text(stringResource(R.string.home_storage_dialog_uri_placeholder)) },
                    readOnly = true,
                    onValueChange = { },
                    maxLines = 1,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .treeKeyControl(
                            isPreview = true,
                            onEnter = { onClickUri(storage.uri) },
                            onPlay = { onClickUri(storage.uri) },
                            onDirectionCenter = { onClickUri(storage.uri) },

                        )
                        .padding(top = AppSize.M),
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(top = AppSize.L)
                        .clip(RoundedCornerShape(AppSize.SS))
                        .clickable { onClickUri(storage.uri) },
                )
            }

            // Name
            OutlinedTextField(
                value = storage.name,
                label = { Text(stringResource(R.string.home_storage_dialog_name_label)) },
                placeholder = { Text(stringResource(R.string.home_storage_dialog_name_placeholder)) },
                onValueChange = { value ->
                    onEditName(value)
                },
                maxLines = 1,
                singleLine = true,
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .padding(top = AppSize.S)
                    .fillMaxWidth()
            )

        }
    }

    if (visibleDeleteConfirm) {
        CommonDialog(
            title = { Text(stringResource(R.string.home_storage_dialog_confirm_title)) },
            confirmButtons = listOf(
                DialogButton(
                    label = stringResource(R.string.home_storage_dialog_remove_button),
                    onClick = {
                        visibleDeleteConfirm = false
                        onClickDelete(storage)
                    }
                )
            ),
            dismissButton =
            DialogButton(
                label = stringResource(R.string.common_cancel_label),
                onClick = {
                    visibleDeleteConfirm = false
                }
            ),
        ) {
            Text(stringResource(R.string.home_storage_dialog_confirm_description))
        }
    }

    LaunchedEffect(storage.uri) {
        if (storage.uri.isInvalidUri) return@LaunchedEffect
        focusRequester.requestFocus()
    }
}

/**
 * Preview
 */
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
private fun HomeScreenStorageEditDialogPreview() {
    AppTheme {
        val storage = StorageModel(
            id = "1",
            name = "Test Storage 1",
            uri = UriModel(uri = "content://test1/"),
            rootUri = UriModel(uri = "content://test1/"),
            type = StorageType.SAF,
            sortOrder = 1,
        )

        HomeScreenStorageEditDialog(
            editStorage = remember { mutableStateOf(storage) },
            onClickUri = {},
            onEditName = {},
            onClickSave = {},
            onClickDelete = {},
            onDismiss = {},
        )
    }
}
