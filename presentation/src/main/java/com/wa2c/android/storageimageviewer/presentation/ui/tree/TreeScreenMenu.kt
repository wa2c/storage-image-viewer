package com.wa2c.android.storageimageviewer.presentation.ui.tree

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.wa2c.android.storageimageviewer.common.values.TreeSortType
import com.wa2c.android.storageimageviewer.common.values.TreeViewType
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeSortModel
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.focusItemStyle
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.DividerNormal
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.DividerThin
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppSize
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTheme
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTypography
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeScreenOption
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeScreenTreeOption
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeScreenViewerOption

@Composable
fun TreeScreenMenu(
    menuExpanded: MutableState<Boolean>,
    optionState: State<TreeScreenOption>,
    onSetOption: (TreeScreenOption) -> Unit,
) {

    Box {
        IconButton(
            onClick = { menuExpanded.value = true },
            modifier = Modifier
                .focusItemStyle(shape = CircleShape)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_menu),
                contentDescription = "Menu",
            )
        }
        DropdownMenu(
            expanded = menuExpanded.value,
            onDismissRequest = { menuExpanded.value = false },
        ) {
            val option = optionState.value
            TreeSortAction(
                option = option,
                onSetSort = { onSetOption(option.copy(sort = it)) },
                onSetTreeOption = { onSetOption(option.copy(treeOption = it)) },
                onSetViewerOption = { onSetOption(option.copy(viewerOption = it)) },
            )
        }
    }
}

@Composable
private fun ColumnScope.TreeSortAction(
    option: TreeScreenOption,
    onSetSort: (TreeSortModel) -> Unit,
    onSetTreeOption: (TreeScreenTreeOption) -> Unit,
    onSetViewerOption: (TreeScreenViewerOption) -> Unit,
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
            contentDescription = stringResource(R.string.tree_menu_sort_section),
            modifier = Modifier
                .padding(end = AppSize.S)
        )

        Text(
            text = stringResource(R.string.tree_menu_sort_section),
            style = AppTypography.titleMedium,
            modifier = Modifier
        )
    }

    DividerThin()

    TreeScreenActionMenuRadio(
        text = stringResource(R.string.tree_menu_sort_by_name),
        selected = option.sort.type == TreeSortType.Name,
    ) {
        onSetSort(option.sort.copy(type = TreeSortType.Name))
    }
    TreeScreenActionMenuRadio(
        text = stringResource(R.string.tree_menu_sort_by_size_check),
        selected = option.sort.type == TreeSortType.Size,
    ) {
        onSetSort(option.sort.copy(type = TreeSortType.Size))
    }
    TreeScreenActionMenuRadio(
        text = stringResource(R.string.tree_menu_sort_by_date_check),
        selected = option.sort.type == TreeSortType.Date,
    ) {
        onSetSort(option.sort.copy(type = TreeSortType.Date))
    }

    DividerThin()

    // Option
    TreeScreenActionMenuCheck(
        text = stringResource(R.string.tree_menu_sort_option_descending_check),
        checked = option.sort.isDescending
    ) {
        onSetSort(option.sort.copy(isDescending = !option.sort.isDescending))
    }
    TreeScreenActionMenuCheck(
        text = stringResource(R.string.tree_menu_sort_option_ignore_case_check),
        checked = option.sort.isIgnoreCase
    ) {
        onSetSort(option.sort.copy(isIgnoreCase = !option.sort.isIgnoreCase))
    }
    TreeScreenActionMenuCheck(
        text = stringResource(R.string.tree_menu_sort_option_numerically_check),
        checked = option.sort.isNumberSort
    ) {
        onSetSort(option.sort.copy(isNumberSort = !option.sort.isNumberSort))
    }
    TreeScreenActionMenuCheck(
        text = stringResource(R.string.tree_menu_sort_option_mix_folder_check),
        checked = option.sort.isFolderMixed
    ) {
        onSetSort(option.sort.copy(isFolderMixed = !option.sort.isFolderMixed))
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
            contentDescription = stringResource(R.string.tree_menu_view_section),
            modifier = Modifier
                .padding(end = AppSize.S)
        )

        Text(
            text = stringResource(R.string.tree_menu_view_section),
            style = AppTypography.titleMedium,
            modifier = Modifier
        )
    }

    DividerThin()

    if (option.isViewerMode) {
        val viewerOption = option.viewerOption
        TreeScreenActionMenuCheck(
            text = stringResource(R.string.tree_menu_view_show_page_check),
            checked = viewerOption.showPage,
        ) {
            onSetViewerOption(viewerOption.copy(showPage = !viewerOption.showPage) )
        }

        TreeScreenActionMenuCheck(
            text = stringResource(R.string.tree_menu_view_volume_scroll_check),
            checked = viewerOption.volumeScroll,
        ) {
            onSetViewerOption(viewerOption.copy(volumeScroll = !viewerOption.volumeScroll) )
        }
    } else {
        val treeOption = option.treeOption
        TreeScreenActionMenuRadio(
            text = stringResource(R.string.tree_menu_view_list_none_check),
            selected = treeOption.viewType == TreeViewType.ListNone,
        ) {
            onSetTreeOption(treeOption.copy(viewType = TreeViewType.ListNone))
        }
        TreeScreenActionMenuRadio(
            text = stringResource(R.string.tree_menu_view_list_small_check),
            selected = treeOption.viewType == TreeViewType.ListSmall,
        ) {
            onSetTreeOption(treeOption.copy(viewType = TreeViewType.ListSmall))
        }
        TreeScreenActionMenuRadio(
            text = stringResource(R.string.tree_menu_view_list_large_check),
            selected = treeOption.viewType == TreeViewType.ListLarge,
        ) {
            onSetTreeOption(treeOption.copy(viewType = TreeViewType.ListLarge))
        }
        TreeScreenActionMenuRadio(
            text = stringResource(R.string.tree_menu_view_grid_small_check),
            selected = treeOption.viewType == TreeViewType.GridSmall,
        ) {
            onSetTreeOption(treeOption.copy(viewType = TreeViewType.GridSmall))
        }
        TreeScreenActionMenuRadio(
            text = stringResource(R.string.tree_menu_view_grid_large_check),
            selected = treeOption.viewType == TreeViewType.GridLarge,
        ) {
            onSetTreeOption(treeOption.copy(viewType = TreeViewType.GridLarge))
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
                    option = TreeScreenOption(),
                    onSetSort = {},
                    onSetTreeOption = {},
                    onSetViewerOption = {},
                )
            }
        }
    }
}
