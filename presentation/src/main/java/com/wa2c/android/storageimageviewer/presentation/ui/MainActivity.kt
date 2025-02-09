package com.wa2c.android.storageimageviewer.presentation.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent {
            val navController = rememberNavController()
            AppTheme {
                MainNavHost(
                    navController = navController,
                )
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        when (event.keyCode) {
            KeyEvent.KEYCODE_DEL -> {
                return dispatchKeyEvent(event.replaceKeyCode(KeyEvent.KEYCODE_BACK))
            }
        }
//        when (event.scanCode) {
//            104 -> {  // PageUp
//                return dispatchKeyEvent(event.replaceKeyCode(KeyEvent.KEYCODE_PAGE_UP))
//            }
//            109 -> { // PageDown
//                return dispatchKeyEvent(event.replaceKeyCode(KeyEvent.KEYCODE_PAGE_DOWN))
//            }
//        }

        return super.dispatchKeyEvent(event)
    }

    private fun KeyEvent.replaceKeyCode(keyCode: Int): KeyEvent {
        return KeyEvent(
            downTime,
            eventTime,
            action,
            keyCode,
            repeatCount,
            metaState,
            deviceId,
            metaState,
            flags,
            source,
        )
    }

}
