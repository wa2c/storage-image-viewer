package com.wa2c.android.storageimageviewer.presentation.ui.tree

import android.content.res.Configuration
import android.text.format.Formatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
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
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.focusItemStyle
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.ifStyle
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.DividerThin
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppSize
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTheme
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTypography
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeScreenDisplayData
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeScreenItemData
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeScreenItemData.Companion.dummyDigits
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TreeScreenLazyList(
    modifier: Modifier,
    currentTreeState: State<TreeScreenItemData>,
    focusedFileState: State<FileModel?>,
    displayState: State<TreeScreenDisplayData>,
    onFocusItem: (FileModel?) -> Unit,
    onClickItem: (FileModel) -> Unit,
) {
    val focusedFile = focusedFileState.value
    val lazyState = rememberLazyListState()
    val parentFocusRequester = remember { FocusRequester() }
    val childFocusRequester = remember { FocusRequester() }

    LazyColumnScrollbar(
        state = lazyState,
        settings = ScrollbarSettings.Default,
        modifier = modifier
    ) {
        LazyColumn(
            state = lazyState,
            modifier = Modifier
                .focusRequester(parentFocusRequester)
        ) {
            val fileList = currentTreeState.value.fileList

            items(
                items = fileList,
            ) { file ->
                var isFocused by remember { mutableStateOf(false) }
                TreeScreenItem(
                    modifier = Modifier
                        .focusItemStyle(isFocused)
                        .ifStyle(focusedFile == file) {
                            focusRequester(childFocusRequester)
                        }
                        .onFocusEvent {
                            isFocused = it.isFocused
                            if (isFocused) {
                                onFocusItem(file)
                            }
                        }
                        .focusable()
                        .clickable { onClickItem(file) },
                    imageList = currentTreeState.value.imageFileList,
                    file = file,
                    viewType = displayState.value.viewType,
                )
                DividerThin()

                if (focusedFile == file) {
                    LaunchedEffect(Unit) {
                        childFocusRequester.requestFocus()
                    }
                }
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
                itemHeight = lazyState.layoutInfo.visibleItemsInfo.firstOrNull()?.size
                    ?: 0,
                parentFocusRequester = parentFocusRequester,
                childFocusRequester = childFocusRequester,
            ) { index, offset ->
                lazyState.requestScrollToItem(index, offset)
            }
        }
    }

//    LaunchedEffect(focusedFile) {
//        if (focusedFile != null) childFocusRequester.restoreFocusedChild ()
//    }

    LaunchedEffect(currentTreeState.value.fileList) {
        setFocus()
    }

    LaunchedEffect(displayState.value.isViewerMode) {
        setFocus()
    }
}

@Composable
private fun TreeScreenItem(
    modifier: Modifier,
    imageList: List<FileModel>,
    file: FileModel,
    viewType: TreeViewType,
) {
    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(horizontal = AppSize.ScreenMargin, vertical = 2.dp)
            .heightIn(min = AppSize.ListItem)
            .fillMaxWidth()
    ) {
        if (imageList.isNotEmpty()) {
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier
                    .padding(end = AppSize.S),
            ) {
                val number = if (file.isDirectory) "" else (imageList.indexOf(file) + 1).toString()

                Text(
                    text = number,
                    style = AppTypography.labelLarge,
                )
                // Dummy for width
                Text(
                    text = imageList.dummyDigits,
                    style = AppTypography.labelLarge,
                    modifier = Modifier
                        .alpha(0f),
                )
            }
        }

        val iconSize = if (viewType.isLarge) AppSize.IconLarge else AppSize.IconMiddle
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
                .padding(start = AppSize.M)
                .padding(vertical = AppSize.SS),
        ) {
            Text(
                text = file.name,
                style = AppTypography.titleLarge,
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
                        style = AppTypography.bodyLarge,
                        maxLines = 1,
                        textAlign = TextAlign.End,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = dateFormatter.format(Date(file.dateModified)),
                    style = AppTypography.bodyLarge,
                    maxLines = 1,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .padding(start = AppSize.S)
                        .weight(1f)
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
private fun TreeScreenLazyListPreview() {
    AppTheme {
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
            modifier = Modifier,
            currentTreeState = remember { mutableStateOf(TreeScreenItemData(dir, list)) },
            focusedFileState = remember { mutableStateOf(null) },
            displayState = remember { mutableStateOf(TreeScreenDisplayData()) },
            onFocusItem = {},
            onClickItem = {},
        )
    }
}
