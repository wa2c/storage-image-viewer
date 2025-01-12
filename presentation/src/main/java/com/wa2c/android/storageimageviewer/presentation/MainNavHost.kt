package com.wa2c.android.storageimageviewer.presentation

import androidx.compose.runtime.Composable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.wa2c.android.storageimageviewer.presentation.home.HomeScreen

@Composable
internal fun MainNavHost(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = HomeScreenName,
    ) {
        // Home Screen
        composable(
            route = HomeScreenName,
        ) {
            HomeScreen(
                onAddStorage = {
                    navController.navigate(route = "$EditScreenRouteName?$EditScreenPramUri=${it.uri}")
                },
                onSelectStorage = {
                    navController.navigate(route = "$EditScreenRouteName?$EditScreenParamId=${it.id}")
                }
            )
        }

        // Edit Screen
        composable(
            route = "$EditScreenRouteName?$EditScreenPramUri={$EditScreenPramUri}&$EditScreenParamId={$EditScreenParamId}",
        ) {

        }
    }
}

private const val HomeScreenName = "home"
private const val EditScreenRouteName = "edit"

private const val EditScreenParamId = "id"
private const val EditScreenPramUri = "uri"
