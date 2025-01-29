package com.wa2c.android.storageimageviewer.presentation.ui.tree

import android.content.res.Configuration
import android.provider.DocumentsContract
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.common.values.TreeViewType
import com.wa2c.android.storageimageviewer.domain.model.FileModel
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.model.TreeDataModel
import com.wa2c.android.storageimageviewer.domain.model.TreeSortModel
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.DividerNormal
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.LoadingBox
import com.wa2c.android.storageimageviewer.presentation.ui.common.showMessage
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Size
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.StorageImageViewerTheme

@Composable
fun TreeScreen(
    viewModel: TreeViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onNavigateBack: () -> Unit,
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val currentTreeState = viewModel.currentTree.collectAsStateWithLifecycle()
    val focusedFileState = viewModel.focusedFile.collectAsStateWithLifecycle()
    val isViewerModeState = viewModel.isViewerMode.collectAsStateWithLifecycle()
    val viewState = viewModel.viewState.collectAsStateWithLifecycle()
    val sortState = viewModel.sortState.collectAsStateWithLifecycle()
    val busyState = viewModel.busyState.collectAsStateWithLifecycle()
    val resultState = viewModel.resultState.collectAsStateWithLifecycle()
    val inputNumberState = remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .treeKeyControl(
                isPreview = true,
                onNumber = { number ->
                    inputNumberState.value = (inputNumberState.value ?: "") + number.toString()
                },
                onSearch = {
                    inputNumberState.value = (currentTreeState.value.imageFileList.indexOf(focusedFileState.value) + 1).toString()
                }
            ),
    ) {
        TreeScreenContainer(
            modifier = Modifier.fillMaxSize(),
            snackBarHostState = snackBarHostState,
            currentTreeState = currentTreeState,
            focusedFileState = focusedFileState,
            isViewerModeState = isViewerModeState,
            viewState = viewState,
            sortState = sortState,
            busyState = busyState,
            onSetView = viewModel::setView,
            onSetSort = viewModel::sortFile,
            onFocusItem = viewModel::focusFile,
            onClickItem = viewModel::openFile,
            onClickUp = viewModel::openParent,
            onClickBack = onNavigateBack,
            onStep = {
                viewModel.focusPage(it)
            }
        )

        AnimatedVisibility(
            visible = isViewerModeState.value,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
            ),
            content = {
                TreeScreenViewer(
                    onClose = viewModel::closeViewer,
                )
            },
        )

        TreeScreenInputNumberDialog(
            inputNumberState = inputNumberState,
            maxPageNumber = currentTreeState.value.imageFileList.size,
        ) { page ->
            viewModel.openPage(page)
        }
    }

    LaunchedEffect(resultState) {
        snackBarHostState.showMessage(resultState.value)
    }

    // Back button
    BackHandler {
        if (busyState.value) {
            viewModel.cancelLoading()
        } else if (!viewModel.isRoot) {
            viewModel.openParent()
        } else {
            onNavigateBack()
        }
    }
}


