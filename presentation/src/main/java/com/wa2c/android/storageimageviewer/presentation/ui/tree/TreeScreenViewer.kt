package com.wa2c.android.storageimageviewer.presentation.ui.tree

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import coil.compose.AsyncImage
import com.wa2c.android.storageimageviewer.domain.model.FileModel

@Composable
fun TreeScreenViewer(
    modifier: Modifier = Modifier,
    viewerFileState: State<FileModel?>,
    fileListState: State<List<FileModel>>,
) {
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
                    .background(MaterialTheme.colorScheme.background)
                    .animateContentSize { initialValue, targetValue ->  }
                ,
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    AsyncImage(
                        model = pageList.getOrNull(page)?.uri?.uri,
                        contentDescription = pageList[page].name,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        },
    )
}
