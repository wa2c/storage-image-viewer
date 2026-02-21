package com.wa2c.android.storageimageviewer.presentation.ui.settings

import androidx.lifecycle.ViewModel
import com.wa2c.android.storageimageviewer.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
): ViewModel() {


    /** UI Theme */
    val uiTheme = settingsRepository.uiTheme

}