private fun Modifier.keyControl(
    onStepPage: (step: Int) -> Unit,
    onShowMenu: () -> Unit
): Modifier {
    return this.treeKeyControl(
        isPreview = true,
//        onEnter = onShowOverlay,
//        onPlay = onZoom,
//        onDirectionUp = { isShift ->
//            onStepPage(-1)
//        },
//        onDirectionDown = { isShift ->
//            onStepPage(+1)
//        },
        onDirectionLeft = {
            onStepPage(-10)
        },
        onDirectionRight = {
            onStepPage(+10)
        },
        onForward = { onStepPage(+1) },
        onBackward = { onStepPage(-1) },
        onForwardSkip = { onStepPage(+10) },
        onBackwardSkip = { onStepPage(-10) },
        onForwardLast = { onStepPage(Int.MAX_VALUE) },
        onBackwardFirst = { onStepPage(-Int.MAX_VALUE) },
        //onNumber = {},
        //onDelete = {},
        onMenu = onShowMenu,
        //onSearch = {},
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TreeScreenContainer(
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState,
    currentTreeState: State<TreeDataModel>,
    focusedFileState: State<FileModel?>,
    isViewerModeState: State<Boolean>,
    viewState: State<TreeViewType>,
    sortState: State<TreeSortModel>,
    busyState: State<Boolean>,
    onSetView: (TreeViewType) -> Unit,
    onSetSort: (TreeSortModel) -> Unit,
    onFocusItem: (FileModel?) -> Unit,
    onClickItem: (FileModel) -> Unit,
    onClickUp: () -> Unit,
    onClickBack: () -> Unit,
    onStep: (step: Int) -> Unit,
) {
    val viewMenuExpanded = remember { mutableStateOf(false) }
    val sortMenuExpanded = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentTreeState.value.dir?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onClickBack) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    TreeViewAction(
                        menuExpanded = viewMenuExpanded,
                        viewState = viewState,
                        onSetView = onSetView,
                        onKeyRight = {
                            viewMenuExpanded.value = false
                            sortMenuExpanded.value = true
                        }
                    )
                    TreeSortAction(
                        menuExpanded = sortMenuExpanded,
                        sortState = sortState,
                        onSetSort = onSetSort,
                        onKeyLeft = {
                            sortMenuExpanded.value = false
                            viewMenuExpanded.value = true
                        }
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
        modifier = Modifier
            .treeKeyControl(
                isPreview = true,
                onMenu = {
                    sortMenuExpanded.value = true
                }
            ),
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                if (currentTreeState.value.fileList.isEmpty()) {
                    TreeScreenEmpty(
                        modifier = Modifier
                            .weight(1f),
                    )
                } else {
                    if (viewState.value.isList) {
                        TreeScreenLazyList(
                            isViewerModeState = isViewerModeState,
                            modifier = Modifier
                                .weight(1f),
                            currentTreeState = currentTreeState,
                            focusedFileState = focusedFileState,
                            viewState = viewState,
                            onFocusItem = onFocusItem,
                            onClickItem = onClickItem,
                        )
                    } else {
                        TreeScreenLazyGrid(
                            isViewerModeState = isViewerModeState,
                            modifier = Modifier
                                .weight(1f),
                            currentTreeState = currentTreeState,
                            focusedFileState = focusedFileState,
                            viewState = viewState,
                            onFocusItem = onFocusItem,
                            onClickItem = onClickItem,
                        )
                    }
                }

                DividerNormal()

                TreeScreenControlBar(
                    dir = currentTreeState.value.dir,
                    onClickUp = onClickUp,
                )
            }
            LoadingBox(
                isLoading = busyState.value,
            )
        }
    }
}

@Composable
private fun TreeScreenControlBar(
    dir: FileModel?,
    onClickUp: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = Size.S),
    ) {
        val pathScroll = rememberScrollState()
        IconButton(
            onClick = onClickUp,
            modifier = Modifier
                .size(Size.IconMiddle),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_folder_up),
                contentDescription = "Up",
            )
        }

        val context = LocalContext.current
        val resolver = LocalContext.current.contentResolver
        val path = dir?.uri?.uri?.toUri()?.let {
            if (DocumentsContract.isDocumentUri(context, it)) DocumentsContract.findDocumentPath(resolver, it)?.path?.lastOrNull()
            else ""
        }

        Text(
            text = path ?: "",
            maxLines = 1,
            modifier = Modifier
                .padding(start = Size.SS)
                .weight(1f)
                .horizontalScroll(pathScroll),
        )

        LaunchedEffect(dir?.uri) {
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
        val dir = FileModel(
            storage = storage,
            uri = UriModel( "content://dir1/"),
            name = "Test directory ",
            isDirectory = true,
            mimeType = "",
            size = 0,
            dateModified = 0,
        )

        val list = listOf(
            FileModel(
                storage = storage,
                uri = UriModel( "content://test1/"),
                name = "Test directory",
                isDirectory = true,
                mimeType = "",
                size = 0,
                dateModified = 1000000000000,
            ),
            FileModel(
                storage = storage,
                uri = UriModel( "content://test2/image1.jpg"),
                name = "image1.jpg",
                isDirectory = false,
                mimeType = "image/jpeg",
                size = 10000,
                dateModified = 1500000000000,
            ),
        )

        TreeScreenContainer(
            snackBarHostState = SnackbarHostState(),
            currentTreeState = remember { mutableStateOf(TreeDataModel(dir, list)) },
            focusedFileState = remember { mutableStateOf(null) },
            isViewerModeState = remember { mutableStateOf(false) },
            sortState = remember { mutableStateOf(TreeSortModel()) },
            viewState = remember { mutableStateOf(TreeViewType.ListSmall) },
            busyState = remember { mutableStateOf(false) },
            onSetView = {},
            onSetSort = {},
            onFocusItem = {},
            onClickItem = {},
            onClickUp = {},
            onClickBack = {},
            onStep = {},
        )
    }
}
