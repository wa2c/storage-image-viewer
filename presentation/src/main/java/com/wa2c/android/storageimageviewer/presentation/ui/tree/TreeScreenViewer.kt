@file:Suppress("DEPRECATION")

package com.wa2c.android.storageimageviewer.presentation.ui.tree

import android.content.res.Configuration
import android.content.res.Resources
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.domain.model.FileModel
import com.wa2c.android.storageimageviewer.domain.model.SortModel
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
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
fun TreeScreenViewer(
    viewModel: TreeViewModel = hiltViewModel(),
    onClose: () -> Unit,
) {
    val imageFileList = viewModel.currentTree.collectAsStateWithLifecycle().value.imageFileList
    val focusedFile = viewModel.focusedFile.collectAsStateWithLifecycle().value

    val pagerState = rememberPagerState(
        pageCount = { imageFileList.size },
        initialPage = focusedFile?.let { imageFileList.indexOf(it) } ?: 0,
    )

    TreeScreenViewerContainer(
        pagerState = pagerState,
        fileList = imageFileList,
        onChangeFile = viewModel::focusFile,
        sortState = viewModel.sortState.collectAsStateWithLifecycle(),
        onSetSort = viewModel::sortFile,
        onClose = onClose,
    )

    LaunchedEffect(imageFileList) {
        pagerState.requestScrollToPage(imageFileList.indexOf(viewModel.focusedFile.value))
    }
    LaunchedEffect(focusedFile) {
        val focusedPage = imageFileList.indexOf(focusedFile).takeIf { it >= 0 } ?: 0
        if (pagerState.currentPage != focusedPage) pagerState.requestScrollToPage(focusedPage)
    }
}

