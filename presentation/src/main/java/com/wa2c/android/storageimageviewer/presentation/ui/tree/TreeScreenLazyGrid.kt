package com.wa2c.android.storageimageviewer.presentation.ui.tree

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.wa2c.android.storageimageviewer.common.utils.Log
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.common.values.TreeViewType
import com.wa2c.android.storageimageviewer.domain.model.FileModel
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Color
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Size
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.StorageImageViewerTheme
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Typography
import my.nanihadesuka.compose.LazyVerticalGridScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TreeScreenLazyGrid(
    modifier: Modifier,
    currentTreeState: State<TreeScreenItemData>,
    focusedFileState: State<FileModel?>,
    displayState: State<TreeScreenDisplayData>,
    onFocusItem: (FileModel?) -> Unit,
    onClickItem: (FileModel) -> Unit,
) {
    val lazyState = rememberLazyGridState()
    val parentFocusRequester = remember { FocusRequester() }
    val childFocusRequester = remember { FocusRequester() }
    val focusedFile = focusedFileState.value

    LazyVerticalGridScrollbar(
        state = lazyState,
        settings = ScrollbarSettings.Default,
        modifier = modifier,
    ) {
        LazyVerticalGrid(
            state = lazyState,
            columns = GridCells
                .Adaptive(minSize = if (displayState.value.viewType.isLarge) 128.dp else 96.dp),
            modifier = Modifier
                .focusRequester(parentFocusRequester)
                .focusProperties {
                    exit = { FocusRequester.Default }
                    enter = { childFocusRequester }
                },
        ) {
            val fileList = currentTreeState.value.fileList
            val focusIndex = fileList.indexOf(focusedFile).takeIf { it >= 0 } ?: 0
            itemsIndexed(
                items = fileList,
            ) { index, file ->
                TreeScreenGridItem(
                    modifier = Modifier
                        .onFocusChanged {
                            if (it.isFocused) {
                                onFocusItem(file)
                            }
                            // else { onFocusItem(null) } NOTE: keep focus
                        }
                        .let {
                            if (index == focusIndex) it.focusRequester(childFocusRequester)
                            else it
                        }
                        .clickable { onClickItem(file) },
                    imageList = currentTreeState.value.imageFileList,
                    file = file,
                )
            }
        }
    }


    val setFocus = remember {
        fun() {
            if (displayState.value.isViewerMode) return
            restoreFocus(
                fileList = currentTreeState.value.fileList,
                focusedFile = focusedFileState.value,
                listHeight = lazyState.layoutInfo.viewportEndOffset,
                itemHeight = lazyState.layoutInfo.visibleItemsInfo.firstOrNull()?.size?.height ?: 0 ,
                parentFocusRequester = parentFocusRequester,
                childFocusRequester = childFocusRequester,
            ) { index, offset ->
                lazyState.requestScrollToItem(index, offset)
            }
        }
    }

    LaunchedEffect(currentTreeState.value.fileList) {
        setFocus()
    }

    LaunchedEffect((displayState.value.isViewerMode)) {
        setFocus()
    }
}


@Composable
private fun TreeScreenGridItem(
    modifier: Modifier,
    imageList: List<FileModel>,
    file: FileModel,
) {
    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
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
                    style = Typography.labelMedium,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .background(Color.TreeBadgeBackground)
                        .padding(horizontal = Size.SS)
                )
            }
        }

        Text(
            text = file.name,
            style = Typography.titleMedium,
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

        Uri.parse("android.resource://com.wa2c.android.storageimageviewer/" + R.drawable.ic_launcher_foreground)

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

        TreeScreenLazyGrid(
            modifier = Modifier,
            currentTreeState = remember { mutableStateOf(TreeScreenItemData(dir, list)) },
            focusedFileState = remember { mutableStateOf(null) },
            displayState = remember { mutableStateOf(TreeScreenDisplayData()) },
            onFocusItem = {},
            onClickItem = {},
        )
    }
}
