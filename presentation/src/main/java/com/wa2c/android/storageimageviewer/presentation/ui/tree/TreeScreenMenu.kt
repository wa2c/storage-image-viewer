package com.wa2c.android.storageimageviewer.presentation.ui.tree

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.wa2c.android.storageimageviewer.common.values.TreeFolderOrder
import com.wa2c.android.storageimageviewer.common.values.TreeSortType
import com.wa2c.android.storageimageviewer.common.values.TreeViewType
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.enabledStyle
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.focusItemStyle
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.DividerThin
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppSize
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTheme
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTypography
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeScreenOption
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeScreenTreeOption
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeScreenViewerOption
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeSortModel

@Composable
fun TreeScreenViewMenu(
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
            val iconRes = if (optionState.value.isViewerMode) R.drawable.ic_image else R.drawable.ic_view
            Icon(
                imageVector = ImageVector.vectorResource(id = iconRes),
                contentDescription = stringResource(R.string.tree_menu_view_section),
            )
        }
        DropdownMenu(
            expanded = menuExpanded.value,
            onDismissRequest = { menuExpanded.value = false },
            modifier = Modifier
                .treeKeyControl(
                    onMenu = { menuExpanded.value = false },
                )
        ) {
            val option = optionState.value
            ViewMenuAction(
                option = option,
                onSetTreeOption = { onSetOption(option.copy(treeOption = it)) },
                onSetViewerOption = { onSetOption(option.copy(viewerOption = it)) },
            )
        }
    }
}

@Composable
private fun ViewMenuAction(
    option: TreeScreenOption,
    onSetTreeOption: (TreeScreenTreeOption) -> Unit,
    onSetViewerOption: (TreeScreenViewerOption) -> Unit,
) {
    if (option.isViewerMode) {
        val viewerOption = option.viewerOption
        TreeScreenActionMenuCheck(
            text = stringResource(R.string.tree_menu_view_show_page_check),
            checked = viewerOption.showPage,
        ) {
            onSetViewerOption(viewerOption.copy(showPage = !viewerOption.showPage) )
        }

        TreeScreenActionMenuCheck(
            text = stringResource(R.string.tree_menu_view_show_animation_check),
            checked = viewerOption.showAnimation,
        ) {
            onSetViewerOption(viewerOption.copy(showAnimation = !viewerOption.showAnimation) )
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
fun TreeScreenSortMenu(
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
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_sort),
                contentDescription = stringResource(R.string.tree_menu_sort_section),
            )
        }
        DropdownMenu(
            expanded = menuExpanded.value,
            onDismissRequest = { menuExpanded.value = false },
            modifier = Modifier
                .treeKeyControl(
                    onMenu = { menuExpanded.value = false },
                )
        ) {
            val option = optionState.value
            Row {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    val enabledFolder = option.treeOption.folderOrder != TreeFolderOrder.Mix
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .align(alignment = Alignment.CenterHorizontally)
                            .padding(AppSize.SS)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_folder),
                            contentDescription = stringResource(R.string.tree_menu_sort_target_folder),
                            modifier = Modifier
                                .enabledStyle(enabledFolder)
                                .padding(end = AppSize.S)
                        )

                        Text(
                            text = stringResource(R.string.tree_menu_sort_target_folder),
                            style = AppTypography.titleMedium,
                            modifier = Modifier
                                .enabledStyle(enabledFolder)
                        )
                    }

                    TreeSortAction(
                        sort = option.folderSort,
                        enabled = enabledFolder,
                        onSetSort = { onSetOption(option.copy(folderSort = it)) },
                    )
                }
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .align(alignment = Alignment.CenterHorizontally)
                            .padding(AppSize.SS)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_image),
                            contentDescription = stringResource(R.string.tree_menu_sort_target_file),
                            modifier = Modifier
                                .padding(end = AppSize.S)
                        )

                        Text(
                            text = stringResource(R.string.tree_menu_sort_target_file),
                            style = AppTypography.titleMedium,
                            modifier = Modifier
                        )
                    }

                    TreeSortAction(
                        sort = option.fileSort,
                        enabled = true,
                        onSetSort = { onSetOption(option.copy(fileSort = it)) },
                    )
                }
            }

            DividerThin()

            TreeScreenActionMenuRadio(
                text = stringResource(R.string.tree_menu_sort_folder_first_check),
                selected = option.treeOption.folderOrder == TreeFolderOrder.First,
            ) {
                val treeOption = optionState.value.treeOption.copy(folderOrder = TreeFolderOrder.First)
                onSetOption(option.copy(treeOption = treeOption))
            }
            TreeScreenActionMenuRadio(
                text = stringResource(R.string.tree_menu_sort_folder_last_check),
                selected = option.treeOption.folderOrder == TreeFolderOrder.Last,
            ) {
                val treeOption = optionState.value.treeOption.copy(folderOrder = TreeFolderOrder.Last)
                onSetOption(option.copy(treeOption = treeOption))
            }
            TreeScreenActionMenuRadio(
                text = stringResource(R.string.tree_menu_sort_folder_mix_check),
                selected = option.treeOption.folderOrder == TreeFolderOrder.Mix,
            ) {
                val treeOption = optionState.value.treeOption.copy(folderOrder = TreeFolderOrder.Mix)
                onSetOption(option.copy(treeOption = treeOption))
            }
        }
    }
}

