package com.wa2c.android.storageimageviewer.presentation.ui.tree

import android.content.res.Configuration
import android.text.format.DateUtils
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.domain.model.FileModel
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.DividerThin
import com.wa2c.android.storageimageviewer.presentation.ui.common.LoadingBox
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Size
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.StorageImageViewerTheme
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Typography

@Composable
fun TreeScreen(
    viewModel: TreeViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onNavigateViewer: (fileList: List<FileModel>, selectedFile: FileModel) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val fileListState = viewModel.currentList.collectAsStateWithLifecycle()
    val busyState = viewModel.busyState.collectAsStateWithLifecycle()
    val resultState = viewModel.resultState.collectAsStateWithLifecycle()

    TreeScreenContainer(
        snackBarHostState = snackBarHostState,
        fileListState = fileListState,
        busyState = busyState,
        onClickItem = {},
        onClickBack = onNavigateBack,
    )

    LaunchedEffect(resultState) {
        resultState.value.exceptionOrNull()?.let {
            snackBarHostState.showSnackbar(it.message ?: it.toString())
            onNavigateBack()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TreeScreenContainer(
    snackBarHostState: SnackbarHostState,
    fileListState: State<List<FileModel>>,
    busyState: State<Boolean>,
    onClickItem: (FileModel) -> Unit,
    onClickBack: () -> Unit,
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
            .fillMaxWidth()
            .padding(horizontal = Size.M, vertical = Size.SS)
            .heightIn(min = Size.ListItem)
            .clickable { onClickItem(file) }
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(
                if (file.isDirectory) R.drawable.ic_folder else R.drawable.ic_image
            ),
            contentDescription = file.name,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(Size.IconMiddle)
        )
        Column(
            modifier = Modifier
                .padding(start = Size.M)
        ) {
            Text(
                text = file.name,
                style = Typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = DateUtils.formatDateTime(context, file.dateModified, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME) ,
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
private fun HomeScreenContainerPreview() {
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
                path = "path1",
                name = "Test directory",
                isDirectory = true,
                mimeType = "",
                size = 0,
                dateModified = 0,
            ),
            FileModel(
                storage = storage,
                path = "path2",
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
