package com.wa2c.android.storageimageviewer.presentation.ui.tree

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppColor
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppSize
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTheme
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
            val pageRange = 1..maxPageNumber
            TreeScreenInputNumberDialogContent(
                inputNumber = inputNumberState.value ?: "",
                onInputNumber = {
                    inputNumberState.value += it.toString()
                },
                onUpNumber = {
                    inputNumberState.value = (inputNumberState.value?.toIntOrNull() ?: 0).plus(1).coerceIn(pageRange).toString()
                },
                onDownNumber = {
                    inputNumberState.value = (inputNumberState.value?.toIntOrNull() ?: 0).minus(1).coerceIn(pageRange).toString()
                },
                onFirst = {
                    inputNumberState.value = pageRange.first.toString()
                },
                onLast = {
                    inputNumberState.value = pageRange.last.toString()
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
    onFirst: () -> Unit,
    onLast: () -> Unit,
    onDelete: () -> Unit,
    onSet: () -> Unit,
    onDismiss: () -> Unit,
) {
    Surface(
        color = AppColor.Transparent,
    ) {
        Box(
            contentAlignment = Alignment.CenterEnd,
            modifier = Modifier
                .clip(RoundedCornerShape(AppSize.S))
                .border(1.dp, MaterialTheme.colorScheme.background, RoundedCornerShape(AppSize.S))
                .background(MaterialTheme.colorScheme.onBackground)
                .padding(AppSize.S)
                .widthIn(min = 100.dp)
                .focusable()
                .treeKeyControl(
                    onEnter = onSet,
                    onPlay = onSet,
                    onDirectionCenter = onSet,
                    onDirectionUp = { onFirst() },
                    onDirectionDown = { onLast() },
                    onDirectionLeft = { onDownNumber(1) },
                    onDirectionRight = { onUpNumber(1) },
                    onForward = { onDownNumber(1) },
                    onBackward = { onDownNumber(1) },
                    onForwardSkip = { onDownNumber(10) },
                    onBackwardSkip = { onDownNumber(10) },
                    onForwardLast = { onDownNumber(Int.MAX_VALUE) },
                    onBackwardFirst = { onDownNumber(Int.MIN_VALUE) },
                    onNumber = onInputNumber,
                    onDelete = onDelete,
                    onSearch = onDismiss,
                ),
        ) {
            Text(
                text = inputNumber,
                style = AppTypography.titleLarge,
                color = MaterialTheme.colorScheme.background,
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
    AppTheme {
        TreeScreenInputNumberDialogContent(
            inputNumber = "123",
            onInputNumber = {},
            onUpNumber = {},
            onDownNumber = {},
            onFirst = {},
            onLast = {},
            onDelete = {},
            onSet = {},
            onDismiss = {},
        )
    }
}
