package com.wa2c.android.storageimageviewer.presentation.ui.common

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.wa2c.android.storageimageviewer.domain.model.SettingsModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Collect flow
 */
fun <T> Flow<T>.collectIn(
    lifecycleOwner: LifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    observer: suspend (T) -> Unit = { },
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(state) {
            this@collectIn.collect { observer(it) }
        }
    }
}

internal class MutableStateAdapter<T>(
    private val state: State<T>,
    private val mutate: (T) -> Unit
) : MutableState<T> {

    override var value: T
        get() = state.value
        set(value) { mutate(value) }

    override fun component1(): T = value
    override fun component2(): (T) -> Unit = { value = it }
}


@SuppressLint("UnrememberedMutableState")
@Composable
internal fun <T> MutableStateFlow<T>.collectAsMutableState(
    context: CoroutineContext = EmptyCoroutineContext
): MutableState<T> = MutableStateAdapter(
    state = collectAsStateWithLifecycle(context = context),
    mutate = { value = it }
)

@SuppressLint("UnrememberedMutableState")
@Composable
internal fun <T> SettingsModel<T>.collectForSettings(scope: CoroutineScope): MutableState<T> = MutableStateAdapter(
    state = this.data.collectAsStateWithLifecycle(runBlocking { this@collectForSettings.get() }),
    mutate = {  scope.launch { this@collectForSettings.set(it) } }
)
