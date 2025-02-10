package com.wa2c.android.storageimageviewer.presentation.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.wa2c.android.storageimageviewer.presentation.ui.common.ScreenParam
import com.wa2c.android.storageimageviewer.presentation.ui.home.HomeScreen
import com.wa2c.android.storageimageviewer.presentation.ui.settings.SettingsScreen
import com.wa2c.android.storageimageviewer.presentation.ui.tree.TreeScreen

@Composable
internal fun MainNavHost(
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
                onSelectStorage = {
                    navController.navigate(route = "${ScreenParam.TreeScreenRouteName}?${ScreenParam.ScreenParamId}=${it.id}")
                },
                onNavigateSettings = {
                    navController.navigate(ScreenParam.SettingsScreenRouteName)
                },
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
                onNavigateBack = {
                    navController.popBackStack(ScreenParam.TreeScreenRouteName, true)
                }
            )
        }

        // Settings Screen
        composable(
            route = ScreenParam.SettingsScreenRouteName,
        ) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack(ScreenParam.SettingsScreenRouteName, true)
                }
            )
        }
    }


}
