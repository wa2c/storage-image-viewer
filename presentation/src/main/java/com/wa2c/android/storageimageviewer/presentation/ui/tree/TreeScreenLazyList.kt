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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
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
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.common.values.TreeViewType
import com.wa2c.android.storageimageviewer.domain.model.FileModel
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.applyIf
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.focusItemStyle
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.DividerThin
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppSize
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTheme
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTypography
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeScreenItemData
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeScreenItemData.Companion.dummyDigits
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeScreenOption
import kotlinx.coroutines.launch
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TreeScreenLazyList(
    modifier: Modifier,
    currentTreeState: State<TreeScreenItemData>,
    targetIndexState: State<Int?>,
    optionState: State<TreeScreenOption>,
    onFocusItem: (FileModel?) -> Unit,
    onClickItem: (FileModel) -> Unit,
) {
    val lazyState = rememberLazyListState()
    val parentFocusRequester = remember { FocusRequester() }
    var childFocusRequester by remember { mutableStateOf<FocusRequester?>(null) }

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

            itemsIndexed(
                items = fileList,
            ) { index, file ->
                TreeScreenItem(
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
                    viewType = optionState.value.treeOption.viewType,
                )
                DividerThin()
            }
        }
    }

    LaunchedEffect(Unit) {
        launch {
            snapshotFlow { targetIndexState.value }.collect { targetIndex ->
                currentTreeState.value.fileList.ifEmpty { return@collect }
                val index = targetIndex?.coerceIn(currentTreeState.value.fileList.indices) ?: return@collect
                val listHeight = lazyState.layoutInfo.viewportEndOffset
                val itemHeight = lazyState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0
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

        TreeScreenLazyList(
            modifier = Modifier,
            currentTreeState = remember { mutableStateOf(TreeScreenItemData(listOf(dir), list)) },
            targetIndexState = remember { mutableStateOf<Int?>(null) },
            optionState = remember { mutableStateOf(TreeScreenOption()) },
            onFocusItem = {},
            onClickItem = {},
        )
    }
}
