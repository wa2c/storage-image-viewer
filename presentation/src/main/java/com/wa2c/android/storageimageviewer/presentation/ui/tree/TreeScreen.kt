package com.wa2c.android.storageimageviewer.presentation.ui.tree

import android.content.res.Configuration
import android.text.format.DateUtils
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.domain.model.FileModel
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.DividerThin
import com.wa2c.android.storageimageviewer.presentation.ui.common.LoadingBox
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
    val snackBarHostState = remember { SnackbarHostState() }
    val fileListState = viewModel.currentList.collectAsStateWithLifecycle()
    val viewerFileState = viewModel.viewerFile.collectAsStateWithLifecycle()
    val busyState = viewModel.busyState.collectAsStateWithLifecycle()
    val resultState = viewModel.resultState.collectAsStateWithLifecycle()

    Box {
        TreeScreenContainer(
            modifier = Modifier.fillMaxSize(),
            snackBarHostState = snackBarHostState,
            fileListState = fileListState,
            busyState = busyState,
            onClickItem = viewModel::openFile,
            onClickBack = onNavigateBack,
        )
        TreeScreenViewer(
            modifier = Modifier.fillMaxSize(),
            viewerFileState = viewerFileState,
            fileListState = fileListState,
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
    busyState: State<Boolean>,
    onClickItem: (FileModel) -> Unit,
    onClickBack: () -> Unit,
) {
    Box(
        modifier = modifier
    ) {
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
                )
            },
            snackbarHost = { SnackbarHost(snackBarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                TreeScreenStorageList(
                    fileListState = fileListState,
                    onClickItem = onClickItem,
                )
                LoadingBox(
                    isLoading = busyState.value
                )
            }
        }
    }

}

@Composable
private fun TreeScreenStorageList(
    fileListState: State<List<FileModel>>,
    onClickItem: (storage: FileModel) -> Unit,
) {
    LazyColumn(
        state = rememberLazyListState(),
        modifier = Modifier,
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
            .heightIn(min = Size.ListItem)
    ) {
        if (file.isDirectory) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_folder),
                contentDescription = file.name,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(Size.IconMiddle)
            )
        } else {
            AsyncImage(
                model = file.uri.uri,
                contentDescription = file.name,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(Size.IconMiddle)
            )
        }
        Column(
            modifier = Modifier
                .padding(start = Size.M)
                .padding(vertical = Size.SS)
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
            busyState = remember { mutableStateOf(false) },
            onClickItem = {},
            onClickBack = {},
        )
    }
}
