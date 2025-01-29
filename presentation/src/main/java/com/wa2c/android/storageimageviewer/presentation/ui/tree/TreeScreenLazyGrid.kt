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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.common.values.TreeViewType
import com.wa2c.android.storageimageviewer.domain.model.FileModel
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.model.TreeDataModel
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Color
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Size
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.StorageImageViewerTheme
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Typography
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TreeScreenLazyGrid(
    isViewerModeState: State<Boolean>,
    modifier: Modifier,
    currentTreeState: State<TreeDataModel>,
    focusedFileState: State<FileModel?>,
    viewState: State<TreeViewType>,
    onFocusItem: (FileModel?) -> Unit,
    onClickItem: (FileModel) -> Unit,
) {
    val parentFocusRequester = remember { FocusRequester() }
    val focusRequester = remember { FocusRequester() }
    val lazyListState = rememberLazyGridState()
    val focusedFile = focusedFileState.value

    LazyVerticalGrid(
        modifier = modifier
            .focusRequester(parentFocusRequester)
            .focusProperties {
                exit = { FocusRequester.Default }
                enter = { focusRequester }
            },
        state = lazyListState,
        columns = GridCells
            .Adaptive(minSize = if (viewState.value.isLarge) 128.dp else 80.dp)
    ) {
        val fileList = currentTreeState.value.fileList
        val focusIndex = fileList.indexOf(focusedFile).takeIf { it >= 0 } ?: 0
        itemsIndexed(
            items = fileList,
        ) { index, file ->
            TreeScreenGridItem(
                modifier = Modifier
                    .clickable { onClickItem(file) },
                imageList = currentTreeState.value.imageFileList,
                file = file,
            )
        }
    }

//    LaunchedEffect(currentTreeState.value.fileList) {
//        restoreFocus(
//            isViewerModeState = isViewerModeState,
//            currentTreeState = currentTreeState,
//            focusedFileState = focusedFileState.value,
//            lazyListState = lazyListState,
//            parentFocusRequester = parentFocusRequester,
//            focusRequester = focusRequester,
//        )
//    }
//
//    LaunchedEffect(isViewerModeState.value) {
//        restoreFocus(
//            isViewerModeState = isViewerModeState,
//            currentTreeState = currentTreeState,
//            focusedFileState = focusedFileState,
//            lazyListState = lazyListState,
//            parentFocusRequester = parentFocusRequester,
//            focusRequester = focusRequester,
//        )
//    }
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
                contentAlignment = Alignment.TopStart,
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
                        .background(Color.TreeBadgeBackground)
                        .padding(horizontal = Size.SS)
                )
            }
        }

        Text(
            text = file.name,
            style = Typography.titleMedium,
            maxLines = 2,
            minLines = 2,
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
            isViewerModeState = remember { mutableStateOf(false) },
            modifier = Modifier,
            currentTreeState = remember { mutableStateOf(TreeDataModel(dir, list)) },
            focusedFileState = remember { mutableStateOf(null) },
            viewState = remember { mutableStateOf(TreeViewType.GridLarge) },
            onFocusItem = {},
            onClickItem = {},
        )
    }
}
