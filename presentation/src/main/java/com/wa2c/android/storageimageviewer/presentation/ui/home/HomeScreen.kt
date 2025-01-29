package com.wa2c.android.storageimageviewer.presentation.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wa2c.android.storageimageviewer.common.utils.Utils
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.DividerThin
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.toUri
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.toUriModel
import com.wa2c.android.storageimageviewer.presentation.ui.common.ValueResource.drawableResId
import com.wa2c.android.storageimageviewer.presentation.ui.common.dialog.CommonDialog
import com.wa2c.android.storageimageviewer.presentation.ui.common.dialog.DialogButton
import com.wa2c.android.storageimageviewer.presentation.ui.common.showMessage
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Size
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.StorageImageViewerTheme
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Typography
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onSelectStorage: (storage: StorageModel) -> Unit,
) {

    val snackBarHostState = remember { SnackbarHostState() }
    val resolver = LocalContext.current.contentResolver
    val storageListState = viewModel.storageList.collectAsStateWithLifecycle()
    val editStorage = remember { mutableStateOf<StorageModel?>(null) }
    val resultState = viewModel.resultState.collectAsStateWithLifecycle()

    val treeOpenLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri == null) {
            // todo message
            return@rememberLauncherForActivityResult
        }
        editStorage.value?.let {
            editStorage.value = it.copy(uri = uri.toUriModel())
        }
    }

    // Register ActivityResult handler
    val requestPermissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        //todo message
    }


    HomeScreenContainer(
        snackBarHostState = snackBarHostState,
        storageListState = storageListState,
        onClickAdd = {
            editStorage.value = StorageModel(
                id = Utils.generateUUID(),
                uri = UriModel(uri = ""),
                name = "",
                type = StorageType.SAF,
                sortOrder = 0,
            )
        },
        onClickEdit = { storage ->
            if (storage.type != StorageType.SAF) return@HomeScreenContainer
            editStorage.value = storage
        },
        onClickItem = onSelectStorage,
        onDragAndDrop = viewModel::onItemMove,
    )

    // Edit Dialog
    HomeScreenStorageEditDialog(
        editStorage = editStorage,
        onClickUri = { uri ->
            treeOpenLauncher.launch(uri.toUri())
        },
        onClickSet = { storage ->
            if (storage.uri.isInvalidUri) {
                // todo message
            } else if (storage.name.isEmpty()) {
                // todo message
            } else {
                resolver.takePersistableUriPermission(
                    storage.uri.toUri() ?: return@HomeScreenStorageEditDialog,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                viewModel.setStorage(storage)
                editStorage.value = null
            }
        },
        onDismiss = {
            editStorage.value = null
        }
    )



    LaunchedEffect(Unit) {
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            requestPermissions.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    LaunchedEffect(resultState) {
        snackBarHostState.showMessage(resultState.value)
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContainer(
    snackBarHostState: SnackbarHostState,
    storageListState: State<List<StorageModel>>,
    onClickAdd: () -> Unit,
    onClickEdit: (storage: StorageModel) -> Unit,
    onClickItem: (storage: StorageModel) -> Unit,
    onDragAndDrop: (from: Int, to: Int) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                navigationIcon = {
                    // todo
                }
            )
        },
        floatingActionButton = {
            SmallFloatingActionButton(
                onClick = onClickAdd,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_folder_add),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 8.dp),
                )
            }
        },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HomeScreenStorageList(
                storageListState = storageListState,
                onClickItem = onClickItem,
                onClickEdit = onClickEdit,
                onDragAndDrop = onDragAndDrop,
            )
        }
    }
}

