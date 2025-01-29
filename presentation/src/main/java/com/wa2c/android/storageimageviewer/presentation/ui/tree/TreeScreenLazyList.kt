package com.wa2c.android.storageimageviewer.presentation.ui.tree

import android.content.res.Configuration
import android.text.format.Formatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.alpha
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
import com.wa2c.android.storageimageviewer.domain.model.TreeDataModel
import com.wa2c.android.storageimageviewer.domain.model.TreeDataModel.Companion.dummyDigits
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.DividerThin
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Size
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.StorageImageViewerTheme
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Typography
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TreeScreenLazyList(
    modifier: Modifier,
    currentTreeState: State<TreeDataModel>,
    focusedFileState: State<FileModel?>,
    viewState: State<TreeViewType>,
    onFocusItem: (FileModel?) -> Unit,
    onClickItem: (FileModel) -> Unit,
) {
    val focusedFile = focusedFileState.value
    val lazyState = rememberLazyListState()
    val parentFocusRequester = remember { FocusRequester() }
    val childFocusRequester = remember { FocusRequester() }

    LazyColumnScrollbar(
        state = lazyState,
        settings = ScrollbarSettings.Default
    ) {
        LazyColumn(
            state = lazyState,
            modifier = modifier
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
                TreeScreenItem(
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
                        },
                    imageList = currentTreeState.value.imageFileList,
                    file = file,
                    viewState = viewState,
                    onClickItem = onClickItem,
                )
                DividerThin()
            }
        }
    }

    val setFocus = remember {
        fun() {
            if (!isViewerModeState.value) return
            restoreFocus(
                fileList = currentTreeState.value.fileList,
                focusedFile = focusedFileState.value,
                listHeight = lazyState.layoutInfo.viewportEndOffset,
                itemHeight = lazyState.layoutInfo.visibleItemsInfo.firstOrNull()?.size
                    ?: 0,
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

    LaunchedEffect(isViewerModeState.value) {
        setFocus()
    }
}

@Composable
private fun TreeScreenItem(
    modifier: Modifier,
    imageList: List<FileModel>,
    file: FileModel,
    viewState: State<TreeViewType>,
    onClickItem: (file: FileModel) -> Unit,
) {
    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable { onClickItem(file) }
            .padding(horizontal = Size.ScreenMargin, vertical = 2.dp)
            .heightIn(min = Size.ListItem)
            .fillMaxWidth()
    ) {
        if (imageList.isNotEmpty()) {
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier
                    .padding(end = Size.S),
            ) {
                val number = if (file.isDirectory) "" else (imageList.indexOf(file) + 1).toString()

                Text(
                    text = number,
                    style = Typography.labelLarge,
                )
                // Dummy for width
                Text(
                    text = imageList.dummyDigits,
                    style = Typography.labelLarge,
                    modifier = Modifier
                        .alpha(0f),
                )
            }
        }

        val iconSize = if (viewState.value.isLarge) Size.IconLarge else Size.IconMiddle
        if (file.isDirectory) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_folder),
                contentDescription = file.name,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(iconSize),
            )
        } else {
            AsyncImage(
                model = file.uri.uri,
                contentDescription = file.name,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(iconSize),
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
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = dateFormatter.format(Date(file.dateModified)),
                    style = Typography.bodyLarge,
                    maxLines = 1,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .padding(start = Size.S)
                        .weight(1f)
                )
            }
        }
    }
}

///**
// * Restore focus
// */
//private fun restoreFocus(
//    isViewerModeState: State<Boolean>,
//    currentTreeState: State<TreeDataModel>,
//    focusedFileState: FileModel?,
//    lazyListState: LazyListState,
//    parentFocusRequester: FocusRequester,
//    focusRequester: FocusRequester,
//) {
//    val list = currentTreeState.value.fileList
//    if (list.isNotEmpty() && !isViewerModeState.value) {
//        val index = list.indexOf(focusedFileState)
//        if (index >= 0) {
//            val listHeight = lazyListState.layoutInfo.viewportEndOffset
//            val itemHeight = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0
//            val offset = (listHeight.toFloat() / 2)  - (itemHeight.toFloat() / 2)
//            lazyListState.requestScrollToItem(index, -offset.toInt())
//        } else {
//            lazyListState.requestScrollToItem(0, 0)
//        }
//
//        try {
//            parentFocusRequester.requestFocus()
//            focusRequester.requestFocus()
//        } catch (e: Exception) {
//            Log.e(e)
//        }
//    }
//}

/**
 * Preview
 */
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
private fun TreeScreenLazyListPreview() {
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

        TreeScreenLazyList(
            isViewerModeState = remember { mutableStateOf(false) },
            modifier = Modifier,
            currentTreeState = remember { mutableStateOf(TreeDataModel(dir, list)) },
            focusedFileState = remember { mutableStateOf(null) },
            viewState = remember { mutableStateOf(TreeViewType.ListSmall) },
            onFocusItem = {},
            onClickItem = {},
        )
    }
}
