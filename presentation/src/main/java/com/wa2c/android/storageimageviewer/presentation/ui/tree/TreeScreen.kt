package com.wa2c.android.storageimageviewer.presentation.ui.tree

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.waterfallPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.domain.model.FileModel
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.focusItemStyle
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.toDisplayTreePath
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.toUri
import com.wa2c.android.storageimageviewer.presentation.ui.common.collectIn
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.DividerNormal
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.LoadingBox
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.StorageIcon
import com.wa2c.android.storageimageviewer.presentation.ui.common.forwardingPainter
import com.wa2c.android.storageimageviewer.presentation.ui.common.showMessage
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppSize
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTheme
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeScreenItemData
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeScreenOption
import kotlinx.coroutines.launch

@Composable
fun TreeScreen(
    viewModel: TreeViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val focusManager =  LocalFocusManager.current
    val snackBarHostState = remember { SnackbarHostState() }
    val currentTreeState = viewModel.currentTree.collectAsStateWithLifecycle()
    val focusedFileState = viewModel.focusedFile.collectAsStateWithLifecycle()
    val optionState = viewModel.screenOption.collectAsStateWithLifecycle()
    val busyState = viewModel.busyState.collectAsStateWithLifecycle()
    val inputNumberState = remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .treeKeyControl(
                isPreview = true,
                isLoading = busyState.value,
                onNumber = { number ->
                    inputNumberState.value = (inputNumberState.value ?: "") + number.toString()
                },
                onSearch = {
                    inputNumberState.value = (currentTreeState.value.getImageIndex(focusedFileState.value) + 1).toString()
                }
            ),
    ) {
        // System UI
        SystemUI(optionState, inputNumberState)

        // List
        TreeScreenContainer(
            modifier = Modifier.fillMaxSize(),
            snackBarHostState = snackBarHostState,
            currentTreeState = currentTreeState,
            focusedFileState = focusedFileState,
            optionState = optionState,
            busyState = busyState,
            onSetOption = viewModel::setOption,
            onFocusItem = viewModel::focusFile,
            onClickItem = viewModel::openFile,
            onClickUp = viewModel::openParent,
            onClickBack = onNavigateBack,
        )

        // Viewer
        AnimatedVisibility(
            visible = optionState.value.isViewerMode,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
            ),
            content = {
                TreeScreenViewer()
            },
        )

        // Page Input
        TreeScreenInputNumberDialog(
            inputNumberState = inputNumberState,
            maxPageNumber = currentTreeState.value.imageFileList.size,
        ) { page ->
            viewModel.openPage(page)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.resultState.collectIn(lifecycleOwner) {
            snackBarHostState.showMessage(context, it)
        }
        viewModel.focusedFile.collectIn(lifecycleOwner) {
            if (it == null) {
                focusManager.clearFocus(true)
            }
        }
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

@Composable
@Suppress("DEPRECATION")
private fun SystemUI(
    optionState: State<TreeScreenOption>,
    inputNumberState: State<String?>,
) {
    inputNumberState.value // Force recompose on show number input dialog
    // note: Some Android appear system ui forcibly on show dialog.

    val systemUiController = rememberSystemUiController()
    if (optionState.value.isViewerMode && !optionState.value.viewerOption.showOverlay) {
        systemUiController.isSystemBarsVisible = false
        systemUiController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    } else {
        systemUiController.isSystemBarsVisible = true
        systemUiController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TreeScreenContainer(
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState,
    currentTreeState: State<TreeScreenItemData>,
    focusedFileState: State<FileModel?>,
    optionState: State<TreeScreenOption>,
    busyState: State<Boolean>,
    onSetOption: (TreeScreenOption) -> Unit,
    onFocusItem: (FileModel?) -> Unit,
    onClickItem: (FileModel) -> Unit,
    onClickUp: () -> Unit,
    onClickBack: () -> Unit,
) {
    val menuExpanded = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        currentTreeState.value.currentFolder?.let { dir ->
                            StorageIcon(
                                storage = dir.storage,
                                modifier = Modifier
                                    .size(AppSize.IconSmall)
                            )
                        }
                        Text(
                            text = currentTreeState.value.currentFolder?.storage?.name ?: "",
                            maxLines = 1,
                            modifier = Modifier
                                .padding(start = AppSize.S)
                                .basicMarquee(),
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClickBack,
                        modifier = Modifier
                            .focusItemStyle(shape = CircleShape),
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_back),
                            contentDescription = "back",
                        )
                    }
                },
                actions = {
                    TreeScreenMenu(
                        menuExpanded = menuExpanded,
                        optionState = optionState,
                        onSetOption = onSetOption,
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
        modifier = Modifier
            .treeKeyControl(
                isPreview = true,
                isLoading = busyState.value,
                onMenu = {
                    menuExpanded.value = true
                }
            ),
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .displayCutoutPadding()
                .waterfallPadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                if (currentTreeState.value.fileList.isEmpty()) {
                    TreeScreenEmpty(
                        showText = currentTreeState.value.routeList.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f),
                    )
                } else {
                    TreeScreenItems(
                        modifier = Modifier
                            .weight(1f),
                        currentTreeState = currentTreeState,
                        focusedFileState = focusedFileState,
                        optionState = optionState,
                        onFocusItem = onFocusItem,
                        onClickItem = onClickItem,
                    )
                }

                DividerNormal()

                TreeScreenControlBar(
                    tree = currentTreeState,
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
private fun TreeScreenItems(
    modifier: Modifier,
    currentTreeState: State<TreeScreenItemData>,
    focusedFileState: State<FileModel?>,
    optionState: State<TreeScreenOption>,
    onFocusItem: (FileModel?) -> Unit,
    onClickItem: (FileModel) -> Unit,
) {
    val contentColor = LocalContentColor.current
    val staticVector = rememberVectorPainter(ImageVector.vectorResource(R.drawable.ic_image))
    val staticImage = remember {
        forwardingPainter(
            painter = staticVector,
            colorFilter = ColorFilter.tint(contentColor),
            alpha = 0.5f,
        )
    }

    val focusedIndex = remember { fun(): Int? {
        val list = currentTreeState.value.fileList.ifEmpty { return null }
        return list.indexOf(focusedFileState.value)
    } }

    val targetIndexState = remember { mutableStateOf(focusedIndex()) }

    if (optionState.value.treeOption.viewType.isList) {
        TreeScreenLazyList(
            modifier = modifier,
            staticImage = staticImage,
            currentTreeState = currentTreeState,
            targetIndexState = targetIndexState,
            optionState = optionState,
            onFocusItem = {
                targetIndexState.value = null
                onFocusItem(it)
            },
            onClickItem = onClickItem,
        )
    } else {
        TreeScreenLazyGrid(
            modifier = modifier,
            staticImage = staticImage,
            currentTreeState = currentTreeState,
            targetIndexState = targetIndexState,
            optionState = optionState,
            onFocusItem = {
                targetIndexState.value = null
                onFocusItem(it)
            },
            onClickItem = onClickItem,
        )
    }

    LaunchedEffect(Unit) {
        launch {
            snapshotFlow { currentTreeState.value.fileList }.collect { value ->
                if (!optionState.value.isViewerMode) {
                    targetIndexState.value = value.indexOf(focusedFileState.value)
                }
            }
        }
        launch {
            snapshotFlow { optionState.value.isViewerMode }.collect { _ ->
                if (!optionState.value.isViewerMode) {
                    targetIndexState.value = currentTreeState.value.fileList.indexOf(focusedFileState.value)
                }
            }
        }
    }
}


@Composable
private fun TreeScreenControlBar(
    tree: State<TreeScreenItemData>,
    onClickUp: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = AppSize.S),
    ) {
        val pathScroll = rememberScrollState()
        IconButton(
            onClick = onClickUp,
            enabled = !tree.value.isRoot,
            modifier = Modifier
                .focusItemStyle()
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_folder_up),
                contentDescription = "Up",
                modifier = Modifier
                    .size(AppSize.IconMiddle)
            )
        }

        val context = LocalContext.current
        val path = tree.value.currentFolder?.let {
            val storagePath = it.storage.rootUri.toUri().toDisplayTreePath(context)
            val currentPath = it.uri.toUri().toDisplayTreePath(context)
            currentPath?.substringAfter(storagePath ?: "") ?: ""
        } ?: ""

        Text(
            text = path,
            maxLines = 1,
            modifier = Modifier
                .padding(start = AppSize.S)
                .weight(1f)
                .horizontalScroll(pathScroll),
        )

        LaunchedEffect(tree.value.currentFolder) {
            pathScroll.scrollTo(pathScroll.maxValue)
        }
    }
}

