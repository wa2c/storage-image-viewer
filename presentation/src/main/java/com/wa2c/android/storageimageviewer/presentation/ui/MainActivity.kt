package com.wa2c.android.storageimageviewer.presentation.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.wa2c.android.storageimageviewer.common.values.UiTheme
import com.wa2c.android.storageimageviewer.presentation.ui.common.ValueResource.isDark
import com.wa2c.android.storageimageviewer.presentation.ui.common.ValueResource.mode
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** Main View Model */
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        AppCompatDelegate.setDefaultNightMode(mainViewModel.uiThemeFlow.value.mode)

        setContent {
            val navController = rememberNavController()
            AppTheme(
                darkTheme = mainViewModel.uiThemeFlow.collectAsStateWithLifecycle().value.isDark()
            ) {
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
