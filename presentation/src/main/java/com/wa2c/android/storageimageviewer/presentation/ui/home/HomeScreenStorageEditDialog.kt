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
import androidx.compose.ui.tooling.preview.Preview
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.StorageIcon
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
                val text = if (storage.isNew) "Add" else "Edit"
                Text(text)
            }
        },
        confirmButtons = buildList {
            if (!storage.isNew) {
                add(
                    DialogButton(
                        label = "Delete", // FIXME
                        onClick = { visibleDeleteConfirm = true }
                    ),
                )
            }
            add(
                DialogButton(
                    label = "Save", // FIXME
                    enabled = storage.name.isNotEmpty() && !storage.uri.isInvalidUri,
                    onClick = { onClickSave(storage) },
                ),
            )
        }

        ,
        dismissButton = DialogButton(
            label = "Cancel", // FIXME
            onClick = onDismiss,
        ),
        onDismiss = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // URI
            Box {
                OutlinedTextField(
                    value = storage.uri.uri,
                    label = { Text("URI") }, // fixme
                    placeholder = { Text("Select URI") }, // fixme
                    readOnly = true,
                    onValueChange = { },
                    maxLines = 1,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .treeKeyControl(
                            isPreview = true,
                            onEnter = { onClickUri(storage.uri) },
                            onPlay = { onClickUri(storage.uri) }

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
                label = { Text("Name") }, // fixme
                placeholder = { Text("Input name") }, // fixme
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
            title = { Text("Delete") },
            confirmButtons = listOf(
                DialogButton(
                    label = "Delete", // FIXME
                    onClick = {
                        visibleDeleteConfirm = false
                        onClickDelete(storage)
                    }
                )
            ),
            dismissButton =
            DialogButton(
                label = "Cancel", // FIXME
                onClick = {
                    visibleDeleteConfirm = false
                }
            ),
        ) {
            Text("Are you sure to delete?")
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
            uri = UriModel(uri = "content://test1/"),
            name = "Test Storage 1",
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
