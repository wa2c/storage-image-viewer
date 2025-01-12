package com.wa2c.android.storageimageviewer.presentation.ui.edit

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wa2c.android.storageimageviewer.common.values.StorageType
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.DividerNormal
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.toUri
import com.wa2c.android.storageimageviewer.presentation.ui.common.LoadingBox
import com.wa2c.android.storageimageviewer.presentation.ui.common.collectAsMutableState
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.Size
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.StorageImageViewerTheme
import com.wa2c.android.storageimageviewer.presentation.ui.edit.components.InputText

@Composable
fun EditScreen(
    viewModel: EditViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val snackBarHostState = remember { SnackbarHostState() }
    val storageState = viewModel.currentStorage.collectAsMutableState()
    val busyState = viewModel.busyState.collectAsStateWithLifecycle()

    val treeOpenLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri == null) {
            // todo message
            return@rememberLauncherForActivityResult
        }
        storageState.value = storageState.value.copy(uri = UriModel(uri.toString()))
    }

    EditScreenContainer(
        snackBarHostState = snackBarHostState,
        storageState = storageState,
        busyState = busyState,
        isNew = viewModel.isNew,
        onClickBack = onNavigateBack,
        onClickSave = { treeOpenLauncher.launch(storageState.value.uri.toUri()) }
    )

    LaunchedEffect(storageState.value.uri) {
        val uri = storageState.value.uri
        if (!uri.isInvalidUri) {
            val result = context.checkCallingUriPermission(uri.toUri(), Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (result != PackageManager.PERMISSION_GRANTED) {
                treeOpenLauncher.launch(uri.toUri())
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditScreenContainer(
    snackBarHostState: SnackbarHostState,
    storageState: MutableState<StorageModel>,
    busyState: State<Boolean>,
    isNew: Boolean,
    onClickBack: () -> Unit,
    onClickSave: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = onClickBack) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            EditScreenContent(
                storageState = storageState,
                isNew = isNew,
                onClickSelectStorage = {},
                onClickSave = onClickSave,
            )
            LoadingBox(
                isLoading = busyState.value
            )
        }
    }
}

@Composable
private fun EditScreenContent(
    storageState: MutableState<StorageModel>,
    isNew: Boolean,
    onClickSelectStorage: () -> Unit,
    onClickSave: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(Size.ScreenMargin)
                .weight(1f)
        ) {
            // Name
            InputText(
                title = "Name", // todo
                hint = "Name", // todo
                value = storageState.value.name,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                ),
            ) {
                storageState.value = storageState.value.copy(name = it)
            }

            // URI
            InputText(
                title = "URI", // todo
                hint = "URI", // todo
                value = storageState.value.uri.uri,
                modifier = Modifier
                    .clickable { onClickSelectStorage() },
                readonly = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                ),
            ) {
                storageState.value = storageState.value.copy(uri = UriModel(it))
            }
        }

        Column {
            DividerNormal()

            Button(
                onClick = onClickSave,
                shape = RoundedCornerShape(Size.SS),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Size.ScreenMargin, vertical = Size.S)
            ) {
                Text(text = "Save") // todo
            }
        }
    }
}

/**
 * Preview
 */
/**
 * Preview
 */
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
private fun HomeScreenContainerPreview() {
    StorageImageViewerTheme {
        val storage = StorageModel(
            id = "1",
            uri = UriModel(uri = "content://test1/"),
            name = "Test Storage 1",
            type = StorageType.SAF,
            sortOrder = 1,
        )

        EditScreenContainer(
            snackBarHostState = SnackbarHostState(),
            storageState = remember { mutableStateOf(storage) },
            busyState = remember { mutableStateOf(false) },
            isNew = true,
            onClickBack = {},
            onClickSave = {},
        )
    }
}
