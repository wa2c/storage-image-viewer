package com.wa2c.android.storageimageviewer.presentation.ui.tree

import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import coil.compose.AsyncImage
import com.wa2c.android.storageimageviewer.domain.model.FileModel
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

@Composable
fun TreeScreenViewerContent(
    modifier: Modifier = Modifier,
    viewerFileState: State<FileModel?>,
    fileListState: State<List<FileModel>>,
) {
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val pageList = fileListState.value.filter { !it.isDirectory }
    val pagerState = rememberPagerState(
        pageCount = { pageList.size },
        initialPage = viewerFileState.value?.let { pageList.indexOf(it) } ?: 0,
    )
    HorizontalPager(
        state = pagerState,
        modifier = modifier
            .focusRequester(focusRequester)
            .background(MaterialTheme.colorScheme.background)
    ) { page ->
        Box(
            modifier = Modifier
                .onKeyEvent { keyEvent ->
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_LEFT,
                        KeyEvent.KEYCODE_SYSTEM_NAVIGATION_LEFT,
                        KeyEvent.KEYCODE_MEDIA_PREVIOUS,
                        KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD,
                        KeyEvent.KEYCODE_NAVIGATE_PREVIOUS, -> {
                            scope.launch {
                                val pageIndex = max(pagerState.currentPage - 1, 0)
                                pagerState.animateScrollToPage(page = pageIndex)
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT,
                        KeyEvent.KEYCODE_SYSTEM_NAVIGATION_RIGHT,
                        KeyEvent.KEYCODE_MEDIA_NEXT,
                        KeyEvent.KEYCODE_MEDIA_STEP_FORWARD,
                        KeyEvent.KEYCODE_NAVIGATE_NEXT,
                        KeyEvent.KEYCODE_MEDIA_PLAY, -> {
                            scope.launch {
                                val pageIndex = min(pagerState.currentPage + 1, pageList.size - 1)
                                pagerState.animateScrollToPage(page = pageIndex)
                            }
                            true
                        }
                        KeyEvent.KEYCODE_PAGE_UP,
                        KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD, -> {
                            scope.launch {
                                val pageIndex = max(pagerState.currentPage - 10, 0)
                                pagerState.animateScrollToPage(page = pageIndex)
                            }
                            true
                        }
                        KeyEvent.KEYCODE_PAGE_DOWN,
                        KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD, -> {
                            scope.launch {
                                val pageIndex = min(pagerState.currentPage + 10, pageList.size - 1)
                                pagerState.animateScrollToPage(page = pageIndex)
                            }
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
                model = pageList.getOrNull(page)?.uri?.uri,
                contentDescription = pageList[page].name,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