@Composable
private fun HomeScreenStorageList(
    storageListState: State<List<StorageModel>>,
    onClickItem: (storage: StorageModel) -> Unit,
    onClickEdit: (storage: StorageModel) -> Unit,
    onDragAndDrop: (from: Int, to: Int) -> Unit,
) {
    val state = rememberReorderableLazyListState(onMove = { from, to ->
        onDragAndDrop(from.index, to.index)
    })
    LazyColumnScrollbar(
        state = state.listState,
        settings = ScrollbarSettings.Default
    ) {
        LazyColumn(
            state = state.listState,
            modifier = Modifier
                .reorderable(state)
                .detectReorderAfterLongPress(state)
        ) {
            items(
                items = storageListState.value,
                key = { it.id },
            ) { storage ->
                ReorderableItem(state, key = storage) { isDragging ->

                    val elevation = animateDpAsState(if (isDragging) Size.S else 0.dp, label = "")
                    HomeScreenStorageItem(
                        storage = storage,
                        modifier = Modifier
                            .shadow(elevation.value)
                            .background(MaterialTheme.colorScheme.surface),
                        onClickItem = onClickItem,
                        onClickEdit = onClickEdit,
                    )
                }
                DividerThin()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeScreenStorageItem(
    storage: StorageModel,
    modifier: Modifier = Modifier,
    onClickItem: (storage: StorageModel) -> Unit,
    onClickEdit: (storage: StorageModel) -> Unit,
) {
    val context = LocalContext.current
    val granted = when (storage.type) {
        StorageType.Device,
        StorageType.SD -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        StorageType.External,
        StorageType.SAF -> {
            context.checkCallingOrSelfUriPermission(storage.uri.toUri(), Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }.let { permission ->
        permission == PackageManager.PERMISSION_GRANTED
    }
    //var color by remember { mutableStateOf(Color.Green) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .let {
                if (storage.type == StorageType.SAF) {
                    it.combinedClickable(
                        onClick = { onClickItem(storage) },
                        onLongClick = { onClickEdit(storage) },
                    )
                } else {
                    it.clickable { onClickItem(storage) }
                }
            }
            .fillMaxWidth()
            //.background(color=  color)
            .padding(horizontal = Size.M, vertical = Size.SS)
            .heightIn(min = Size.ListItem)
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(storage.type.drawableResId()),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(Size.IconMiddle)
        )
        Column(
            modifier = Modifier
                .padding(start = Size.M)
        ) {
            Text(
                text = storage.name,
                style = Typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val subText = if (granted) {
                storage.uri.uri
            } else {
                "Not granted" // FIXME
            }

            Text(
                text = subText,
                style = Typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun HomeScreenStorageEditDialog(
    editStorage: MutableState<StorageModel?>,
    onClickUri: (uri: UriModel) -> Unit,
    onClickSet: (storage: StorageModel) -> Unit,
    onDismiss: () -> Unit,
) {
    val storage = editStorage.value ?: return

    CommonDialog(
        title = "Edit",
        confirmButtons = listOf(
            DialogButton(
                label = "Save", // FIXME
                onClick = {
                    onClickSet(storage)
                }
            )
        ),
        dismissButton = DialogButton(
            label = "Cancel", // FIXME
            onClick = onDismiss,
        ),
        onDismiss = onDismiss
    ) {
        Column {
            // Name
            OutlinedTextField(
                value = storage.name,
                label = { Text("Name") }, // fixme
                placeholder = { Text("Input name") }, // fixme
                onValueChange = { value ->
                    editStorage.value = storage.copy(name = value)
                },
                maxLines = 1,
                singleLine = true,
            )

            // URI
            Box {
                OutlinedTextField(
                    value = storage.uri.uri,
                    label = { Text("URI") }, // fixme
                    placeholder = { Text("Select URI") }, // fixme
                    readOnly = true,
                    onValueChange = { value ->
                        editStorage.value = storage.copy(uri = UriModel(value))
                    },
                    maxLines = 1,
                    singleLine = true,
                    modifier = Modifier
                        .padding(top = Size.M)
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(top = Size.L)
                        .clip(RoundedCornerShape(Size.SS))
                        .clickable { onClickUri(storage.uri) }
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
private fun HomeScreenContainerPreview() {
    StorageImageViewerTheme {
        val storageList = listOf(
            StorageModel(
                id = "1",
                uri = UriModel(uri = "content://test1/"),
                name = "Test Storage 1",
                type = StorageType.SAF,
                sortOrder = 1,
            ),
            StorageModel(
                id = "2",
                uri = UriModel(uri = "content://test2/"),
                name = "Test Storage 2",
                type = StorageType.Device,
                sortOrder = 2,
            ),
        )

        HomeScreenContainer(
            snackBarHostState = remember { SnackbarHostState() },
            storageListState = remember { mutableStateOf(storageList) },
            onClickAdd = {},
            onClickEdit = {},
            onClickItem = {},
            onDragAndDrop = { _, _ -> },
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
private fun HomeScreenStorageEditDialogPreview() {
    StorageImageViewerTheme {
        val storage = StorageModel(
            id = "1",
            uri = UriModel(uri = "content://test1/"),
            name = "Test Storage 1",
            type = StorageType.SAF,
            sortOrder = 1,
        )

        HomeScreenStorageEditDialog(
            editStorage = remember { mutableStateOf(storage) },
            onClickUri = {},
            onClickSet = {},
            onDismiss = {},
        )
    }
}
