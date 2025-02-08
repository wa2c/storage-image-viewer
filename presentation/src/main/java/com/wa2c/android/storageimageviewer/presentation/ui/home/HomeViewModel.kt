package com.wa2c.android.storageimageviewer.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wa2c.android.storageimageviewer.common.result.AppException
import com.wa2c.android.storageimageviewer.common.result.AppResult
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.model.UriModel
import com.wa2c.android.storageimageviewer.domain.repository.StorageRepository
import com.wa2c.android.storageimageviewer.presentation.ui.common.MainCoroutineScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val storageRepository: StorageRepository,
): ViewModel(), CoroutineScope by MainCoroutineScope() {

    private val _resultState = MutableSharedFlow<Result<AppResult>>()
    val resultState = _resultState.asSharedFlow()

    val storageList = storageRepository.storageListFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList(),
    )

    private val _editStorage: MutableStateFlow<StorageModel?> = MutableStateFlow(null)
    val editStorage = _editStorage.asStateFlow()

    fun setUri(uri: String?, initialName: String?) {
        launch {
            if (uri.isNullOrEmpty()) {
                // Cancel
                _resultState.emit(Result.failure(AppException.StorageSelectCancelledException()))
            } else {
                val storage = editStorage.value ?: return@launch
                val name = storage.name.ifEmpty { initialName ?: "" }
                updateEditStorage(storage.copy(
                    uri = UriModel(uri),
                    name = name,
                    type = storageRepository.getStorageType(uri),
                ))
            }
        }
    }

    fun newStorage() {
        updateEditStorage(StorageModel.Empty)
    }

    fun updateEditStorage(storage: StorageModel?) {
        _editStorage.value = storage
    }

    /**
     * Save storage
     */
    fun saveStorage(storage: StorageModel) {
        launch {
            storageRepository.saveStorage(storage)
            updateEditStorage(null)
        }
    }

    /**
     * Delete storage
     */
    fun deleteStorage(storage: StorageModel) {
        launch {
            storageRepository.deleteStorage(storage)
            updateEditStorage(null)
        }
    }

    /**
     * Move item.
     */
    fun onItemMove(fromPosition: Int, toPosition: Int) {
        runBlocking {
            storageRepository.moveStorageOrder(fromPosition, toPosition)
        }
    }
}
