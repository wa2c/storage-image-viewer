package com.wa2c.android.storageimageviewer.presentation.ui.tree

import android.content.res.Configuration
import android.text.format.DateUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.wa2c.android.storageimageviewer.common.values.SortType
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.domain.model.FileModel
import com.wa2c.android.storageimageviewer.domain.model.SortModel
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.DividerNormal
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.DividerThin
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.LoadingBox
import com.wa2c.android.storageimageviewer.presentation.ui.common.showMessage
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Size
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.StorageImageViewerTheme
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Typography

@Composable
fun TreeScreen(
    viewModel: TreeViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onNavigateBack: () -> Unit,
) {
    val view = LocalView.current
    val window = (view.context as? ComponentActivity)?.window

    val snackBarHostState = remember { SnackbarHostState() }
    val fileListState = viewModel.currentList.collectAsStateWithLifecycle()
    val currentDirState = viewModel.currentDir.collectAsStateWithLifecycle()
    val viewerFileState = viewModel.viewerFile.collectAsStateWithLifecycle()
    val sortState = viewModel.sortState.collectAsStateWithLifecycle()
    val busyState = viewModel.busyState.collectAsStateWithLifecycle()
    val resultState = viewModel.resultState.collectAsStateWithLifecycle()

    Box {
        window?.let {
            val windowInsetsController = WindowInsetsControllerCompat(window, view)
            //WindowCompat.setDecorFitsSystemWindows(window, visibleViewer)
            //windowInsetsController.isAppearanceLightStatusBars = visibleViewer
            //windowInsetsController.isAppearanceLightStatusBars = !visibleViewer
        }

        TreeScreenContainer(
            modifier = Modifier.fillMaxSize(),
            snackBarHostState = snackBarHostState,
            fileListState = fileListState,
            currentFileState = currentDirState,
            sortState = sortState,
            busyState = busyState,
            onSetSort = viewModel::sortFile,
            onClickItem = viewModel::openFile,
            onClickUp = viewModel::openParent,
            onClickBack = onNavigateBack,
        )

        AnimatedVisibility(
            visible = (viewerFileState.value != null),
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
            ),
            content = {
                TreeScreenViewerContainer(
                    initialFile = viewerFileState,
                    fileListState = fileListState,
                )
            },
        )
    }

    LaunchedEffect(resultState) {
        snackBarHostState.showMessage(resultState.value)
    }

    // Back button
    BackHandler {
        if (busyState.value) {
            viewModel.cancelLoading()
        } else if (viewerFileState.value != null) {
            viewModel.closeViewer()
        } else if (!viewModel.isRoot) {
            viewModel.openParent()
        } else {
            onNavigateBack()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TreeScreenContainer(
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState,
    fileListState: State<List<FileModel>>,
    currentFileState: State<FileModel?>,
    sortState: State<SortModel>,
    busyState: State<Boolean>,
    onSetSort: (SortModel) -> Unit,
    onClickItem: (FileModel) -> Unit,
    onClickUp: () -> Unit,
    onClickBack: () -> Unit,
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = onClickBack) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    Box {

                        IconButton(
                            onClick = { sortMenuExpanded = true }
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_sort),
                                contentDescription = "Search",
                            )
                        }
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false },
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
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
        modifier = Modifier
            .onPreviewKeyEvent { keyEvent ->
                val keyCode = keyEvent.key
                val isDown = keyEvent.type == KeyEventType.KeyDown
                if (keyCode == Key.Menu && isDown) {
                    sortMenuExpanded = true
                    true
                } else {
                    false
                }
            }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column {
                if (fileListState.value.isEmpty()) {
                    TreeScreenEmpty(
                        modifier = Modifier
                            .weight(1f)
                    )
                } else {
                    TreeScreenStorageList(
                        modifier = Modifier
                            .weight(1f),
                        fileListState = fileListState,
                        onClickItem = onClickItem,
                    )
                }

                DividerNormal()

                TreeScreenControlBar(
                    file = currentFileState,
                    onClickUp = onClickUp
                )
            }
            LoadingBox(
                isLoading = busyState.value,
            )
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

@Composable
private fun TreeScreenControlBar(
    file: State<FileModel?>,
    onClickUp: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = Size.S)
    ) {
        val pathScroll = rememberScrollState()
        IconButton(
            onClick = onClickUp,
            modifier = Modifier
                .size(Size.IconMiddle)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_folder_up),
                contentDescription = "Up",
            )
        }
        Text(
            text = file.value?.uri?.uri ?: "",
            maxLines = 1,
            modifier = Modifier
                .padding(start = Size.SS)
                .weight(1f)
                .horizontalScroll(pathScroll)
        )

        LaunchedEffect(file.value?.uri) {
            pathScroll.scrollTo(pathScroll.maxValue)
        }
    }
}

@Composable
private fun TreeScreenEmpty(
    modifier: Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize(),
    ) {
        Text(
            text = "Empty", // FIXME
        )
    }
}

@Composable
private fun TreeScreenStorageList(
    modifier: Modifier,
    fileListState: State<List<FileModel>>,
    onClickItem: (storage: FileModel) -> Unit,
) {
    LazyColumn(
        state = rememberLazyListState(),
        modifier = modifier,
    ) {
        items(
            items = fileListState.value,
            key = { it },
        ) { file ->
            TreeScreenItem(
                file = file,
                onClickItem = onClickItem,
            )
            DividerThin()
        }
    }
}

@Composable
private fun TreeScreenItem(
    file: FileModel,
    onClickItem: (file: FileModel) -> Unit,
) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onClickItem(file) }
            .fillMaxWidth()
            .padding(horizontal = Size.M)
            .heightIn(min = Size.ListItem),
    ) {
        if (file.isDirectory) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_folder),
                contentDescription = file.name,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(Size.IconMiddle),
            )
        } else {
            AsyncImage(
                model = file.uri.uri,
                contentDescription = file.name,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(Size.IconMiddle),
            )
        }
        Column(
            modifier = Modifier
                .padding(start = Size.M)
                .padding(vertical = Size.SS),
        ) {
            Text(
                text = file.name,
                style = Typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = DateUtils.formatDateTime(context, file.dateModified, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME) ,
                style = Typography.bodyLarge,
                maxLines = 1,
                textAlign = TextAlign.End,
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
private fun TreeScreenContainerPreview() {
    StorageImageViewerTheme {
        val storage = StorageModel(
            id = "1",
            uri = UriModel(uri = "content://test1/"),
            name = "Test Storage 1",
            type = StorageType.SAF,
            sortOrder = 1,
        )
        val list = listOf(
            FileModel(
                storage = storage,
                uri = UriModel( "content://test1/"),
                name = "Test directory",
                isDirectory = true,
                mimeType = "",
                size = 0,
                dateModified = 0,
            ),
            FileModel(
                storage = storage,
                uri = UriModel( "content://test2/"),
                name = "Test file.jpg",
                isDirectory = true,
                mimeType = "image/jpeg",
                size = 10000,
                dateModified = 0,
            ),
        )

        TreeScreenContainer(
            snackBarHostState = SnackbarHostState(),
            fileListState = remember { mutableStateOf(list) },
            currentFileState = remember { mutableStateOf(list.getOrNull(0)) },
            sortState = remember { mutableStateOf(SortModel()) },
            busyState = remember { mutableStateOf(false) },
            onSetSort = {},
            onClickItem = {},
            onClickUp = {},
            onClickBack = {},
        )
    }
}
