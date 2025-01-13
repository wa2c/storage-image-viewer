package com.wa2c.android.storageimageviewer.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.repository.StorageRepository
import com.wa2c.android.storageimageviewer.presentation.ui.common.MainCoroutineScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val storageRepository: StorageRepository,
): ViewModel(), CoroutineScope by MainCoroutineScope() {

    val storageList = storageRepository.storageListFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList(),
    )

    /**
     * Move item.
     */
    fun onItemMove(fromPosition: Int, toPosition: Int) {
        runBlocking {
            storageRepository.moveConnection(fromPosition, toPosition)
        }
    }

    /**
     * Edit storage
     */
    fun setStorage(storage: StorageModel) {
        launch {
            storageRepository.setStorage(storage)
        }
    }

}
