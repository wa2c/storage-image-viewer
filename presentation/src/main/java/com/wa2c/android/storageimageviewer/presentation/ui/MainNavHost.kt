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
                onSelectStorage = {
                    navController.navigate(route = "${ScreenParam.EditScreenRouteName}?${ScreenParam.EditScreenParamId}=${it.id}")
                }
            )
        }

        // Edit Screen
        composable(
            route = "${ScreenParam.EditScreenRouteName}?${ScreenParam.EditScreenParamId}={${ScreenParam.EditScreenParamId}}",
            arguments = listOf(
                navArgument(ScreenParam.EditScreenParamId) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            EditScreen(
                onNavigateBack = {}
            )
        }
    }
}
