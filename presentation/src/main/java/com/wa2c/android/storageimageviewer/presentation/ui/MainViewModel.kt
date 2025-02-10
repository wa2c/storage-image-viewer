package com.wa2c.android.storageimageviewer.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wa2c.android.storageimageviewer.common.values.UiTheme
import com.wa2c.android.storageimageviewer.domain.repository.SettingsRepository
import com.wa2c.android.storageimageviewer.domain.repository.StorageRepository
import com.wa2c.android.storageimageviewer.presentation.ui.common.MainCoroutineScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
): ViewModel(), CoroutineScope by MainCoroutineScope() {


    /** UI Theme */
    val uiThemeFlow = settingsRepository.uiThemeFlow.stateIn(viewModelScope, SharingStarted.Eagerly, UiTheme.DEFAULT)

}
