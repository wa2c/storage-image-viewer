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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.toSize
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.domain.model.FileModel
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.toUri
import com.wa2c.android.storageimageviewer.presentation.ui.common.components.LoadingBox
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppColor
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppSize
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTheme
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTypography
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeScreenItemData
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeScreenOption
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.ZoomState
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@Composable
fun TreeScreenViewer(
    viewModel: TreeViewModel = hiltViewModel(),
) {
    val treeState = viewModel.currentTree.collectAsStateWithLifecycle()
    val focusedFile = viewModel.focusedFile.collectAsStateWithLifecycle()
    val optionState = viewModel.screenOption.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(
        pageCount = { treeState.value.imageFileList.size },
        initialPage = treeState.value.getImageIndex(focusedFile.value),
    )

    TreeScreenViewerContainer(
        pagerState = pagerState,
        treeState = treeState,
        optionState = optionState,
        onSetOption = viewModel::setOption,
        onClose = viewModel::closeViewer,
    )

    LaunchedEffect(Unit) {
        launch {
            snapshotFlow { treeState.value }.collect { tree ->
                pagerState.requestScrollToPage(tree.getImageIndex(viewModel.focusedFile.value))
            }
        }
        launch {
            snapshotFlow { pagerState.currentPage }.collect { page ->
                viewModel.focusFile(treeState.value.getImageFile(page))
            }
        }
    }
}

@Suppress("DEPRECATION")
@Composable
fun TreeScreenViewerContainer(
    pagerState: PagerState,
    treeState:  State<TreeScreenItemData>,
    optionState: State<TreeScreenOption>,
    onSetOption: (TreeScreenOption) -> Unit,
    onClose: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val zoomState = rememberZoomState()
    var size = remember { androidx.compose.ui.geometry.Size.Unspecified }
    val sortMenuExpanded = remember { mutableStateOf(false) }
    val systemUiController = rememberSystemUiController()
    if (optionState.value.viewerOption.showOverlay) {
        systemUiController.isSystemBarsVisible = true
        systemUiController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
    } else {
        systemUiController.isSystemBarsVisible = false
        systemUiController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    val pageRange = 0..<pagerState.pageCount
    val animatedColor = remember { Animatable(AppColor.ViewerOverlayBackground) }

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
            treeState = treeState,
            onStepPage = { step ->
                scope.launch {
                    // Change page
                    val page = (pagerState.currentPage + step).coerceIn(pageRange)
                    if (page != pagerState.currentPage) pagerState.animateScrollToPage(page = page)
                    else animatedColor.flashPage()
                }
            },
            onClickShowOverlay = {
                optionState.value.let {
                    val viewerOption = it.viewerOption.copy(showOverlay = !it.viewerOption.showOverlay)
                    onSetOption(it.copy(viewerOption = viewerOption))
                }
            },
            modifier = Modifier
                .keyControl(
                    zoomState = zoomState,
                    useVolume = optionState.value.viewerOption.volumeScroll,
                    onStepPage = { step ->
                        scope.launch {
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
                        optionState.value.let {
                            val viewerOption = it.viewerOption.copy(showOverlay = !it.viewerOption.showOverlay)
                            onSetOption(it.copy(viewerOption = viewerOption))
                        }
                    },
                    onShowMenu = {
                        optionState.value.let {
                            val viewerOption = it.viewerOption.copy(showOverlay = true)
                            onSetOption(it.copy(viewerOption = viewerOption))
                        }
                        sortMenuExpanded.value = true
                    }
                )
        )

        // Page
        if (optionState.value.viewerOption.showPage) {
            Box(
                contentAlignment = Alignment.BottomStart,
                modifier = Modifier
                    .let {
                        if (optionState.value.viewerOption.showOverlay) it.navigationBarsPadding()
                        else it.padding(bottom = AppSize.M)
                    }
                    .padding(start = AppSize.M)
            ) {
                Text(
                    text = "${(pagerState.currentPage + 1)} / ${pagerState.pageCount}",
                    style = AppTypography.labelMedium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppSize.L))
                        .background(color = animatedColor.value)
                        .padding(horizontal = AppSize.S, AppSize.SS)
                )
            }
        }

        // Overlay
        AnimatedVisibility(
            visible = optionState.value.viewerOption.showOverlay,
            enter = fadeIn(),
            exit = fadeOut(),
            content = {
                TreeScreenViewerOverlay(
                    file = treeState.value.imageFileList[pagerState.currentPage],
                    sortMenuExpanded = sortMenuExpanded,
                    optionState = optionState,
                    onSetOption = onSetOption,
                    onClose = onClose,
                )
            },
        )
    }

    BackHandler {
        if (zoomState.scale > 1.0f) {
            scope.launch { zoomState.reset() }
        } else {
            onClose()
        }
    }
}

