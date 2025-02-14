package com.wa2c.android.storageimageviewer.presentation.ui.tree

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

fun Modifier.treeKeyControl(
    isPreview: Boolean = false,
    isLoading: Boolean = false,
    useVolume: Boolean = false,
    onEnter: (() -> Unit)? = null,
    onPlay: (() -> Unit)? = null,
    onDirectionCenter: (() -> Unit)? = null,
    onDirectionUp: ((isShift: Boolean) -> Unit)? = null,
    onDirectionDown: ((isShift: Boolean) -> Unit)? = null,
    onDirectionLeft:((isShift: Boolean) -> Unit)? = null,
    onDirectionRight: ((isShift: Boolean) -> Unit)? = null,
    onForward: (() -> Unit)? = null,
    onBackward: (() -> Unit)? = null,
    onForwardSkip: (() -> Unit)? = null,
    onBackwardSkip: (() -> Unit)? = null,
    onForwardLast: (() -> Unit)? = null,
    onBackwardFirst: (() -> Unit)? = null,
    onNumber: ((Int) -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onMenu: (() -> Unit)? = null,
    onSearch: (() -> Unit)? = null,
): Modifier {
    return if (isPreview) {
        this::onPreviewKeyEvent
    } else {
        this::onKeyEvent
    }.invoke { keyEvent ->
        if (keyEvent.type != KeyEventType.KeyDown) return@invoke false

        keyEvent.key.toNumber()?.let { number ->
            onNumber?.let {
                onNumber(number)
            }
            return@invoke true
        }

        when (keyEvent.key) {
            Key.DirectionCenter -> {
                isLoading || keyAction(onDirectionCenter)
            }
            Key.DirectionLeft -> {
                isLoading || onDirectionLeft?.invoke(keyEvent.isShiftPressed)?.let { true } ?: false
            }
            Key.DirectionRight -> {
                isLoading || onDirectionRight?.invoke(keyEvent.isShiftPressed)?.let { true } ?: false
            }
            Key.DirectionUp -> {
                isLoading || onDirectionUp?.invoke(keyEvent.isShiftPressed)?.let { true } ?: false
            }
            Key.DirectionDown -> {
                isLoading || onDirectionDown?.invoke(keyEvent.isShiftPressed)?.let { true } ?: false
            }
            Key.MediaPrevious,
            Key.MediaRewind,
            Key.MediaStepBackward,
            Key.NavigatePrevious,
            Key.SystemNavigationLeft, -> {
                isLoading || keyAction(onBackward)
            }
            Key.MediaNext,
            Key.MediaFastForward,
            Key.MediaStepForward,
            Key.NavigateNext,
            Key.SystemNavigationRight, -> {
                isLoading ||  keyAction(onForward)
            }
            Key.PageUp,
            Key.MediaSkipBackward, -> {
                isLoading || keyAction(onBackwardSkip)
            }
            Key.PageDown,
            Key.MediaSkipForward, -> {
                isLoading || keyAction(onForwardSkip)
            }
            Key.LeftBracket,
            Key.MoveHome, -> {
                isLoading || keyAction(onBackwardFirst)
            }
            Key.RightBracket,
            Key.MoveEnd, -> {
                isLoading || keyAction(onForwardLast)
            }
            Key.VolumeUp -> {
                if (useVolume) {
                    isLoading || keyAction(onBackward)
                } else {
                    false
                }
            }
            Key.VolumeDown -> {
                if (useVolume) {
                    isLoading || keyAction(onForward)
                } else {
                    false
                }
            }
            Key.Enter,
            Key.NumPadEnter, -> {
                isLoading || keyAction(onEnter)
            }
            Key.Spacebar,
            Key.MediaPlay,
            Key.MediaPlayPause, -> {
                isLoading || keyAction(onPlay)
            }
            Key.Backspace,
            Key.Delete, -> {
                keyAction(onDelete)
            }
            Key.M,
            Key.Menu -> {
                keyAction(onMenu)
            }
            Key.S,
            Key.Search -> {
                keyAction(onSearch)
            }
            else -> {
                false
            }
        }
    }
}

private fun keyAction(action: (() -> Unit)?): Boolean {
    if (action != null) {
        action()
        return true
    } else {
        return false
    }
}

fun Key.toNumber(): Int? {
    return when (this) {
        Key.Zero, Key.NumPad0 -> 0
        Key.One, Key.NumPad1 -> 1
        Key.Two, Key.NumPad2 -> 2
        Key.Three, Key.NumPad3 -> 3
        Key.Four, Key.NumPad4 -> 4
        Key.Five, Key.NumPad5 -> 5
        Key.Six, Key.NumPad6 -> 6
        Key.Seven, Key.NumPad7 -> 7
        Key.Eight, Key.NumPad8 -> 8
        Key.Nine, Key.NumPad9 -> 9
        else -> null
    }
}
