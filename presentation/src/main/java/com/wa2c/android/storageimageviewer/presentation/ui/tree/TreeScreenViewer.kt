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

@Composable
fun TreeScreenViewer(
    modifier: Modifier = Modifier,
    viewerFileState: State<FileModel?>,
    fileListState: State<List<FileModel>>,
) {
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val visibleState = rememberUpdatedState { MutableTransitionState(viewerFileState.value != null) }
    AnimatedVisibility(
        visibleState = visibleState.value(),
        enter = slideInVertically(
            animationSpec = tween(
                durationMillis = 500,
                easing = LinearEasing,
            ),
            initialOffsetY = { fullHeight -> fullHeight },
        ),
        exit = slideOutVertically(
            animationSpec = tween(
                durationMillis = 500,
                easing = LinearEasing,
            ),
            targetOffsetY = { fullHeight -> fullHeight },
        ),
        content = {
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
                    .animateContentSize { initialValue, targetValue ->  }
                ,
            ) { page ->
                Box(
                    modifier = Modifier
                        .onKeyEvent { keyEvent ->
                            when (keyEvent.nativeKeyEvent.keyCode) {
                                KeyEvent.KEYCODE_DPAD_LEFT,
                                KeyEvent.KEYCODE_SYSTEM_NAVIGATION_LEFT,
                                KeyEvent.KEYCODE_MEDIA_PREVIOUS,
                                KeyEvent.KEYCODE_NAVIGATE_PREVIOUS, -> {
                                    if (pagerState.currentPage > 0) {
                                        scope.launch {
                                            pagerState.animateScrollToPage(page = pagerState.currentPage - 1)
                                        }
                                    }
                                    true
                                }
                                KeyEvent.KEYCODE_DPAD_RIGHT,
                                KeyEvent.KEYCODE_SYSTEM_NAVIGATION_RIGHT,
                                KeyEvent.KEYCODE_MEDIA_NEXT,
                                KeyEvent.KEYCODE_NAVIGATE_NEXT, -> {
                                    if (pagerState.currentPage <= pageList.size - 1) {
                                        scope.launch {
                                            pagerState.animateScrollToPage(page = pagerState.currentPage + 1)
                                        }
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
        },
    )


}
