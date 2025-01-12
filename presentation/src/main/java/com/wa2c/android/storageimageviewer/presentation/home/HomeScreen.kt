package com.wa2c.android.storageimageviewer.presentation.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.theme.StorageImageViewerTheme

@Composable
fun HomeScreen(
    //viewModel: HomeViewModel = hiltViewModel(),
) {
    HomeScreenContainer(
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContainer(
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
            )
        },
//        floatingActionButton = {
//            MultiFloatingActionButton(
//                icon = ImageVector.vectorResource(id = R.drawable.ic_add_folder),
//                items = arrayListOf(
//                    FabItem(
//                        icon = painterResource(id = R.drawable.ic_edit),
//                        label = stringResource(id = R.string.home_add_connection_input),
//                        onFabItemClicked = onClickAddItem
//                    ),
//                    FabItem(
//                        icon =  painterResource(id = R.drawable.ic_search),
//                        label = stringResource(id = R.string.home_add_connection_search),
//                        onFabItemClicked = onClickSearchItem
//                    ),
//                ),
//                shape = CircleShape,
//                containerColor = MaterialTheme.colorScheme.primary,
//                showBackgroundEffect = false,
//            )
//        },
//        snackbarHost = { AppSnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(paddingValues)
        ) {

            HomeScreenStorageList(
            )
        }
    }
}

@Composable
private fun HomeScreenStorageList(

) {
    Text("Home")
}

@Composable
private fun HomeScreenStorageItem(

) {

}

/**
 * Preview
 */
@Preview(
    name = "Preview",
    group = "Group",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
private fun HomeScreenContainerPreview() {
    StorageImageViewerTheme {
        HomeScreenContainer(

        )
    }
}
