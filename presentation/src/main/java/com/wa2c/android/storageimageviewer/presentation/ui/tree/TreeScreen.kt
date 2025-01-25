package com.wa2c.android.storageimageviewer.presentation.ui.tree

import android.content.res.Configuration
import android.provider.DocumentsContract
import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.wa2c.android.storageimageviewer.common.utils.Log
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.domain.model.FileModel
import com.wa2c.android.storageimageviewer.domain.model.SortModel
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.model.TreeData
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.DividerNormal
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.DividerThin
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.LoadingBox
import com.wa2c.android.storageimageviewer.presentation.ui.common.showMessage
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Color
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Size
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.StorageImageViewerTheme
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val sortState = viewModel.sortState.collectAsStateWithLifecycle()
    val busyState = viewModel.busyState.collectAsStateWithLifecycle()
    val resultState = viewModel.resultState.collectAsStateWithLifecycle()
    val inputNumber = remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

                keyEvent.key.toNumber()?.let {
                    inputNumber.value = (inputNumber.value ?: "") + it.toString()
                    return@onPreviewKeyEvent true
                }

                false
            },
    ) {
        TreeScreenContainer(
            modifier = Modifier.fillMaxSize(),
            snackBarHostState = snackBarHostState,
            currentTreeState = currentTreeState,
            focusedFileState = focusedFileState,
            isViewerModeState = isViewerModeState,
            sortState = sortState,
            busyState = busyState,
            onSetSort = viewModel::sortFile,
            onFocusItem = viewModel::focusFile,
            onClickItem = viewModel::openFile,
            onClickUp = viewModel::openParent,
            onClickBack = onNavigateBack,
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

        val number = inputNumber.value
        if (!number.isNullOrEmpty()) {
            InputNumber(
                initialNumber = number,
                onSetNumber = { num ->
                    val imageFileList = currentTreeState.value.imageFileList
                    num.toIntOrNull()?.let {
                        imageFileList.getOrNull((it - 1).coerceIn(0..<imageFileList.size)) ?.let { file ->
                            viewModel.openFile(file)
                        }
                    }
                    inputNumber.value = null
                },
                onDismiss = {
                    inputNumber.value = null
                }
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TreeScreenContainer(
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState,
    currentTreeState: State<TreeData>,
    focusedFileState: State<FileModel?>,
    isViewerModeState: State<Boolean>,
    sortState: State<SortModel>,
    busyState: State<Boolean>,
    onSetSort: (SortModel) -> Unit,
    onFocusItem: (FileModel?) -> Unit,
    onClickItem: (FileModel) -> Unit,
    onClickUp: () -> Unit,
    onClickBack: () -> Unit,
) {
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
                    TreeSortAction(
                        menuExpanded = sortMenuExpanded,
                        sortState = sortState,
                        onSetSort = onSetSort,
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
        modifier = Modifier
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (keyEvent.key) {
                    Key.Menu -> {
                        sortMenuExpanded.value = true
                        true
                    }

                    else -> {
                        false
                    }
                }
            },
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
                    TreeScreenStorageList(
                        isViewerModeState = isViewerModeState,
                        modifier = Modifier
                            .weight(1f),
                        currentTreeState = currentTreeState,
                        focusedFileState = focusedFileState,
                        onFocusItem = onFocusItem,
                        onClickItem = onClickItem,
                    )
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TreeScreenStorageList(
    isViewerModeState: State<Boolean>,
    modifier: Modifier,
    currentTreeState: State<TreeData>,
    focusedFileState: State<FileModel?>,
    onFocusItem: (FileModel?) -> Unit,
    onClickItem: (FileModel) -> Unit,
) {
    val parentFocusRequester = remember { FocusRequester() }
    val focusRequester = remember { FocusRequester() }
    val lazyListState = rememberLazyListState()
    val focusedFile = focusedFileState.value

    LazyColumn(
        modifier = modifier
            .focusRequester(parentFocusRequester)
            .focusProperties {
                exit = { FocusRequester.Default }
                enter = { focusRequester }
            },
        state = lazyListState,
    ) {
        val fileList = currentTreeState.value.fileList
        val focusIndex = fileList.indexOf(focusedFile).takeIf { it >= 0 } ?: 0
        itemsIndexed(
            items = fileList,
        ) { index, file ->
            TreeScreenItem(
                modifier = Modifier
                    .onFocusChanged {
                        if (it.isFocused) {
                            onFocusItem(file)
                        }
                        // else { onFocusItem(null) } NOTE: keep focus
                    }
                    .let {
                        if (index == focusIndex) it.focusRequester(focusRequester)
                        else it
                    },
                treeData = currentTreeState.value,
                file = file,
                onClickItem = onClickItem,
            )
            DividerThin()
        }
    }

    LaunchedEffect(currentTreeState.value.fileList) {
        restoreFocus(
            isViewerModeState = isViewerModeState,
            currentTreeState = currentTreeState,
            focusedFileState = focusedFileState,
            lazyListState = lazyListState,
            parentFocusRequester = parentFocusRequester,
            focusRequester = focusRequester,
        )
    }

    LaunchedEffect(isViewerModeState.value) {
        restoreFocus(
            isViewerModeState = isViewerModeState,
            currentTreeState = currentTreeState,
            focusedFileState = focusedFileState,
            lazyListState = lazyListState,
            parentFocusRequester = parentFocusRequester,
            focusRequester = focusRequester,
        )
    }
}

/**
 * Restore focus
 */
private suspend fun restoreFocus(
    isViewerModeState: State<Boolean>,
    currentTreeState: State<TreeData>,
    focusedFileState: State<FileModel?>,
    lazyListState: LazyListState,
    parentFocusRequester: FocusRequester,
    focusRequester: FocusRequester,
) {
    val list = currentTreeState.value.fileList
    if (list.isNotEmpty() && !isViewerModeState.value) {
        val index = list.indexOf(focusedFileState.value)
        if (index >= 0) {
            val listHeight = lazyListState.layoutInfo.viewportEndOffset
            val itemHeight = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0
            val offset = (listHeight.toFloat() / 2)  - (itemHeight.toFloat() / 2)
            lazyListState.scrollToItem(index, -offset.toInt())
        } else {
            lazyListState.scrollToItem(0, 0)
        }

        try {
            parentFocusRequester.requestFocus()
            focusRequester.requestFocus()
        } catch (e: Exception) {
            Log.e(e)
        }
    }
}

@Composable
private fun TreeScreenItem(
    modifier: Modifier,
    treeData: TreeData,
    file: FileModel,
    onClickItem: (file: FileModel) -> Unit,
) {
    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable { onClickItem(file) }
            .fillMaxWidth()
            .padding(horizontal = Size.ScreenMargin)
            .heightIn(min = Size.ListItem),
    ) {
        Box(
            contentAlignment = Alignment.CenterEnd,
            modifier = Modifier
                .padding(end = Size.S),
        ){
            val number = if (file.isDirectory) "" else (treeData.imageFileList.indexOf(file) + 1).toString()

            Text(
                text = number,
                style = Typography.labelLarge,
            )
            // Dummy for width
            Text(
                text = treeData.dummyDigit,
                style = Typography.labelLarge,
                modifier = Modifier
                    .alpha(0f),
            )
        }

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
                modifier = Modifier
                    .fillMaxWidth()
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                if (!file.isDirectory) {
                    Text(
                        text = Formatter.formatFileSize(context, file.size),
                        style = Typography.bodyLarge,
                        maxLines = 1,
                        textAlign = TextAlign.End,
                    )
                }
                Text(
                    text = dateFormatter.format(Date(file.dateModified)),
                    style = Typography.bodyLarge,
                    maxLines = 1,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .padding(start = Size.S)
                )
            }
        }
    }
}

@Composable
private fun InputNumber(
    initialNumber: String,
    onSetNumber: (String) -> Unit,
    onDismiss: () -> Unit = {},
) {
    var number by remember { mutableStateOf(initialNumber) }
    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        Surface {
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier
                    .clip(RoundedCornerShape(Size.S))
                    .background(Color.NumberInputBackground)
                    .padding(Size.S)
                    .widthIn(min = 100.dp)
                    .focusable()
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false

                        keyEvent.key.toNumber()?.let {
                            number += it
                            return@onKeyEvent true
                        }

                        when (keyEvent.key) {
                            Key.Backspace,
                            Key.Delete -> {
                                number = number.dropLast(1)
                                return@onKeyEvent true
                            }
                            Key.Enter,
                            Key.NumPadEnter,
                            Key.DirectionCenter,
                            Key.Spacebar -> {
                                onSetNumber(number)
                                return@onKeyEvent true
                            }
                        }

                        false
                    },
            ) {
                Text(
                    text = number,
                    style = Typography.titleLarge,
                    modifier = Modifier
                )
            }
        }
    }

    LaunchedEffect(number) {
        launch {
            delay(3000)
            onDismiss()
        }
    }
}

