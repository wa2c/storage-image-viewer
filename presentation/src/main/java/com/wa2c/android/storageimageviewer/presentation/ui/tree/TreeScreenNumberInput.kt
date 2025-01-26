package com.wa2c.android.storageimageviewer.presentation.ui.tree

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Color
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Size
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.StorageImageViewerTheme
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

@Composable
fun TreeScreenInputNumberDialog(
    inputNumberState: MutableState<String?>,
    maxPageNumber: Int,
    onSet: (String) -> Unit
) {
    if (inputNumberState.value != null) {
        Dialog(
            onDismissRequest = { inputNumberState.value = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
            ),
        ) {
            TreeScreenInputNumberDialogContent(
                inputNumber = inputNumberState.value ?: "",
                onInputNumber = {
                    inputNumberState.value += it.toString()
                },
                onUpNumber = {
                    max((inputNumberState.value?.toIntOrNull() ?: 0).plus(1), 1).let {
                        inputNumberState.value = it.toString()
                    }
                },
                onDownNumber = {
                    min((inputNumberState.value?.toIntOrNull() ?: 0).minus(1), maxPageNumber).let {
                        inputNumberState.value = it.toString()
                    }
                },
                onDelete = {
                    inputNumberState.value = inputNumberState.value?.dropLast(1)
                },
                onSet = {
                    val text = inputNumberState.value ?: return@TreeScreenInputNumberDialogContent
                    onSet(text)
                    inputNumberState.value = null
                },
                onDismiss = {
                    inputNumberState.value = null
                },
            )
        }
    }

    LaunchedEffect(inputNumberState.value) {
        launch {
            delay(3000)
            inputNumberState.value = null
        }
    }
}

@Composable
private fun TreeScreenInputNumberDialogContent(
    inputNumber: String,
    onInputNumber: (Int) -> Unit,
    onUpNumber: (step: Int) -> Unit,
    onDownNumber: (step: Int) -> Unit,
    onDelete: () -> Unit,
    onSet: () -> Unit,
    onDismiss: () -> Unit,
) {
    Surface {
        Box(
            contentAlignment = Alignment.CenterEnd,
            modifier = Modifier
                .clip(RoundedCornerShape(Size.S))
                .background(Color.NumberInputBackground)
                .padding(Size.S)
                .widthIn(min = 100.dp)
                .focusable()
                .treeKeyControl(
                    onNumber = onInputNumber,
                    onDirectionRight = { onUpNumber(1) },
                    onDirectionUp = { onUpNumber(1) },
                    onDirectionLeft = { onDownNumber(1) },
                    onDirectionDown = { onDownNumber(1) },
                    onForward = { onDownNumber(1) },
                    onBackward = { onDownNumber(1) },
                    onForwardSkip = { onDownNumber(10) },
                    onBackwardSkip = { onDownNumber(10) },
                    onForwardLast = { onDownNumber(Int.MAX_VALUE) },
                    onBackwardFirst = { onDownNumber(Int.MIN_VALUE) },
                    onSearch = onDismiss,
                    onDelete = onDelete,
                    onEnter = onSet,
                    onPlay = onSet,
                ),
        ) {
            Text(
                text = inputNumber,
                style = Typography.titleLarge,
                modifier = Modifier
            )
        }
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
private fun TreeScreenInputNumberPreview() {
    StorageImageViewerTheme {
        TreeScreenInputNumberDialogContent(
            inputNumber = "123",
            onInputNumber = {},
            onUpNumber = {},
            onDownNumber = {},
            onDelete = {},
            onSet = {},
            onDismiss = {},
        )
    }
}
