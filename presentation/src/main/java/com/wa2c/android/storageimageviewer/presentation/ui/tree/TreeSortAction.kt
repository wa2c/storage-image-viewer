package com.wa2c.android.storageimageviewer.presentation.ui.tree

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.wa2c.android.storageimageviewer.common.values.SortType
import com.wa2c.android.storageimageviewer.domain.model.SortModel
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Size

@Composable
fun TreeSortAction(
    menuExpanded: MutableState<Boolean>,
    sortState: State<SortModel>,
    onSetSort: (SortModel) -> Unit,
) {
    Box {
        IconButton(
            onClick = { menuExpanded.value = true }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_sort),
                contentDescription = "Search",
            )
        }
        DropdownMenu(
            expanded = menuExpanded.value,
            onDismissRequest = { menuExpanded.value = false },
        ) {
            // Type
            TreeScreenActionMenuRadio(
                text = "Name",
                selected = sortState.value.type == SortType.Name,
            ) {
                onSetSort(sortState.value.copy(type = SortType.Name))
            }
            TreeScreenActionMenuRadio(
                text = "Size",
                selected = sortState.value.type == SortType.Size,
            ) {
                onSetSort(sortState.value.copy(type = SortType.Size))
            }
            TreeScreenActionMenuRadio(
                text = "Date",
                selected = sortState.value.type == SortType.Date,
            ) {
                onSetSort(sortState.value.copy(type = SortType.Date))
            }

            // Option
            TreeScreenActionMenuCheck(
                text = "Descending",
                checked = sortState.value.isDescending
            ) {
                onSetSort(sortState.value.copy(isDescending = !sortState.value.isDescending))
            }
            TreeScreenActionMenuCheck(
                text = "Ignore case",
                checked = sortState.value.isIgnoreCase
            ) {
                onSetSort(sortState.value.copy(isIgnoreCase = !sortState.value.isIgnoreCase))
            }
            TreeScreenActionMenuCheck(
                text = "Number",
                checked = sortState.value.isNumberSort
            ) {
                onSetSort(sortState.value.copy(isNumberSort = !sortState.value.isNumberSort))
            }
            TreeScreenActionMenuCheck(
                text = "Folder mix",
                checked = sortState.value.isFolderMixed
            ) {
                onSetSort(sortState.value.copy(isFolderMixed = !sortState.value.isFolderMixed))
            }
        }
    }
}


@Composable
private fun TreeScreenActionMenuRadio(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) = DropdownMenuItem(
    text = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
            )
            Text(
                text = text,
                modifier = Modifier
                    .padding(start = Size.SS)
            )
        }
    },
    onClick = onClick,
)

@Composable
private fun TreeScreenActionMenuCheck(
    text: String,
    checked: Boolean,
    onClick: () -> Unit,
) = DropdownMenuItem(
    text = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = null,
            )
            Text(
                text = text,
                modifier = Modifier
                    .padding(start = Size.SS),
            )
        }
    },
    onClick = onClick,
)
