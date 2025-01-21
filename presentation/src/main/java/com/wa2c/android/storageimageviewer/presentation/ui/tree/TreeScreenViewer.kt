package com.wa2c.android.storageimageviewer.presentation.ui.tree

import android.content.res.Configuration
import android.content.res.Resources
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.domain.model.FileModel
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.model.TreeData
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.toUri
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.LoadingBox
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Color
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Size
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.StorageImageViewerTheme
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Typography
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeScreenViewerContainer(
    initialFile: State<FileModel?>,
    currentTreeState: State<TreeData>,
    onChangeFile: (FileModel?) -> Unit,
    onClose: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    val fileList = currentTreeState.value.fileList.filter { !it.isDirectory }
    val pagerState = rememberPagerState(
        pageCount = { fileList.size },
        initialPage = initialFile.value?.let { fileList.indexOf(it) } ?: 0,
    )
    var visibleOverlay by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val sheetState = rememberModalBottomSheetState(false)

        // Viewer
        TreeScreenViewerContent(
            focusRequester = focusRequester,
            pagerState = pagerState,
            fileList = fileList,
            onStepPage = { step ->
                scope.launch {
                    val page = (pagerState.currentPage + step).coerceIn(0, pagerState.pageCount - 1)
                    pagerState.animateScrollToPage(page = page)
                }
            },
            onClickShowOverlay = {
                visibleOverlay = !visibleOverlay
            },
            onClickShowBottomSheet = {
                scope.launch {
                    sheetState.show()
                }
            }
        )

        Box(
            contentAlignment = Alignment.TopEnd,
            modifier = Modifier
                .statusBarsPadding()
                .padding(end = Size.S)
        ) {
            Text(
                text = "${(pagerState.currentPage + 1)} / ${pagerState.pageCount}",
                style = Typography.labelMedium,
                modifier = Modifier
                    .clip(RoundedCornerShape(Size.L))
                    .background(color = Color.ViewerOverlayBackground)
                    .padding(horizontal = Size.S, Size.SS)
            )
        }

        // Overlay
        AnimatedVisibility(
            visible = visibleOverlay,
            enter = fadeIn(),
            exit = fadeOut(),
            content = {
                TreeScreenViewerOverlay(
                    file = fileList[pagerState.currentPage],
                    onClose = onClose,
                )
            },
        )

        if (sheetState.isVisible) {
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = {
                    scope.launch { sheetState.hide() }
                },
            ) {
                Text(text = "Modal Bottom Sheet")
            }
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        onChangeFile(fileList.getOrNull(pagerState.currentPage))
    }

    BackHandler {
        if (visibleOverlay) {
            visibleOverlay = false
        } else  {
            onClose()
        }
    }
}

@Composable
private fun TreeScreenViewerContent(
    focusRequester: FocusRequester,
    pagerState: PagerState,
    fileList: List<FileModel>,
    onStepPage: (step: Int) -> Unit,
    onClickShowOverlay: () -> Unit,
    onClickShowBottomSheet: () -> Unit,
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .onKeyEvent { keyEvent ->
                if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (keyEvent.nativeKeyEvent.scanCode) {
                    104 -> {  // PageUp
                        onStepPage(+1)
                        return@onKeyEvent true
                    }
                    109 -> { // PageDown
                        onStepPage(-1)
                        return@onKeyEvent true
                    }
                }

                when (keyEvent.key) {
                    Key.DirectionLeft,
                    Key.MediaPrevious,
                    Key.MediaRewind,
                    Key.MediaStepBackward,
                    Key.NavigatePrevious,
                    Key.SystemNavigationLeft, -> {
                        if (keyEvent.isShiftPressed) onStepPage(-10) else onStepPage(-1)
                        true
                    }
                    Key.DirectionRight,
                    Key.MediaNext,
                    Key.MediaFastForward,
                    Key.MediaStepForward,
                    Key.NavigateNext,
                    Key.SystemNavigationRight, -> {
                        if (keyEvent.isShiftPressed) onStepPage(10) else onStepPage(1)
                        true
                    }
                    Key.PageUp,
                    Key.MediaSkipBackward, -> {
                        onStepPage(-10)
                        true
                    }
                    Key.PageDown,
                    Key.MediaSkipForward, -> {
                        onStepPage(10)
                        true
                    }
                    Key.LeftBracket,
                    Key.MoveHome, -> {
                        onStepPage(Int.MIN_VALUE)
                        true
                    }
                    Key.RightBracket,
                    Key.MoveEnd, -> {
                        onStepPage(Int.MAX_VALUE)
                        true
                    }
                    Key.Enter,
                    Key.NumPadEnter,
                    Key.DirectionCenter,
                    Key.MediaPlay,
                    Key.MediaPlayPause, -> {
                        onClickShowOverlay()
                        true
                    }
                    Key.DirectionUp -> {
                        onClickShowBottomSheet()
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
            .focusRequester(focusRequester)
            .focusable()
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) { page ->
        Box {
            var isLoading by remember { mutableStateOf(false) }
            val file = fileList.getOrNull(page)

            AsyncImage(
                model = file?.uri?.toUri(),
                contentDescription = file?.name,
                onLoading = {
                    isLoading = true
                },
                onSuccess = {
                    isLoading = false
                },
                onError = {
                    isLoading = false
                },
                modifier = Modifier
                    .fillMaxSize()
                    .zoomable(
                        zoomState = rememberZoomState(),
                        onTap = { offset ->
                            val width = Resources.getSystem().displayMetrics.widthPixels
                            if (offset.x <= width * 0.15f) {
                                onStepPage(-1)
                            } else if (offset.x >= width * 0.85f) {
                                onStepPage(+1)
                            } else {
                                onClickShowOverlay()
                            }
                        }
                        ,
                    )
            )

            LoadingBox(
                isLoading = isLoading
            )
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TreeScreenViewerOverlay(
    file: FileModel,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = file.name,
                    modifier = Modifier
                        .basicMarquee()
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onClose
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                        contentDescription = "Close",
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors( containerColor = Color.ViewerOverlayBackground),
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
private fun TreeScreenContainerPreview() {
    StorageImageViewerTheme {
        val storage = StorageModel(
            id = "1",
            uri = UriModel(uri = "content://test1/"),
            name = "Test Storage 1",
            type = StorageType.SAF,
            sortOrder = 1,
        )
        val list = listOf(
            FileModel(
                storage = storage,
                uri = UriModel( "content://test1/"),
                name = "Test directory",
                isDirectory = true,
                mimeType = "",
                size = 0,
                dateModified = 0,
            ),
            FileModel(
                storage = storage,
                uri = UriModel( "content://test2/"),
                name = "Test file.jpg",
                isDirectory = true,
                mimeType = "image/jpeg",
                size = 10000,
                dateModified = 0,
            ),
        )

        TreeScreenViewerContainer(
            initialFile = remember { mutableStateOf(null) },
            currentTreeState = remember { mutableStateOf(TreeData(null, list)) },
            onChangeFile = {},
            onClose = {},
        )
    }
}
