package com.wa2c.android.storageimageviewer.presentation.ui.tree

import android.content.res.Configuration
import android.content.res.Resources
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.toSize
import coil.compose.AsyncImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
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
import net.engawapg.lib.zoomable.ZoomState
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

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
    val zoomState = rememberZoomState()
    var visibleOverlay by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Viewer
        TreeScreenViewerContent(
            focusRequester = focusRequester,
            pagerState = pagerState,
            zoomState = zoomState,
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
    }

    LaunchedEffect(pagerState.currentPage) {
        onChangeFile(fileList.getOrNull(pagerState.currentPage))
    }

    BackHandler {
        if (zoomState.scale > 1.0f) {
            scope.launch { zoomState.reset() }
        } else {
            onClose()
        }
    }
}

@Composable
private fun TreeScreenViewerContent(
    focusRequester: FocusRequester,
    pagerState: PagerState,
    zoomState: ZoomState,
    fileList: List<FileModel>,
    onStepPage: (step: Int) -> Unit,
    onClickShowOverlay: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var size = remember { androidx.compose.ui.geometry.Size.Unspecified }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .onSizeChanged { size = it.toSize() }
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
                    Key.DirectionLeft -> {
                        if (zoomState.scale > 1.0f) {
                            scope.launch {
                                val offset = if (keyEvent.isShiftPressed) 300f else 100f
                                zoomState.translate(size.center, x = -offset)
                            }
                        } else {
                            if (keyEvent.isShiftPressed) onStepPage(-10) else onStepPage(-1)
                        }
                        true
                    }
                    Key.DirectionRight -> {
                        if (zoomState.scale > 1.0f) {
                            val offset = if (keyEvent.isShiftPressed) 300f else 100f
                            scope.launch { zoomState.translate(size.center, x = +offset) }
                        } else {
                            if (keyEvent.isShiftPressed) onStepPage(10) else onStepPage(1)
                        }
                        true
                    }
                    Key.DirectionUp -> {
                        if (zoomState.scale > 1.0f) {
                            val offset = if (keyEvent.isShiftPressed) 300f else 100f
                            scope.launch { zoomState.translate(size.center, y = -offset) }
                        } else {
                            // TODO
                        }
                        true
                    }
                    Key.DirectionDown -> {
                        if (zoomState.scale > 1.0f) {
                            val offset = if (keyEvent.isShiftPressed) 300f else 100f
                            scope.launch { zoomState.translate(size.center, y = +offset) }
                        } else {
                            // TODO
                        }
                        true
                    }
                    Key.MediaPrevious,
                    Key.MediaRewind,
                    Key.MediaStepBackward,
                    Key.NavigatePrevious,
                    Key.SystemNavigationLeft, -> {
                        if (keyEvent.isShiftPressed) onStepPage(-10) else onStepPage(-1)
                        true
                    }
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
                    Key.DirectionCenter, -> {
                        onClickShowOverlay()
                        true
                    }
                    Key.Spacebar,
                    Key.MediaPlay,
                    Key.MediaPlayPause, -> {
                        scope.launch {
                            zoomState.changeScale(
                                targetScale = zoomState.getNextScale(),
                                position = size.center,
                            )
                        }
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
                        zoomState = zoomState,
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

    LaunchedEffect(pagerState.currentPage) {
        zoomState.reset()
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

private suspend fun ZoomState.translate(center: Offset, x: Float = 0f, y: Float = 0f) {
    val centerX = center.x - offsetX / scale
    val centerY = center.y - offsetY / scale
    centerByLayoutCoordinate(
        offset = Offset(x = centerX + x, y = centerY + y),
        scale,
        tween(100)
    )
}

private fun ZoomState.getNextScale(): Float {
     return (scale + 1.0f).let {
        if (it > maxScale) 1.0f
        else it
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
