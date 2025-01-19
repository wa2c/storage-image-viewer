package com.wa2c.android.storageimageviewer.presentation.ui.tree

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.wa2c.android.storageimageviewer.common.result.AppException
import com.wa2c.android.storageimageviewer.common.result.AppResult
import com.wa2c.android.storageimageviewer.common.values.SortType
import com.wa2c.android.storageimageviewer.domain.model.FileModel
import com.wa2c.android.storageimageviewer.domain.model.SortModel
import com.wa2c.android.storageimageviewer.domain.repository.StorageRepository
import com.wa2c.android.storageimageviewer.presentation.ui.common.MainCoroutineScope
import com.wa2c.android.storageimageviewer.presentation.ui.common.ScreenParam
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.Normalizer
import javax.inject.Inject

@HiltViewModel
class TreeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val storageRepository: StorageRepository,
): ViewModel(), CoroutineScope by MainCoroutineScope() {
    private val paramId: String? = savedStateHandle[ScreenParam.ScreenParamId]

    private val _currentList = MutableStateFlow<List<FileModel>>(emptyList())
    val currentList = _currentList.asStateFlow()

    private val _currentDir = MutableStateFlow<FileModel?>(null)
    val currentDir = _currentDir.asStateFlow()

    private val _viewerFile = MutableStateFlow<FileModel?>(null)
    val viewerFile = _viewerFile.asStateFlow()

    private val _busyState = MutableStateFlow(false)
    val busyState = _busyState.asStateFlow()

    private val _resultState = MutableStateFlow(Result.success<AppResult>(AppResult.Success))
    val resultState = _resultState.asStateFlow()

    private val _sortState = MutableStateFlow(SortModel())
    val sortState = _sortState.asStateFlow()

    val isRoot: Boolean
        get() = currentDir.value?.isRoot ?: true

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

    fun sortFile(sortModel: SortModel) {
        launch {
            _busyState.emit(true)
            _sortState.emit(sortModel)
            _currentList.emit(currentList.value.sortedWith(FileComparator(sortState.value)))
            _busyState.emit(false)
        }
    }

    fun openFile(file: FileModel) {
        launch {
            if (file.isDirectory) {
                _busyState.emit(true)
                runCatching {
                    val list = getChildren(file)
                    file to list
                }.onSuccess { (file, list) ->
                    _currentDir.emit(file)
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
                val file = currentDir.value ?: return@launch
                val parent = storageRepository.getParent(file) ?: return@launch
                val list = getChildren(parent)
                parent to list
            }.onSuccess { (file, list) ->
                _currentDir.emit(file)
                _currentList.emit(list)
                _resultState.emit(Result.success(AppResult.Success))
            }.onFailure {
                _resultState.emit(Result.failure(it))
            }.also {
                _busyState.emit(false)
            }
        }
    }

    private suspend fun getChildren(file: FileModel): List<FileModel> {
        return storageRepository.getChildren(file).sortedWith(FileComparator(sortState.value))
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

/**
 * File comparator
 */
private class FileComparator(
    val sort: SortModel,
): Comparator<FileModel> {
    override fun compare(f1: FileModel, f2: FileModel): Int {
        return compareFile(f1, f2).let {
            if (sort.isDescending) -it else it
        }
    }

    private fun compareFile(f1: FileModel, f2: FileModel): Int {
        // Sort by folder
        if (!sort.isFolderMixed) {
            val comp = f1.isDirectory.compareTo(f2.isDirectory)
            if (comp != 0) return -comp
        }

        // Sort by size
        if (sort.type == SortType.Size) {
            val comp = f1.size.compareTo(f2.size)
            if (comp != 0) return comp
        }

        // Sort by date
        if (sort.type == SortType.Date) {
            val comp = f1.dateModified.compareTo(f2.dateModified)
            if (comp != 0) return comp
        }

        // Sort by name
        val (s1, s2) = if (sort.isIgnoreCase) {
            normalize(f1.name) to normalize(f2.name)
        } else {
            f1.name to f2.name
        }

        return if (sort.isNumberSort) compareWithNumber(s1, s2)
        else s1.compareTo(s2)
    }

    private fun normalize(s: String) = Normalizer.normalize(s, Normalizer.Form.NFKC).lowercase()

    private fun compareWithNumber(argS1: String?, argS2: String?): Int {
        var s1 = argS1
        var s2 = argS2
        if (s1.isNullOrEmpty() && s2.isNullOrEmpty())
            return 0
        else if (s1.isNullOrEmpty() && !s2.isNullOrEmpty())
            return 1
        else if (!s1.isNullOrEmpty() && s2.isNullOrEmpty())
            return -1

        s1 = Normalizer.normalize(s1, Normalizer.Form.NFKC).lowercase()
        s2 = Normalizer.normalize(s2, Normalizer.Form.NFKC).lowercase()

        var thisMarker = 0
        var thatMarker = 0
        val s1Length = s1.length
        val s2Length = s2.length

        while (thisMarker < s1Length && thatMarker < s2Length) {
            val thisChunk = getChunk(s1, s1Length, thisMarker)
            thisMarker += thisChunk.length

            val thatChunk = getChunk(s2, s2Length, thatMarker)
            thatMarker += thatChunk.length

            // If both chunks contain numeric characters, sortTextView them numerically
            var result: Int
            if (thisChunk[0].isDigit() && thatChunk[0].isDigit()) {
                // Simple chunk comparison by length.
                val thisChunkLength = thisChunk.length
                result = thisChunkLength - thatChunk.length
                // If equal, the first different number counts
                if (result == 0) {
                    for (i in 0 until thisChunkLength) {
                        result = thisChunk[i] - thatChunk[i]
                        if (result != 0) {
                            return result
                        }
                    }
                }
            } else {
                result = thisChunk.compareTo(thatChunk)
            }

            if (result != 0)
                return result
        }

        return s1Length - s2Length
    }

    /** Length of string is passed in for improved efficiency (only need to calculate it once)  */
    private fun getChunk(s: String, slength: Int, argMarker: Int): String {
        var marker = argMarker
        val chunk = StringBuilder()
        var c = s[marker]
        chunk.append(c)
        marker++
        if (c.isDigit()) {
            while (marker < slength) {
                c = s[marker]
                if (!c.isDigit())
                    break
                chunk.append(c)
                marker++
            }
        } else {
            while (marker < slength) {
                c = s[marker]
                if (c.isDigit())
                    break
                chunk.append(c)
                marker++
            }
        }
        return chunk.toString()
    }

}
