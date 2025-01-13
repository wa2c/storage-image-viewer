package com.wa2c.android.storageimageviewer.presentation.ui.tree

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.wa2c.android.storageimageviewer.common.exception.AppException
import com.wa2c.android.storageimageviewer.common.utils.Utils
import com.wa2c.android.storageimageviewer.domain.model.FileModel
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.repository.StorageRepository
import com.wa2c.android.storageimageviewer.presentation.ui.common.MainCoroutineScope
import com.wa2c.android.storageimageviewer.presentation.ui.common.ScreenParam
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TreeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val storageRepository: StorageRepository,
): ViewModel(), CoroutineScope by MainCoroutineScope() {
    private val paramId: String? = savedStateHandle[ScreenParam.ScreenParamId]

    /** Init storage */
    private var initStorage: StorageModel = StorageModel.Empty

    /** Current Storage */
    private val _currentStorage = MutableStateFlow(initStorage)
    val currentStorage = _currentStorage.asStateFlow()

    private val _currentList = MutableStateFlow<List<FileModel>>(emptyList())
    val currentList = _currentList.asStateFlow()

    private val _busyState = MutableStateFlow(false)
    val busyState = _busyState.asStateFlow()

    private val _resultState = MutableStateFlow(Result.success(Unit))
    val resultState = _resultState.asStateFlow()

    init {
        launch {
            runCatching {
                val storage = paramId?.let { storageRepository.getStorage(paramId) } ?: throw AppException.StorageNotFoundException(paramId)
                val list = storageRepository.getChildList(storage)
                (storage to list)
            }.onSuccess { (storage, list) ->
                initStorage = storage
                _currentStorage.emit(storage)
                _currentList.emit(list)
                _resultState.emit(Result.success(Unit))
            }.onFailure {
                _resultState.emit(Result.failure(it))
            }
        }
    }




}
