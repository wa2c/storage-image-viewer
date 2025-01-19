package com.wa2c.android.storageimageviewer.presentation.ui.tree

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import coil.compose.AsyncImage
import com.wa2c.android.storageimageviewer.domain.model.FileModel
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Size
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TreeScreenViewerContainer(
    modifier: Modifier = Modifier,
    fileState: State<FileModel?>,
    fileListState: State<List<FileModel>>,
) {
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val fileList = fileListState.value.filter { !it.isDirectory }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        var visibleOverlay by remember { mutableStateOf(false) }
        val pagerState = rememberPagerState(
            pageCount = { fileList.size },
            //initialPage = fileList.indexOf(file).takeIf { it >= 0 } ?: 0,
            initialPage = 1
        )
        val file = fileList.getOrNull(pagerState.currentPage) ?: return@Box

        TreeScreenViewerContent(
            focusRequester = focusRequester,
            pagerState = pagerState,
            file = file,
            onStepPage = { step ->
                scope.launch {
                    val page = (pagerState.currentPage + step).coerceIn(0, pagerState.pageCount - 1)
                    pagerState.animateScrollToPage(page = page)
                }
            },
            onClickCenter = { visibleOverlay = true }
        )

        AnimatedVisibility(
            visible = visibleOverlay,
            enter = fadeIn(),
            exit = fadeOut(),
            content = {
                TreeScreenViewerOverlay(
                    file = file,
                    onDismiss = { visibleOverlay = false }
                )
            },
        )

    }
}

@Composable
private fun TreeScreenViewerContent(
    focusRequester: FocusRequester,
    pagerState: PagerState,
    file: FileModel,
    onStepPage: (step: Int) -> Unit,
    onClickCenter: () -> Unit,
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .focusRequester(focusRequester)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) { page ->
        Box(
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
                        Key.MediaRewind,
                        Key.MediaStepBackward,
                        Key.NavigatePrevious,
                        Key.SystemNavigationLeft, -> {
                            if (keyEvent.isShiftPressed) onStepPage(-10) else onStepPage(-1)
                            true
                        }
                        Key.DirectionRight,
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
                        else -> {
                            false
                        }
                    }
                }
                .focusable()
                .fillMaxSize()
        ) {
            AsyncImage(
                model = file.uri,
                contentDescription = file.name,
                modifier = Modifier
                    .fillMaxSize(),
            )
//            Row(
//
//            ) {
//                Box(
//                    modifier = Modifier
//                        .clickable {  }
//                        .weight(0.2f)
//                        .background(color = Color.Red)
//                )
//
//                Box(
//                    modifier = Modifier
//                        .clickable { onClickCenter() }
//                        .weight(0.6f)
//                )
//
//                Box(
//                    modifier = Modifier
//                        .clickable {  }
//                        .weight(0.2f)
//                )
//            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}


@Composable
private fun TreeScreenViewerOverlay(
    file: FileModel,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row {
            Text(
                text = file.name,
                modifier = Modifier
                    .padding(horizontal = Size.M, vertical = Size.S)
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
        ) {

        }

        Row {
            Text(
                text = file.name,
                modifier = Modifier
                    .padding(horizontal = Size.M, vertical = Size.S)
            )
        }
    }


    LaunchedEffect(Unit) {
        delay(3000)
        onDismiss()
    }
}
