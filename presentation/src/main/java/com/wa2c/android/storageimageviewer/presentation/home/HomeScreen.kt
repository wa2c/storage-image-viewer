package com.wa2c.android.storageimageviewer.presentation.home

import android.content.res.Configuration
import android.content.res.Resources.Theme
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wa2c.android.storageimageviewer.common.value.StorageType
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.common.DividerThin
import com.wa2c.android.storageimageviewer.presentation.common.ValueResource.drawableResId
import com.wa2c.android.storageimageviewer.presentation.common.theme.Size
import com.wa2c.android.storageimageviewer.presentation.common.theme.StorageImageViewerTheme
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onAddStorage: (uri: UriModel) -> Unit,
    onSelectStorage: (storage: StorageModel) -> Unit,
) {
    val treeOpenLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri == null) {
            // todo message
            return@rememberLauncherForActivityResult
        }
        onAddStorage(UriModel(uri.toString()))
    }

    val storageListState = viewModel.storageList.collectAsStateWithLifecycle()

    HomeScreenContainer(
        storageListState = storageListState,
        onClickAdd = { treeOpenLauncher.launch(null) },
        onClickItem = onSelectStorage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContainer(
    storageListState: State<List<StorageModel>>,
    onClickAdd: () -> Unit,
    onClickItem: (storage: StorageModel) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
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
//        snackbarHost = { AppSnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HomeScreenStorageList(
                storageListState = storageListState,
                onClickItem = onClickItem,
                onDragAndDrop = { from, to ->  }
            )
        }
    }
}

@Composable
private fun HomeScreenStorageList(
    storageListState: State<List<StorageModel>>,
    onClickItem: (storage: StorageModel) -> Unit,
    onDragAndDrop: (from: Int, to: Int) -> Unit,
) {
    val state = rememberReorderableLazyListState(onMove = { from, to ->
        onDragAndDrop(from.index, to.index)
    })
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
                )
            }
            DividerThin()
        }
    }
}

@Composable
private fun HomeScreenStorageItem(
    storage: StorageModel,
    modifier: Modifier = Modifier,
    onClickItem: (storage: StorageModel) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(horizontal = Size.M, vertical = Size.SS)
            .heightIn(min = Size.ListItem)
            .clickable { onClickItem(storage) }
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
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = storage.uri.uri,
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
    name = "Preview",
    group = "Group",
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
            storageListState = remember { mutableStateOf(storageList) },
            onClickAdd = {},
            onClickItem = {},
        )
    }
}
