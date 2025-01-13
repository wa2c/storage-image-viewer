package com.wa2c.android.storageimageviewer.presentation.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.wa2c.android.storageimageviewer.presentation.ui.common.ScreenParam
import com.wa2c.android.storageimageviewer.presentation.ui.edit.EditScreen
import com.wa2c.android.storageimageviewer.presentation.ui.home.HomeScreen
import com.wa2c.android.storageimageviewer.presentation.ui.tree.TreeScreen

@Composable
internal fun MainNavHost(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = ScreenParam.HomeScreenName,
    ) {
        // Home Screen
        composable(
            route = ScreenParam.HomeScreenName,
        ) {
            HomeScreen(
                onAddStorage = {
                    navController.navigate(route = ScreenParam.EditScreenRouteName)
                },
                onEditStorage = {
                    navController.navigate(route = "${ScreenParam.EditScreenRouteName}?${ScreenParam.ScreenParamId}=${it.id}")
                },
                onSelectStorage = {
                    navController.navigate(route = "${ScreenParam.TreeScreenRouteName}?${ScreenParam.ScreenParamId}=${it.id}")
                }
            )
        }

        // Edit Screen
        composable(
            route = "${ScreenParam.EditScreenRouteName}?${ScreenParam.ScreenParamId}={${ScreenParam.ScreenParamId}}",
            arguments = listOf(
                navArgument(ScreenParam.ScreenParamId) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            EditScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Tree Screen
        composable(
            route = "${ScreenParam.TreeScreenRouteName}?${ScreenParam.ScreenParamId}={${ScreenParam.ScreenParamId}}",
            arguments = listOf(
                navArgument(ScreenParam.ScreenParamId) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            TreeScreen(
                onNavigateViewer = { fileList, selectedFile ->

                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