@Composable
fun TreeScreenViewerContainer(
    pagerState: PagerState,
    fileList: List<FileModel>,
    onChangeFile: (FileModel?) -> Unit,
    sortState: State<SortModel>,
    onSetSort: (SortModel) -> Unit,
    onClose: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val zoomState = rememberZoomState()
    var size = remember { androidx.compose.ui.geometry.Size.Unspecified }
    var visibleOverlay by remember { mutableStateOf(false) }
    val sortMenuExpanded = remember { mutableStateOf(false) }

    val systemUiController = rememberSystemUiController()
    if (visibleOverlay) {
        systemUiController.isSystemBarsVisible = true
        systemUiController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
    } else {
        systemUiController.isSystemBarsVisible = false
        systemUiController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    val pageRange = 0..<pagerState.pageCount
    val animatedColor = remember { Animatable(Color.ViewerOverlayBackground) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size = it.toSize() }
    ) {
        // Viewer
        TreeScreenViewerContent(
            focusRequester = focusRequester,
            pagerState = pagerState,
            zoomState = zoomState,
            fileList = fileList,
            onStepPage = { step ->
                scope.launch {
                    // Change page
                    val page = (pagerState.currentPage + step).coerceIn(pageRange)
                    if (page != pagerState.currentPage) pagerState.animateScrollToPage(page = page)
                    else animatedColor.flashPage()
                }
            },
            onClickShowOverlay = {
                visibleOverlay = !visibleOverlay
            },
            modifier = Modifier
                .keyControl(
                    zoomState = zoomState,
                    onStepPage = { step ->
                        scope.launch {
                            // Change page
                            val page = (pagerState.currentPage + step).coerceIn(pageRange)
                            if (page != pagerState.currentPage) pagerState.animateScrollToPage(page = page)
                            else animatedColor.flashPage()
                        }
                    },
                    onZoom = {
                        scope.launch {
                            zoomState.changeScale(
                                targetScale = zoomState.getNextScale(),
                                position = size.center,
                            )
                        }
                    },
                    onZoomScroll = { isXPositive, isYPositive, isSkip ->
                        scope.launch {
                            val offset = if (isSkip == null) Float.MAX_VALUE else if (isSkip == true) 300f else 100f
                            val xValue = (if (isXPositive == null) 0 else if (isXPositive) 1 else -1) * offset
                            val yValue = (if (isYPositive == null) 0 else if (isYPositive) 1 else -1) * offset
                            zoomState.translate(size.center, x = xValue, y = yValue)
                        }
                    },
                    onShowOverlay = {
                        visibleOverlay = !visibleOverlay
                    },
                    onShowMenu = {
                        visibleOverlay = true
                        sortMenuExpanded.value = true
                    }
                )
        )

        // Page
        Box(
            contentAlignment = Alignment.BottomStart,
            modifier = Modifier
                .let {
                    if (visibleOverlay) it.navigationBarsPadding()
                    else it.padding(bottom = Size.M)
                }
                .padding(start = Size.M)
        ) {
            Text(
                text = "${(pagerState.currentPage + 1)} / ${pagerState.pageCount}",
                style = Typography.labelMedium,
                modifier = Modifier
                    .clip(RoundedCornerShape(Size.L))
                    .background(color = animatedColor.value)
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
                    sortMenuExpanded = sortMenuExpanded,
                    sortState = sortState,
                    onSetSort = onSetSort,
                    onClose = {
                        visibleOverlay = true
                        onClose()
                    },
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
            visibleOverlay = true
            onClose()
        }
    }
}

/**
 * Flash page background (Indicates that page transition is not possible.)
 */
private suspend fun Animatable<androidx.compose.ui.graphics.Color, AnimationVector4D>.flashPage() {
    animateTo(
        targetValue = Color.ViewerOverlayFlash,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
    )
    animateTo(
        targetValue = Color.ViewerOverlayBackground,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
    )
}

private fun Modifier.keyControl(
    zoomState: ZoomState,
    onStepPage: (step: Int) -> Unit,
    onZoom: () -> Unit,
    onZoomScroll: (isXPositive: Boolean?, isYPositive: Boolean?, isSkip: Boolean?) -> Unit,
    onShowOverlay: () -> Unit,
    onShowMenu: () -> Unit,
): Modifier {
    return this.treeKeyControl(
        onEnter = onShowOverlay,
        onPlay = onZoom,
        onDirectionUp = { isShift ->
            if (zoomState.scale > 1.0f) {
                onZoomScroll(null, false, isShift)
            } else {
                onShowOverlay()
            }
        },
        onDirectionDown = { isShift ->
            if (zoomState.scale > 1.0f) {
                onZoomScroll(null, true, isShift)
            } else {
                onShowOverlay()
            }
        },
        onDirectionLeft = { isShift ->
            if (zoomState.scale > 1.0f) {
                onZoomScroll(false, null, isShift)
            } else {
                if (isShift) onStepPage(-10) else onStepPage(-1)
            }
        },
        onDirectionRight = { isShift ->
            if (zoomState.scale > 1.0f) {
                onZoomScroll(true, null, isShift)
            } else {
                if (isShift) onStepPage(10) else onStepPage(1)
            }
        },
        onForward = { onStepPage(+1) },
        onBackward = { onStepPage(-1) },
        onForwardSkip = { onStepPage(+10) },
        onBackwardSkip = { onStepPage(-10) },
        onForwardLast = { onStepPage(Int.MAX_VALUE) },
        onBackwardFirst = { onStepPage(-Int.MAX_VALUE) },
        //onNumber = {},
        //onDelete = {},
        onMenu = onShowMenu,
        //onSearch = {},
    )
}

@Composable
private fun TreeScreenViewerContent(
    focusRequester: FocusRequester,
    pagerState: PagerState,
    zoomState: ZoomState,
    fileList: List<FileModel>,
    onStepPage: (step: Int) -> Unit,
    onClickShowOverlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier
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
                            if (offset.x <= width * 0.2f) {
                                onStepPage(-1)
                            } else if (offset.x >= width * 0.80f) {
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
    sortMenuExpanded: MutableState<Boolean>,
    sortState: State<SortModel>,
    onSetSort: (SortModel) -> Unit,
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
            actions = {
                TreeSortAction(
                    menuExpanded = sortMenuExpanded,
                    sortState = sortState,
                    onSetSort = onSetSort,
                )
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
            pagerState = rememberPagerState(pageCount = { list.size }),
            fileList = list,
            onChangeFile = {},
            sortState = remember { mutableStateOf(SortModel()) },
            onSetSort = {},
            onClose = {},
        )
    }
}