@Composable
private fun TreeSortAction(
    sort: TreeSortModel,
    enabled: Boolean,
    onSetSort: (TreeSortModel) -> Unit,
) {
    DividerThin(
        modifier = Modifier
            .padding(horizontal = AppSize.SS)
    )

    TreeScreenActionMenuRadio(
        text = stringResource(R.string.tree_menu_sort_by_name_check),
        selected = sort.type == TreeSortType.Name,
        enabled = enabled,
    ) {
        onSetSort(sort.copy(type = TreeSortType.Name))
    }
    TreeScreenActionMenuRadio(
        text = stringResource(R.string.tree_menu_sort_by_size_check),
        selected = sort.type == TreeSortType.Size,
        enabled = enabled,
    ) {
        onSetSort(sort.copy(type = TreeSortType.Size))
    }
    TreeScreenActionMenuRadio(
        text = stringResource(R.string.tree_menu_sort_by_date_check),
        selected = sort.type == TreeSortType.Date,
        enabled = enabled,
    ) {
        onSetSort(sort.copy(type = TreeSortType.Date))
    }

    DividerThin(
        modifier = Modifier
            .padding(horizontal = AppSize.SS)
    )

    // Option
    TreeScreenActionMenuCheck(
        text = stringResource(R.string.tree_menu_sort_option_descending_check),
        checked = sort.isDescending,
        enabled = enabled,
    ) {
        onSetSort(sort.copy(isDescending = !sort.isDescending))
    }
    TreeScreenActionMenuCheck(
        text = stringResource(R.string.tree_menu_sort_option_ignore_case_check),
        checked = sort.isIgnoreCase,
        enabled = enabled,
    ) {
        onSetSort(sort.copy(isIgnoreCase = !sort.isIgnoreCase))
    }
    TreeScreenActionMenuCheck(
        text = stringResource(R.string.tree_menu_sort_option_numerically_check),
        checked = sort.isNumberSort,
        enabled = enabled,
    ) {
        onSetSort(sort.copy(isNumberSort = !sort.isNumberSort))
    }
}

@Composable
private fun TreeScreenActionMenuRadio(
    text: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) = DropdownMenuItem(
    text = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = selected,
                enabled = enabled,
                onClick = null,
            )
            Text(
                text = text,
                modifier = Modifier
                    .padding(start = AppSize.S)
            )
        }
    },
    enabled = enabled,
    onClick = onClick,
)

@Composable
private fun TreeScreenActionMenuCheck(
    text: String,
    checked: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) = DropdownMenuItem(
    text = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = checked,
                enabled = enabled,
                onCheckedChange = null,
            )
            Text(
                text = text,
                modifier = Modifier
                    .padding(start = AppSize.S),
            )
        }
    },
    enabled = enabled,
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
private fun TreeScreenViewMenuPreview() {
    AppTheme {
        Scaffold {
            Column(
                modifier = Modifier
                    .padding(it)
            ) {
                TreeScreenViewMenu(
                    menuExpanded = remember { mutableStateOf(true) },
                    optionState = remember { mutableStateOf(TreeScreenOption()) },
                    onSetOption = {},
                )
            }
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
private fun TreeScreenSortMenuPreview() {
    AppTheme {
        Scaffold {
            Column(
                modifier = Modifier
                    .padding(it)
            ) {
                TreeScreenSortMenu(
                    menuExpanded = remember { mutableStateOf(true) },
                    optionState = remember { mutableStateOf(TreeScreenOption()) },
                    onSetOption = {},
                )
            }
        }
    }
}
