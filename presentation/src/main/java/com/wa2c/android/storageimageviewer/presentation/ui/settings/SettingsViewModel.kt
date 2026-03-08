package com.wa2c.android.storageimageviewer.presentation.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wa2c.android.storageimageviewer.domain.model.AppMessage
import com.wa2c.android.storageimageviewer.domain.repository.SettingsRepository
import com.wa2c.android.storageimageviewer.domain.repository.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val storageRepository: StorageRepository,
    private val settingsRepository: SettingsRepository,
): ViewModel() {


    fun sendMessage(result: AppMessage) = viewModelScope.launch {
        storageRepository.sendMessage(result)
    }

    /** UI Theme */
    val uiTheme = settingsRepository.uiTheme

}
