package com.wa2c.android.storageimageviewer.presentation.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.wa2c.android.storageimageviewer.common.utils.Utils
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
class EditViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val storageRepository: StorageRepository,
): ViewModel(), CoroutineScope by MainCoroutineScope() {
    private val paramId: String? = savedStateHandle[ScreenParam.EditScreenParamId]

    /** Init storage */
    private var initStorage: StorageModel = StorageModel.Empty

    /** Current Storage */
    val currentStorage = MutableStateFlow<StorageModel>(initStorage)

    private val _busyState = MutableStateFlow(false)
    val busyState = _busyState.asStateFlow()

    /** True if adding new settings */
    val isNew: Boolean
        get() = paramId.isNullOrEmpty()

    /** True if data changed */
    val isChanged: Boolean
        get() = isNew || initStorage != currentStorage.value

    init {
        launch {
            val storage = paramId?.let {
                storageRepository.getStorage(paramId)?.also { initStorage = it }
            } ?: StorageModel.Empty.copy(id = Utils.generateUUID())
            currentStorage.emit(storage)
        }
    }




}
