package com.wa2c.android.storageimageviewer.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wa2c.android.storageimageviewer.common.values.UiTheme
import com.wa2c.android.storageimageviewer.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
): ViewModel() {


    /** UI Theme */
    val uiThemeFlow = settingsRepository.uiTheme.data
        .stateIn(viewModelScope, SharingStarted.Eagerly, UiTheme.DEFAULT)

}