@Composable
private fun TreeScreenEmpty(
    showText: Boolean,
    modifier: Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize(),
    ) {
        if (showText) {
            Text(
                text = stringResource(R.string.tree_empty_label),
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
    AppTheme {
        val storage = StorageModel(
            id = "1",
            name = "Test Storage 1",
            uri = UriModel(uri = "content://test1/"),
            rootUri = UriModel(uri = "content://test1/"),
            type = StorageType.SAF,
            sortOrder = 1,
        )
        val dir = FileModel(
            storage = storage,
            uri = UriModel( "content://dir1/"),
            isDirectory = true,
            name = "Test directory ",
            mimeType = "",
            size = 0,
            dateModified = 0,
        )

        val list = listOf(
            FileModel(
                storage = storage,
                uri = UriModel( "content://test1/"),
                isDirectory = true,
                name = "Test directory",
                mimeType = "",
                size = 0,
                dateModified = 1000000000000,
            ),
            FileModel(
                storage = storage,
                uri = UriModel( "content://test2/image1.jpg"),
                isDirectory = false,
                name = "image1.jpg",
                mimeType = "image/jpeg",
                size = 10000,
                dateModified = 1500000000000,
            ),
        )

        TreeScreenContainer(
            snackBarHostState = SnackbarHostState(),
            currentTreeState = remember { mutableStateOf(TreeScreenItemData(listOf(dir), list)) },
            focusedFileState = remember { mutableStateOf(null) },
            optionState = remember { mutableStateOf(TreeScreenOption()) },
            busyState = remember { mutableStateOf(false) },
            onSetOption = {},
            onFocusItem = {},
            onClickItem = {},
            onClickUp = {},
            onClickBack = {},
        )
    }
}
