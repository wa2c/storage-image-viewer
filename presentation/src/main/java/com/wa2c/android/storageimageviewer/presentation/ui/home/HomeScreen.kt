package com.wa2c.android.storageimageviewer.presentation.ui.home

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.waterfallPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wa2c.android.storageimageviewer.common.result.AppException
import com.wa2c.android.storageimageviewer.common.utils.Log
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.applyIf
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.focusItemStyle
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.toDisplayTreePath
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.toUri
import com.wa2c.android.storageimageviewer.presentation.ui.common.collectIn
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.DividerThin
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.StorageIcon
import com.wa2c.android.storageimageviewer.presentation.ui.common.showMessage
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppSize
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTheme
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTypography
import kotlinx.coroutines.launch
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
    onNavigateSettings: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val resolver = context.contentResolver
    val storageListState = viewModel.storageList.collectAsStateWithLifecycle()
    val editStorage = viewModel.editStorage.collectAsStateWithLifecycle()

    val treeOpenLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        viewModel.setUri(uri.toString(), uri.toDisplayTreePath(context))
    }

    HomeScreenContainer(
        snackBarHostState = snackBarHostState,
        storageListState = storageListState,
        onClickAdd = viewModel::newStorage,
        onClickEdit = viewModel::updateEditStorage,
        onClickItem = onSelectStorage,
        onClickSettings = onNavigateSettings,
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
        onClickSave = { storage ->
            try {
                // Grant URI permission
                resolver.takePersistableUriPermission(
                    storage.uri.toUri(),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
                viewModel.saveStorage(storage)
            } catch (e: Exception) {
                scope.launch {
                    snackBarHostState.showMessage(context, Result.failure(AppException.StorageEditException(e)))
                }
            }
        },
        onClickDelete = { storage ->
            try {
                val existsUri = storageListState.value.filter { it.id != storage.id }.any { it.uri == storage.uri }
                if (!existsUri) {
                    // Release URI permission (if other storage is not using)
                    resolver.releasePersistableUriPermission(
                        storage.uri.toUri(),
                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                }
                viewModel.deleteStorage(storage)
            } catch (e: Exception) {
                scope.launch {
                    snackBarHostState.showMessage(context, Result.failure(AppException.StorageEditException(e)))
                }
            }
        },
        onDismiss = {
            viewModel.updateEditStorage(null)
        },
    )

    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusManager.clearFocus()
        viewModel.resultState.collectIn(lifecycleOwner = lifecycleOwner) {
            snackBarHostState.showMessage(context, it)
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
    onClickSettings: () -> Unit,
    onDragAndDrop: (from: Int, to: Int) -> Unit,
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        maxLines = 1,
                        modifier = Modifier
                            .padding(start = AppSize.S)
                            .basicMarquee(),
                    )
                },
                navigationIcon = {
                   try {
                        context.packageManager.getApplicationIcon(context.packageName)
                    } catch (e: Exception) {
                        Log.w(e)
                        ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
                    }?.let { icon ->
                        Image(
                            bitmap = icon.toBitmap().asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(AppSize.IconSmall)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onClickSettings,
                        modifier = Modifier
                            .focusItemStyle()
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_settings),
                            contentDescription = null,
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onClickAdd,
                modifier = Modifier
                    .focusItemStyle()
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_folder_add),
                    contentDescription = stringResource(R.string.home_storage_dialog_title_add),
                )
            }
        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .displayCutoutPadding()
                .waterfallPadding()
            ,
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

    val savedFocus = rememberSaveable { mutableStateOf<StorageModel?>(null) }
    var focusRequester by remember { mutableStateOf<FocusRequester?>(null) }

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
            items(items = storageListState.value) { storage ->
                ReorderableItem(state, key = storage) { isDragging ->
                    val elevation = animateDpAsState(if (isDragging) AppSize.S else 0.dp, label = "")
                    HomeScreenStorageItem(
                        storage = storage,
                        modifier = Modifier
                            .focusItemStyle()
                            .applyIf(storage == savedFocus.value) {
                                val requester = FocusRequester()
                                focusRequester(requester).also { focusRequester = requester }
                            }
                            .focusable()
                            .clickable {
                                onClickItem(storage)
                                savedFocus.value = storage
                            }
                            .shadow(elevation.value),
                        onClickEdit = onClickEdit,
                    )
                }
                DividerThin()
            }
        }
    }

    LaunchedEffect(Unit) {
        savedFocus.value?.let { focusedFile ->
            val index = storageListState.value.indexOf(focusedFile)
            val listHeight = state.listState.layoutInfo.viewportEndOffset
            val itemHeight = state.listState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0
            val offset = (listHeight.toFloat() / 2) - (itemHeight.toFloat() / 2)
            state.listState.requestScrollToItem(index, -offset.toInt())
        }

        focusRequester?.let { requester ->
            requester.requestFocus()
            focusRequester = null
            savedFocus.value = null
        }
    }

}

@Composable
private fun HomeScreenStorageItem(
    storage: StorageModel,
    modifier: Modifier = Modifier,
    onClickEdit: (storage: StorageModel) -> Unit,
) {
    val context = LocalContext.current
    val granted = context.checkCallingOrSelfUriPermission(storage.uri.toUri(), Intent.FLAG_GRANT_READ_URI_PERMISSION) == PackageManager.PERMISSION_GRANTED

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSize.M, vertical = AppSize.SS)
            .heightIn(min = AppSize.ListItem),
    ) {
        Box(
            modifier = Modifier
                .clickable { onClickEdit(storage) }
                .focusable()
        ) {
            StorageIcon(
                storage = storage,
            )
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_drop_down),
                contentDescription = "",
                modifier = Modifier
                    .size(AppSize.IconAddition)
                    .align(Alignment.BottomCenter)
                    .offset(y = AppSize.M)
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
                storage.rootUri.toUri().toDisplayTreePath(context) ?: ""
            } else {
                stringResource(R.string.home_storage_no_granted)
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
                name = "Test Storage 1",
                uri = UriModel(uri = "content://test1/"),
                rootUri = UriModel(uri = "content://test1/"),
                type = StorageType.SAF,
                sortOrder = 1,
            ),
            StorageModel(
                id = "2",
                name = "Test Storage 2",
                uri = UriModel(uri = "content://test2/"),
                rootUri = UriModel(uri = "content://test2/"),
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
            onClickSettings = {},
            onDragAndDrop = { _, _ -> },
        )
    }
}
