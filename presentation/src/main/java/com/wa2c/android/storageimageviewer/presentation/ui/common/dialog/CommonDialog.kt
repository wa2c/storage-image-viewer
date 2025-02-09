package com.wa2c.android.storageimageviewer.presentation.ui.common.dialog

import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppSize
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTheme

/**
 * Common dialog
 */
@Composable
fun CommonDialog(
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit)? = null,
    confirmButtons: List<DialogButton>? = null,
    dismissButton: DialogButton? = null,
    onDismiss: (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null
) {
    AlertDialog(
        title = title,
        text = content,
        confirmButton = {
            if (!confirmButtons.isNullOrEmpty()) {
                Row {
                    confirmButtons.forEach {
                        Button(
                            onClick = it.onClick,
                            contentPadding = PaddingValues(horizontal = AppSize.S, vertical = AppSize.SS),
                            enabled = it.enabled,
                            modifier = Modifier
                                .padding(start = AppSize.S)
                                .heightIn(min = 0.dp)
                        ) {
                            Text(it.label)
                        }
                    }
                }
            }
        },
        dismissButton = {
            if (dismissButton != null) {
                TextButton(
                    onClick = dismissButton.onClick,
                    enabled = dismissButton.enabled,
                ) {
                    Text(dismissButton.label)
                }
            }
        },
        onDismissRequest = {
            if (onDismiss != null) {
                onDismiss()
            }
        },
//        properties = DialogProperties(
//            usePlatformDefaultWidth = false
//        ),
        modifier = modifier
            .padding(0.dp)

    )
}

/**
 * Dialog button
 */
class DialogButton(
    /** Button label */
    val label: String,
    /** Enabled */
    val enabled: Boolean = true,
    /** Click event */
    val onClick: () -> Unit,
)

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
private fun CommonDialogPreview() {
    AppTheme {
        CommonDialog(
            title = { Text("Title") },
            confirmButtons = listOf(
                DialogButton(label = "Button1") { },
                DialogButton(label = "Button2") { },
            ),
            dismissButton = DialogButton(label = "Cancel") { },
            onDismiss = {},
        ) {
            Text("Dialog content text")
        }
    }
}
