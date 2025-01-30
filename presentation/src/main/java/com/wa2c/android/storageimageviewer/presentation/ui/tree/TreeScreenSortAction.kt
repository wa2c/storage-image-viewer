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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.wa2c.android.storageimageviewer.common.values.TreeSortType
import com.wa2c.android.storageimageviewer.common.values.TreeViewType
import com.wa2c.android.storageimageviewer.domain.model.TreeSortModel
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.DividerThin
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppSize

@Composable
fun TreeViewAction(
    menuExpanded: MutableState<Boolean>,
    viewType: TreeViewType,
    onSetView: (TreeViewType) -> Unit,
    onKeyLeft: () -> Unit = {},
    onKeyRight: () -> Unit = {},
) {

    Box {
        IconButton(
            onClick = { menuExpanded.value = true }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_view),
                contentDescription = "View",
            )
        }
        DropdownMenu(
            expanded = menuExpanded.value,
            onDismissRequest = { menuExpanded.value = false },
            modifier = Modifier
                .treeKeyControl(
                    isPreview = true,
                    onDirectionLeft = { onKeyLeft() },
                    onDirectionRight = { onKeyRight() },
                )
        ) {
            // Type
            TreeScreenActionMenuRadio(
                text = "List",
                selected = viewType.isList,
            ) {
                onSetView(if (viewType.isLarge) TreeViewType.ListLarge else TreeViewType.ListSmall)
            }
            TreeScreenActionMenuRadio(
                text = "Grid",
                selected = !viewType.isList,
            ) {
                onSetView(if (viewType.isLarge) TreeViewType.GridLarge else TreeViewType.GridSmall)
            }

            DividerThin()

            TreeScreenActionMenuRadio(
                text = "Large",
                selected = viewType.isLarge,
            ) {
                onSetView(if (viewType.isList) TreeViewType.ListLarge else TreeViewType.GridLarge)
            }
            TreeScreenActionMenuRadio(
                text = "Small",
                selected = !viewType.isLarge,
            ) {
                onSetView(if (viewType.isList) TreeViewType.ListSmall else TreeViewType.GridSmall)
            }
        }

    }
}

@Composable
fun TreeSortAction(
    menuExpanded: MutableState<Boolean>,
    sort: TreeSortModel,
    onSetSort: (TreeSortModel) -> Unit,
    onKeyLeft: () -> Unit = {},
    onKeyRight: () -> Unit = {},
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
            modifier = Modifier
                .treeKeyControl(
                    isPreview = true,
                    onDirectionLeft = { onKeyLeft() },
                    onDirectionRight = { onKeyRight() },
                )
        ) {
            // Type
            TreeScreenActionMenuRadio(
                text = "Name",
                selected = sort.type == TreeSortType.Name,
            ) {
                onSetSort(sort.copy(type = TreeSortType.Name))
            }
            TreeScreenActionMenuRadio(
                text = "Size",
                selected = sort.type == TreeSortType.Size,
            ) {
                onSetSort(sort.copy(type = TreeSortType.Size))
            }
            TreeScreenActionMenuRadio(
                text = "Date",
                selected = sort.type == TreeSortType.Date,
            ) {
                onSetSort(sort.copy(type = TreeSortType.Date))
            }

            DividerThin()

            // Option
            TreeScreenActionMenuCheck(
                text = "Descending",
                checked = sort.isDescending
            ) {
                onSetSort(sort.copy(isDescending = !sort.isDescending))
            }
            TreeScreenActionMenuCheck(
                text = "Ignore case",
                checked = sort.isIgnoreCase
            ) {
                onSetSort(sort.copy(isIgnoreCase = !sort.isIgnoreCase))
            }
            TreeScreenActionMenuCheck(
                text = "Number",
                checked = sort.isNumberSort
            ) {
                onSetSort(sort.copy(isNumberSort = !sort.isNumberSort))
            }
            TreeScreenActionMenuCheck(
                text = "Folder mix",
                checked = sort.isFolderMixed
            ) {
                onSetSort(sort.copy(isFolderMixed = !sort.isFolderMixed))
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
                    .padding(start = AppSize.SS)
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
                    .padding(start = AppSize.SS),
            )
        }
    },
    onClick = onClick,
)
