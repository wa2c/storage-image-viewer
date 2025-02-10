package com.wa2c.android.storageimageviewer.presentation.ui.settings

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.LibraryDefaults
import com.wa2c.android.storageimageviewer.common.values.Language
import com.wa2c.android.storageimageviewer.common.values.UiTheme
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.MutableStateAdapter
import com.wa2c.android.storageimageviewer.presentation.ui.common.ValueResource.labelRes
import com.wa2c.android.storageimageviewer.presentation.ui.common.ValueResource.mode
import com.wa2c.android.storageimageviewer.presentation.ui.common.showMessage
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppSize
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTheme
import com.wa2c.android.storageimageviewer.presentation.ui.settings.components.OptionItem
import com.wa2c.android.storageimageviewer.presentation.ui.settings.components.SettingsItem
import com.wa2c.android.storageimageviewer.presentation.ui.settings.components.SettingsSingleChoiceItem
import com.wa2c.android.storageimageviewer.presentation.ui.settings.components.TitleItem
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    SettingsScreenContainer(
        snackBarHostState = snackBarHostState,
        theme = MutableStateAdapter(
            state = viewModel.uiThemeFlow.collectAsStateWithLifecycle(UiTheme.DEFAULT),
            mutate = { value ->
                viewModel.setUiTheme(value)
                AppCompatDelegate.setDefaultNightMode(value.mode)
            },
        ),
        language = MutableStateAdapter(
            state =  remember {
                mutableStateOf(Language.findByCodeOrDefault(AppCompatDelegate.getApplicationLocales().toLanguageTags()))
            },
            mutate = { value ->
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(value.code))
            },
        ),
        onStartIntent = { intent ->
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                scope.launch {
                    snackBarHostState.showMessage(context, Result.failure(e))
                }
            }
        },
        onClickBack = onNavigateBack,
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreenContainer(
    snackBarHostState: SnackbarHostState,
    theme: MutableState<UiTheme>,
    language: MutableState<Language>,
    onStartIntent: (Intent) -> Unit,
    onClickBack: () -> Unit,
) {
    var showLibraries by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_settings),
                            contentDescription = null,
                            modifier = Modifier
                                .size(AppSize.IconSmall)
                        )
                        Text(
                            text = stringResource(id = R.string.settings_title),
                            maxLines = 1,
                            modifier = Modifier
                                .padding(start = AppSize.S)
                                .basicMarquee(),
                        )
                    }
                },
                //colors = getAppTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = {
                        if (showLibraries) {
                            showLibraries = false
                        } else {
                            onClickBack()
                        }
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_back),
                            contentDescription = "back",
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(paddingValues)
        ) {
            if (showLibraries) {
                LibrariesContainer(
                    colors = LibraryDefaults.libraryColors(
                        backgroundColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                )
            } else {
                SettingsList(
                    theme = theme,
                    language = language,
                    onShowLibraries = { showLibraries = true },
                    onStartIntent = onStartIntent,
                )
            }
        }
    }

    // Back button
    BackHandler {
        if (showLibraries) {
            showLibraries = false
        } else {
            onClickBack()
        }
    }
}

/**
 * Settings List
 */
@Composable
private fun SettingsList(
    theme: MutableState<UiTheme>,
    language: MutableState<Language>,
    onShowLibraries: () -> Unit,
    onStartIntent: (Intent) -> Unit,
) {

    val context = LocalContext.current

    LazyColumn {
        // Screen
        item {
            // Settings Title
            TitleItem(text = stringResource(id = R.string.settings_section_set))

            // UI Theme
            SettingsSingleChoiceItem(
                title = stringResource(id = R.string.settings_set_theme),
                items = UiTheme.entries.map { OptionItem(it, stringResource(it.labelRes)) },
                selectedItem = theme,
            )

            // Language
            SettingsSingleChoiceItem(
                title = stringResource(id = R.string.settings_set_language),
                items = Language.entries.map { OptionItem(it, stringResource(it.labelRes)) },
                selectedItem = language,
            )

            // Information Title
            TitleItem(text = stringResource(id = R.string.settings_section_info))

            // Libraries
            SettingsItem(text = stringResource(id = R.string.settings_info_libraries)) {
                onShowLibraries()
            }

            // Source Code
            SettingsItem(text = stringResource(id = R.string.settings_info_source)) {
                onStartIntent(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/wa2c/storage-image-viewer")
                    )
                )
            }

            // App
            SettingsItem(text = stringResource(id = R.string.settings_info_app)) {
                onStartIntent(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + context.packageName)
                    )
                )
            }

        }
    }
}

/**
 * Preview
 */
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
private fun SettingsScreenContainerPreview() {
    AppTheme {
        SettingsScreenContainer(
            snackBarHostState = remember { SnackbarHostState() },
            theme = remember { mutableStateOf(UiTheme.DEFAULT) },
            language = remember { mutableStateOf(Language.default) },
            onStartIntent = {},
            onClickBack = {},
        )
    }
}
