package com.wa2c.android.storageimageviewer.presentation.ui.tree

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.wa2c.android.storageimageviewer.common.values.TreeSortType
import com.wa2c.android.storageimageviewer.common.values.TreeViewType
import com.wa2c.android.storageimageviewer.domain.model.TreeSortModel
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.DividerNormal
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.DividerThin
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppSize
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTheme
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTypography
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeScreenDisplayData

@Composable
fun TreeActionMenu(
    menuExpanded: MutableState<Boolean>,
    displayDataState: State<TreeScreenDisplayData>,
    onSetDisplay: (TreeScreenDisplayData) -> Unit,
) {
    Box {
        IconButton(
            onClick = { menuExpanded.value = true }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_menu),
                contentDescription = "Search",
            )
        }
        DropdownMenu(
            expanded = menuExpanded.value,
            onDismissRequest = { menuExpanded.value = false },
        ) {
            val displayData = displayDataState.value
            TreeSortAction(
                displayData = displayData,
                onSetSort = { onSetDisplay(displayData.copy(sort = it)) },
                onSetView = { onSetDisplay(displayData.copy(viewType = it)) },
                onSetShowPage = { onSetDisplay(displayData.copy(showPage = it)) },
            )
        }
    }
}

@Composable
private fun ColumnScope.TreeSortAction(
    displayData: TreeScreenDisplayData,
    onSetSort: (TreeSortModel) -> Unit,
    onSetView: (TreeViewType) -> Unit,
    onSetShowPage: (Boolean) -> Unit,
) {
    // Sort

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .align(alignment = Alignment.CenterHorizontally)
            .padding(AppSize.SS)
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_sort),
            contentDescription = "Sort",
            modifier = Modifier
                .padding(end = AppSize.S)
        )

        Text(
            text = "Sort",
            style = AppTypography.titleMedium,
            modifier = Modifier
        )
    }

    DividerThin()

    TreeScreenActionMenuRadio(
        text = "Name",
        selected = displayData.sort.type == TreeSortType.Name,
    ) {
        onSetSort(displayData.sort.copy(type = TreeSortType.Name))
    }
    TreeScreenActionMenuRadio(
        text = "Size",
        selected = displayData.sort.type == TreeSortType.Size,
    ) {
        onSetSort(displayData.sort.copy(type = TreeSortType.Size))
    }
    TreeScreenActionMenuRadio(
        text = "Date",
        selected = displayData.sort.type == TreeSortType.Date,
    ) {
        onSetSort(displayData.sort.copy(type = TreeSortType.Date))
    }

    DividerThin()

    // Option
    TreeScreenActionMenuCheck(
        text = "Descending",
        checked = displayData.sort.isDescending
    ) {
        onSetSort(displayData.sort.copy(isDescending = !displayData.sort.isDescending))
    }
    TreeScreenActionMenuCheck(
        text = "Ignore case",
        checked = displayData.sort.isIgnoreCase
    ) {
        onSetSort(displayData.sort.copy(isIgnoreCase = !displayData.sort.isIgnoreCase))
    }
    TreeScreenActionMenuCheck(
        text = "Number",
        checked = displayData.sort.isNumberSort
    ) {
        onSetSort(displayData.sort.copy(isNumberSort = !displayData.sort.isNumberSort))
    }
    TreeScreenActionMenuCheck(
        text = "Folder mix",
        checked = displayData.sort.isFolderMixed
    ) {
        onSetSort(displayData.sort.copy(isFolderMixed = !displayData.sort.isFolderMixed))
    }

    DividerNormal()

    // View

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .align(alignment = Alignment.CenterHorizontally)
            .padding(AppSize.SS)
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_view),
            contentDescription = "View",
            modifier = Modifier
                .padding(end = AppSize.S)
        )

        Text(
            text = "View",
            style = AppTypography.titleMedium,
            modifier = Modifier
        )
    }

    DividerThin()

    if (displayData.isViewerMode) {
        TreeScreenActionMenuCheck(
            text = "Page",
            checked = displayData.showPage,
        ) {
            onSetShowPage(!displayData.showPage)
        }
    } else {
        TreeScreenActionMenuRadio(
            text = "List",
            selected = displayData.viewType.isList,
        ) {
            onSetView(if (displayData.viewType.isLarge) TreeViewType.ListLarge else TreeViewType.ListSmall)
        }
        TreeScreenActionMenuRadio(
            text = "Grid",
            selected = !displayData.viewType.isList,
        ) {
            onSetView(if (displayData.viewType.isLarge) TreeViewType.GridLarge else TreeViewType.GridSmall)
        }

        DividerThin()

        TreeScreenActionMenuRadio(
            text = "Large",
            selected = displayData.viewType.isLarge,
        ) {
            onSetView(if (displayData.viewType.isList) TreeViewType.ListLarge else TreeViewType.GridLarge)
        }
        TreeScreenActionMenuRadio(
            text = "Small",
            selected = !displayData.viewType.isLarge,
        ) {
            onSetView(if (displayData.viewType.isList) TreeViewType.ListSmall else TreeViewType.GridSmall)
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
                    .padding(start = AppSize.S)
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
                    .padding(start = AppSize.S),
            )
        }
    },
    onClick = onClick,
)


/**
 * Preview
 */
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
private fun TreeScreenLazyGridPreview() {
    AppTheme {
        Scaffold {
            Column(
                modifier = Modifier
                    .padding(it)
            ) {
                TreeSortAction(
                    displayData = TreeScreenDisplayData(),
                    onSetSort = {},
                    onSetView = {},
                    onSetShowPage = {},
                )
            }
        }
    }
}
