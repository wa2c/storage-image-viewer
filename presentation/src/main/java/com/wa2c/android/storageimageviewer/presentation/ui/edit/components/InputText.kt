package com.wa2c.android.storageimageviewer.presentation.ui.edit.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Size
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.StorageImageViewerTheme

/**
 * Input text
 */
@Composable
fun InputText(
    title: String,
    hint: String,
    value: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readonly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Next,
    ),
    onChange: (String) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Size.SS)
    ) {
        OutlinedTextField(
            value = value ?: "",
            label = { Text(title) },
            enabled = enabled,
            readOnly = readonly,
            placeholder = { Text(hint) },
            onValueChange = { value ->
                onChange(if (keyboardOptions.keyboardType == KeyboardType.Number) value.filter { it.isDigit() } else value)
            },
            keyboardOptions = keyboardOptions,
            maxLines = 1,
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Preview(
    name = "Preview",
    group = "Group",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
private fun InputTextPreview() {
    StorageImageViewerTheme {
        InputText(
            title = "Title",
            hint = "Hint",
            value = "Input",
            enabled = true,
        ) {
        }
    }
}