/**
 * Flash page background (Indicates that page transition is not possible.)
 */
private suspend fun Animatable<Color, AnimationVector4D>.flashPage() {
    animateTo(
        targetValue = AppColor.ViewerOverlayFlash,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
    )
    animateTo(
        targetValue = AppColor.ViewerOverlayBackground,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
    )
}

private fun Modifier.keyControl(
    zoomState: ZoomState,
    useVolume: Boolean,
    onStepPage: (step: Int) -> Unit,
    onZoom: () -> Unit,
    onZoomScroll: (isXPositive: Boolean?, isYPositive: Boolean?, isSkip: Boolean?) -> Unit,
    onShowOverlay: () -> Unit,
    onShowMenu: () -> Unit,
): Modifier {
    return this.treeKeyControl(
        useVolume = useVolume,
        onEnter = onShowOverlay,
        onPlay = onZoom,
        onDirectionUp = { isShift ->
            if (zoomState.scale > 1.0f) {
                onZoomScroll(null, false, isShift)
            } else {
                onStepPage(-10)
            }
        },
        onDirectionDown = { isShift ->
            if (zoomState.scale > 1.0f) {
                onZoomScroll(null, true, isShift)
            } else {
                onStepPage(+10)
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
        onMenu = onShowMenu,
    )
}

@Composable
private fun TreeScreenViewerContent(
    focusRequester: FocusRequester,
    pagerState: PagerState,
    zoomState: ZoomState,
    treeState: State<TreeScreenItemData>,
    onStepPage: (step: Int) -> Unit,
    onClickShowOverlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageFileList = treeState.value.imageFileList
    HorizontalPager(
        state = pagerState,
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable()
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) { page ->
        val file = imageFileList.getOrNull(page)
        SubcomposeAsyncImage(
            model = file?.uri?.toUri(),
            contentDescription = file?.name,
            loading = {
                LoadingBox(isLoading = true)
            },
            modifier = Modifier
                .fillMaxSize()
                .zoomable(
                    zoomState = zoomState,
                    onTap = { offset ->
                        if (pagerState.isScrollInProgress) return@zoomable
                        val width = Resources.getSystem().displayMetrics.widthPixels
                        if (offset.x <= width * 0.2f) {
                            onStepPage(-1)
                        } else if (offset.x >= width * 0.80f) {
                            onStepPage(+1)
                        } else {
                            onClickShowOverlay()
                        }
                    },
                ),
        )
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
    optionState: State<TreeScreenOption>,
    onSetOption: (TreeScreenOption) -> Unit,
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
                TreeScreenMenu(
                    menuExpanded = sortMenuExpanded,
                    optionState = optionState,
                    onSetOption = onSetOption,
                )
            },
            colors = TopAppBarDefaults.topAppBarColors( containerColor = AppColor.ViewerOverlayBackground),
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
    AppTheme {
        val storage = StorageModel(
            id = "1",
            name = "Test Storage 1",
            uri = UriModel(uri = "content://test1/"),
            rootUri = UriModel(uri = "content://test1/"),
            type = StorageType.SAF,
            sortOrder = 1,
        )
        val list = listOf(
            FileModel(
                storage = storage,
                uri = UriModel( "content://test1/"),
                isDirectory = true,
                name = "Test directory",
                mimeType = "",
                size = 0,
                dateModified = 0,
            ),
            FileModel(
                storage = storage,
                uri = UriModel( "content://test2/"),
                isDirectory = true,
                name = "Test file.jpg",
                mimeType = "image/jpeg",
                size = 10000,
                dateModified = 0,
            ),
        )

        TreeScreenViewerContainer(
            pagerState = rememberPagerState(pageCount = { list.size }),
            treeState = remember { mutableStateOf(TreeScreenItemData(fileList = list)) },
            optionState = remember { mutableStateOf(TreeScreenOption()) },
            onSetOption = {},
            onClose = {},
        )
    }
}
