package com.wa2c.android.storageimageviewer.presentation.ui.home

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wa2c.android.storageimageviewer.common.utils.Log
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.toUri
import com.wa2c.android.storageimageviewer.presentation.ui.common.ValueResource.drawableResId
import com.wa2c.android.storageimageviewer.presentation.ui.common.collectIn
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.DividerThin
import com.wa2c.android.storageimageviewer.presentation.ui.common.dialog.CommonDialog
import com.wa2c.android.storageimageviewer.presentation.ui.common.dialog.DialogButton
import com.wa2c.android.storageimageviewer.presentation.ui.common.showMessage
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppSize
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTheme
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTypography
import com.wa2c.android.storageimageviewer.presentation.ui.tree.treeKeyControl
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
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackBarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val resolver = context.contentResolver
    val storageListState = viewModel.storageList.collectAsStateWithLifecycle()
    val editStorage = viewModel.editStorage.collectAsStateWithLifecycle()

    val treeOpenLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        viewModel.setUri(uri?.toString(), uri?.lastPathSegment)
    }

    HomeScreenContainer(
        snackBarHostState = snackBarHostState,
        storageListState = storageListState,
        onClickAdd = {
            viewModel.newStorage()

        },
        onClickEdit = { storage ->
            viewModel.updateEditStorage(storage)
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
        onEditName = { text ->
            viewModel.updateEditStorage(storage = editStorage.value?.copy(name = text))
        },
        onClickSet = { storage ->
            if (storage.uri.isInvalidUri) {
                // todo message
            } else if (storage.name.isEmpty()) {
                // todo message
            } else {
                resolver.takePersistableUriPermission(
                    storage.uri.toUri() ?: return@HomeScreenStorageEditDialog,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
                viewModel.setStorage(storage)
            }
        },
        onDismiss = {
            viewModel.updateEditStorage(null)
        },
    )

    LaunchedEffect(Unit) {
        viewModel.resultState.collectIn(lifecycleOwner = lifecycleOwner) {
            snackBarHostState.showMessage(it)
        }
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
                },
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
        snackbarHost = { SnackbarHost(snackBarHostState) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
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
    val state = rememberReorderableLazyListState(
        onMove = { from, to ->
            onDragAndDrop(from.index, to.index)
        },
    )
    LazyColumnScrollbar(
        state = state.listState,
        settings = ScrollbarSettings.Default,
    ) {
        LazyColumn(
            state = state.listState,
            modifier = Modifier
                .reorderable(state)
                .detectReorderAfterLongPress(state),
        ) {
            items(
                items = storageListState.value,
                key = { it.id },
            ) { storage ->
                ReorderableItem(state, key = storage) { isDragging ->

                    val elevation = animateDpAsState(if (isDragging) AppSize.S else 0.dp, label = "")
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
    val granted = context.checkCallingOrSelfUriPermission(storage.uri.toUri(), Intent.FLAG_GRANT_READ_URI_PERMISSION) == PackageManager.PERMISSION_GRANTED

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
            .padding(horizontal = AppSize.M, vertical = AppSize.SS)
            .heightIn(min = AppSize.ListItem),
    ) {
        if (storage.type == StorageType.SAF) {
            try {
                storage.uri.toUri()?.authority?.let { authority ->
                    val packages: List<PackageInfo> = context.packageManager.getInstalledPackages(PackageManager.GET_PROVIDERS)
                    packages.firstOrNull { pack ->
                        pack.providers?.firstOrNull { provider ->
                            provider.authority?.let { authority.contains(it) } ?: false
                        } != null
                    }?.applicationInfo?.loadIcon(context.packageManager)
                }
            } catch (e: Exception) {
                Log.w(e)
                null
            }?.let { drawable ->
                // App Icon
                Image(
                    bitmap = drawable.toBitmap().asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(AppSize.IconMiddle),
                )
            } ?: let {
                Icon(
                    imageVector = ImageVector.vectorResource(storage.type.drawableResId()),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(AppSize.IconMiddle),
                )
            }
        } else {
            Icon(
                imageVector = ImageVector.vectorResource(storage.type.drawableResId()),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(AppSize.IconMiddle),
            )
        }

        Column(
            modifier = Modifier
                .padding(start = AppSize.M),
        ) {
            Text(
                text = storage.name,
                style = AppTypography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val subText = if (granted) {
                 DocumentsContract.getTreeDocumentId(storage.uri.toUri())
            } else {
                "Not granted" // FIXME
            }

            Text(
                text = subText,
                style = AppTypography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun HomeScreenStorageEditDialog(
    editStorage: State<StorageModel?>,
    onClickUri: (uri: UriModel) -> Unit,
    onEditName: (name: String) -> Unit,
    onClickSet: (storage: StorageModel) -> Unit,
    onDismiss: () -> Unit,
) {
    val storage = editStorage.value ?: return

    CommonDialog(
        title = "Edit",
        confirmButtons = listOf(
            DialogButton(
                label = "Save", // FIXME
                enabled = storage.name.isNotEmpty() && !storage.uri.isInvalidUri,
                onClick = {
                    onClickSet(storage)
                },
            ),
        ),
        dismissButton = DialogButton(
            label = "Cancel", // FIXME
            onClick = onDismiss,
        ),
        onDismiss = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // URI
            Box {
                OutlinedTextField(
                    value = storage.uri.uri,
                    label = { Text("URI") }, // fixme
                    placeholder = { Text("Select URI") }, // fixme
                    readOnly = true,
                    onValueChange = { },
                    maxLines = 1,
                    singleLine = true,
                    modifier = Modifier
                        .treeKeyControl(
                            isPreview = true,
                            onEnter = { onClickUri(storage.uri) },
                            onPlay = { onClickUri(storage.uri) }

                        )
                        .padding(top = AppSize.M),
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(top = AppSize.L)
                        .clip(RoundedCornerShape(AppSize.SS))
                        .clickable { onClickUri(storage.uri) },
                )
            }

            // Name
            OutlinedTextField(
                value = storage.name,
                label = { Text("Name") }, // fixme
                placeholder = { Text("Input name") }, // fixme
                onValueChange = { value ->
                    onEditName(value)
                },
                maxLines = 1,
                singleLine = true,
                modifier = Modifier
                    .padding(top = AppSize.S)
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
    AppTheme {
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
    AppTheme {
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
            onEditName = {},
            onClickSet = {},
            onDismiss = {},
        )
    }
}
