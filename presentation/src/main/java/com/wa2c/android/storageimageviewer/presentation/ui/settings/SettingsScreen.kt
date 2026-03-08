package com.wa2c.android.storageimageviewer.presentation.ui.settings

import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
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
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.wa2c.android.storageimageviewer.common.values.Language
import com.wa2c.android.storageimageviewer.common.values.UiTheme
import com.wa2c.android.storageimageviewer.presentation.R
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.LocalSnackbarHostState
import com.wa2c.android.storageimageviewer.presentation.ui.common.Extensions.focusItemStyle
import com.wa2c.android.storageimageviewer.presentation.ui.common.MessageExt.toAppMessage
import com.wa2c.android.storageimageviewer.presentation.ui.common.MutableStateAdapter
import com.wa2c.android.storageimageviewer.presentation.ui.common.ValueResource.labelRes
import com.wa2c.android.storageimageviewer.presentation.ui.common.collectForSettings
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppSize
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppTheme
import com.wa2c.android.storageimageviewer.presentation.ui.settings.components.OptionItem
import com.wa2c.android.storageimageviewer.presentation.ui.settings.components.SettingsItem
import com.wa2c.android.storageimageviewer.presentation.ui.settings.components.SettingsSingleChoiceItem
import com.wa2c.android.storageimageviewer.presentation.ui.settings.components.TitleItem

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val language = remember {
        MutableStateAdapter(
            state = mutableStateOf(Language.findByCodeOrDefault(AppCompatDelegate.getApplicationLocales().toLanguageTags())),
            mutate = { value -> AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(value.code)) },
        )
    }

    SettingsScreenContainer(
        theme = viewModel.uiTheme.collectForSettings(scope),
        language = language,
        onStartIntent = { intent ->
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                viewModel.sendMessage(e.toAppMessage())
            }
        },
        onClickBack = onNavigateBack,
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreenContainer(
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
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_settings),
                            contentDescription = null,
                            modifier = Modifier
                                .size(AppSize.IconSmall),
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
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (showLibraries) {
                                showLibraries = false
                            } else {
                                onClickBack()
                            }
                        },
                        modifier = Modifier
                            .focusItemStyle(shape = CircleShape),
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_back),
                            contentDescription = "back",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(LocalSnackbarHostState.current) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(paddingValues),
        ) {
            if (showLibraries) {
                val libraries by produceLibraries(R.raw.aboutlibraries)
                LibrariesContainer(
                    libraries = libraries,
                    modifier = Modifier
                        .fillMaxSize(),
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
        // Settings Title
        item {
            TitleItem(text = stringResource(id = R.string.settings_section_set))
        }
        // UI Theme
        item {
            SettingsSingleChoiceItem(
                title = stringResource(id = R.string.settings_set_theme),
                items = UiTheme.entries.map { OptionItem(it, stringResource(it.labelRes)) },
                selectedItem = theme,
            )
        }
        // Language
        item {
            SettingsSingleChoiceItem(
                title = stringResource(id = R.string.settings_set_language),
                items = Language.entries.map { OptionItem(it, stringResource(it.labelRes)) },
                selectedItem = language,
            )
        }

        // Information Title
        item {
            TitleItem(text = stringResource(id = R.string.settings_section_info))
        }
        // Libraries
        item {
            SettingsItem(text = stringResource(id = R.string.settings_info_libraries)) {
                onShowLibraries()
            }
        }
        // Source Code
        item {
            SettingsItem(text = stringResource(id = R.string.settings_info_source)) {
                onStartIntent(
                    Intent(
                        Intent.ACTION_VIEW,
                        "https://github.com/wa2c/storage-image-viewer".toUri(),
                    ),
                )
            }
        }
        // App
        item {
            SettingsItem(text = stringResource(id = R.string.settings_info_app)) {
                onStartIntent(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        ("package:" + context.packageName).toUri(),
                    ),
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
            theme = remember { mutableStateOf(UiTheme.DEFAULT) },
            language = remember { mutableStateOf(Language.default) },
            onStartIntent = {},
            onClickBack = {},
        )
    }
}
