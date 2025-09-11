package com.wa2c.android.storageimageviewer.presentation.ui.settings

import androidx.lifecycle.ViewModel
import com.wa2c.android.storageimageviewer.domain.repository.SettingsRepository
import com.wa2c.android.storageimageviewer.presentation.ui.common.MainCoroutineScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
): ViewModel(), CoroutineScope by MainCoroutineScope() {


    /** UI Theme */
    val uiTheme = settingsRepository.uiTheme

}
