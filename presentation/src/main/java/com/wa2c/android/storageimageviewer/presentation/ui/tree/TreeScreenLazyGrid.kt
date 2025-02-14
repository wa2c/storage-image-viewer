package com.wa2c.android.storageimageviewer.presentation.ui.tree

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.domain.model.FileModel
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.applyIf
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.focusItemStyle
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppColor
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppSize
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTheme
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTypography
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeScreenItemData
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeScreenOption
import kotlinx.coroutines.launch
import my.nanihadesuka.compose.LazyVerticalGridScrollbar
import my.nanihadesuka.compose.ScrollbarSettings

@Composable
fun TreeScreenLazyGrid(
    modifier: Modifier,
    currentTreeState: State<TreeScreenItemData>,
    targetIndexState: State<Int?>,
    optionState: State<TreeScreenOption>,
    onForwardSkip: () -> Unit,
    onBackwardSkip: () -> Unit,
    onFocusItem: (FileModel?) -> Unit,
    onClickItem: (FileModel) -> Unit,
) {
    val lazyState = rememberLazyGridState()
    val parentFocusRequester = remember { FocusRequester() }
    var childFocusRequester by remember { mutableStateOf<FocusRequester?>(null) }

    LazyVerticalGridScrollbar(
        state = lazyState,
        settings = ScrollbarSettings.Default,
        modifier = modifier,
    ) {
        LazyVerticalGrid(
            state = lazyState,
            columns = GridCells
                .Adaptive(minSize = if (optionState.value.treeOption.viewType.isLarge) 128.dp else 96.dp),
            modifier = Modifier
                .focusRequester(parentFocusRequester)
                .treeKeyControl(
                    isPreview = true,
                    onForwardSkip = onForwardSkip,
                    onBackwardSkip = onBackwardSkip,
                )
        ) {
            itemsIndexed(
                items = currentTreeState.value.fileList,
            ) { index, file ->
                TreeScreenGridItem(
                    modifier = Modifier
                        .applyIf(targetIndexState.value == index) {
                            val requester = FocusRequester()
                            focusRequester(requester).also { childFocusRequester = requester }
                        }
                        .focusItemStyle()
                        .onFocusChanged {
                            if (it.isFocused) {
                                onFocusItem(file)
                            }
                        }
                        .clickable { onClickItem(file) },
                    imageList = currentTreeState.value.imageFileList,
                    file = file,
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        launch {
            snapshotFlow { targetIndexState.value }.collect { targetIndex ->
                currentTreeState.value.fileList.ifEmpty { return@collect }
                val index = targetIndex?.coerceIn(currentTreeState.value.fileList.indices) ?: return@collect
                val listHeight = lazyState.layoutInfo.viewportEndOffset
                val itemHeight = lazyState.layoutInfo.visibleItemsInfo.firstOrNull()?.size?.height ?: 0
                val offset = (listHeight.toFloat() / 2) - (itemHeight.toFloat() / 2)
                lazyState.requestScrollToItem(index, -offset.toInt())
            }
        }
        launch {
            snapshotFlow { childFocusRequester }.collect {
                if (it != null) {
                    parentFocusRequester.requestFocus()
                    it.requestFocus()
                    childFocusRequester = null
                }
            }
        }
    }


}

@Composable
private fun TreeScreenGridItem(
    modifier: Modifier,
    imageList: List<FileModel>,
    file: FileModel,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(1f)
            .fillMaxSize()
    ) {
        if (file.isDirectory) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_folder),
                contentDescription = file.name,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            )
        } else {
            val number = (imageList.indexOf(file) + 1).toString() + " "
            Box (
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                AsyncImage(
                    model = file.uri.uri,
                    contentDescription = file.name,
                    modifier = Modifier
                        .fillMaxSize()
                )
                Text(
                    text = number,
                    style = AppTypography.labelMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .background(AppColor.TreeBadgeBackground)
                        .padding(horizontal = AppSize.SS)
                )
            }
        }

        Text(
            text = file.name,
            style = AppTypography.titleMedium,
            maxLines = 1,
            minLines = 1,
            softWrap = true,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
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
private fun TreeScreenLazyGridPreview() {
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

        TreeScreenLazyGrid(
            modifier = Modifier,
            currentTreeState = remember { mutableStateOf(TreeScreenItemData(listOf(dir), list)) },
            targetIndexState = remember { mutableStateOf<Int?>(null) },
            optionState = remember { mutableStateOf(TreeScreenOption()) },
            onForwardSkip = {},
            onBackwardSkip = {},
            onFocusItem = {},
            onClickItem = {},
        )
    }
}
