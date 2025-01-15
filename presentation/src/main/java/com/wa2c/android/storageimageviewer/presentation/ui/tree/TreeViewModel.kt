package com.wa2c.android.storageimageviewer.presentation.ui.tree

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.wa2c.android.storageimageviewer.common.result.AppException
import com.wa2c.android.storageimageviewer.common.result.AppResult
import com.wa2c.android.storageimageviewer.domain.model.FileModel
import com.wa2c.android.storageimageviewer.domain.repository.StorageRepository
import com.wa2c.android.storageimageviewer.presentation.ui.common.MainCoroutineScope
import com.wa2c.android.storageimageviewer.presentation.ui.common.ScreenParam
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
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

    private val _currentList = MutableStateFlow<List<FileModel>>(emptyList())
    val currentList = _currentList.asStateFlow()

    private val _currentFile = MutableStateFlow<FileModel?>(null)
    val currentFile = _currentFile.asStateFlow()

    private val _viewerFile = MutableStateFlow<FileModel?>(null)
    val viewerFile = _viewerFile.asStateFlow()

    private val _busyState = MutableStateFlow(false)
    val busyState = _busyState.asStateFlow()

    private val _resultState = MutableStateFlow(Result.success<AppResult>(AppResult.Success))
    val resultState = _resultState.asStateFlow()

    val isRoot: Boolean
        get() = currentFile.value?.isRoot ?: true

    init {
        launch {
            _busyState.emit(true)
            runCatching {
                paramId?.let { storageRepository.getStorageFile(paramId) } ?: throw AppException.StorageNotFoundException(paramId)
            }.onSuccess { file ->
                openFile(file)
            }.onFailure {
                _resultState.emit(Result.failure(it))
            }.also {
                _busyState.emit(false)
            }
        }
    }

    fun openFile(file: FileModel) {
        launch {
            if (file.isDirectory) {
                _busyState.emit(true)
                runCatching {
                    val list = storageRepository.getChildList(file)
                    file to list
                }.onSuccess { (file, list) ->
                    _currentFile.emit(file)
                    _currentList.emit(list)
                    _resultState.emit(Result.success(AppResult.Success))
                }.onFailure {
                    _resultState.emit(Result.failure(it))
                }.also {
                    _busyState.emit(false)
                }
            } else {
                _viewerFile.emit(file)
            }
        }
    }

    fun openParent() {
        launch {
            _busyState.emit(true)
            runCatching {
                val file = currentFile.value ?: return@launch
                val parent = storageRepository.getParent(file) ?: return@launch
                val list = storageRepository.getChildList(parent)
                parent to list
            }.onSuccess { (file, list) ->
                _currentFile.emit(file)
                _currentList.emit(list)
                _resultState.emit(Result.success(AppResult.Success))
            }.onFailure {
                _resultState.emit(Result.failure(it))
            }.also {
                _busyState.emit(false)
            }
        }
    }

    fun closeViewer() {
        launch {
            _viewerFile.emit(null)
        }
    }

    fun cancelLoading() {
        coroutineContext.cancelChildren()
    }

}