private fun Key.toNumber(): Char? {
    return when (this) {
        Key.Zero, Key.NumPad0 -> '0'
        Key.One, Key.NumPad1 -> '1'
        Key.Two, Key.NumPad2 -> '2'
        Key.Three, Key.NumPad3 -> '3'
        Key.Four, Key.NumPad4 -> '4'
        Key.Five, Key.NumPad5 -> '5'
        Key.Six, Key.NumPad6 -> '6'
        Key.Seven, Key.NumPad7 -> '7'
        Key.Eight, Key.NumPad8 -> '8'
        Key.Nine, Key.NumPad9 -> '9'
        else -> null
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
            name = "Test directory",
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
            currentTreeState = remember { mutableStateOf(TreeData(dir, list)) },
            focusedFileState = remember { mutableStateOf(null) },
            isViewerModeState = remember { mutableStateOf(false) },
            sortState = remember { mutableStateOf(SortModel()) },
            busyState = remember { mutableStateOf(false) },
            onSetSort = {},
            onFocusItem = {},
            onClickItem = {},
            onClickUp = {},
            onClickBack = {},
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
private fun TreeScreenInputNumberPreview() {
    StorageImageViewerTheme {
        InputNumber(
            initialNumber = "123",
            onSetNumber = {},
            onDismiss = {},
        )
    }
}
