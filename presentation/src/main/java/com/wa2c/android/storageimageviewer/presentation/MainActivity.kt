package com.wa2c.android.storageimageviewer.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.wa2c.android.storageimageviewer.presentation.home.HomeScreen
import com.wa2c.android.storageimageviewer.presentation.common.theme.StorageImageViewerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            StorageImageViewerTheme {
                MainNavHost(
                    navController = navController,
                )
            }
        }
    }
}
