package com.wa2c.android.storageimageviewer.presentation.ui.tree

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wa2c.android.storageimageviewer.common.result.AppException
import com.wa2c.android.storageimageviewer.common.result.AppResult
import com.wa2c.android.storageimageviewer.common.values.TreeSortType
import com.wa2c.android.storageimageviewer.domain.model.FileModel
import com.wa2c.android.storageimageviewer.domain.model.TreeSortModel
import com.wa2c.android.storageimageviewer.domain.repository.StorageRepository
import com.wa2c.android.storageimageviewer.presentation.ui.common.MainCoroutineScope
import com.wa2c.android.storageimageviewer.presentation.ui.common.ScreenParam
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeScreenDisplayData
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeScreenItemData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.Normalizer
import javax.inject.Inject

@HiltViewModel
class TreeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val storageRepository: StorageRepository,
): ViewModel(), CoroutineScope by MainCoroutineScope() {
    private val paramId: String? = savedStateHandle[ScreenParam.ScreenParamId]

    // Tree
    private val _currentTree = MutableStateFlow(TreeScreenItemData())
    val currentTree = _currentTree.asStateFlow()

    private val _focusedFile = MutableStateFlow<FileModel?>(null)
    val focusedFile = _focusedFile.asStateFlow()

    private val _busyState = MutableStateFlow(false)
    val busyState = _busyState.asStateFlow()

    private val _resultState = MutableStateFlow(Result.success<AppResult>(AppResult.Success))
    val resultState = _resultState.asStateFlow()

    val isRoot: Boolean
        get() = currentTree.value.isRoot

    private val _isViewerMode = MutableStateFlow(false)

    val displayData = combine(
        storageRepository.sortFlow,
        storageRepository.treeViewTypeFlow,
        storageRepository.viewShowOverlayFlow,
        storageRepository.viewShowPageFlow,
        _isViewerMode
    ) { sort, type, overlay, page, isViewerMode ->
        TreeScreenDisplayData(sort, type, overlay, page, isViewerMode)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = TreeScreenDisplayData()
    )

    init {
        launch {
            _busyState.emit(true)
            runCatching {
                paramId?.let { storageRepository.getStorageFile(paramId) } ?: throw AppException.StorageNotFoundException(paramId)
            }.onSuccess { file ->
                openFile(file)
            }.onFailure {
                _resultState.emit(Result.failure(it))
            }
        }
    }

    fun setDisplay(display: TreeScreenDisplayData) {
        launch {
            if (display.sort != displayData.value.sort) {
                currentTree.value.let { tree ->
                    _currentTree.emit(tree.copy(fileList = tree.fileList.sortedWith(FileComparator(display.sort))))
                }
            }
            storageRepository.setSort(display.sort)
            storageRepository.setTreeViewType(display.viewType)
            storageRepository.setViewShowPageFlow(display.showPage)
            storageRepository.setViewShowOverlayFlow(display.showOverlay)
            _isViewerMode.value = display.isViewerMode
        }

    }

    fun focusFile(
        file: FileModel?,
    ) {
        _focusedFile.value = file
    }

    fun openPage(
        inputNumber: String
    ) {
        val imageFileList = currentTree.value.imageFileList.ifEmpty { return }
        inputNumber.toIntOrNull()?.let {
            imageFileList.getOrNull((it - 1).coerceIn(imageFileList.indices)) ?.let { file ->
                openFile(file)
            }
        }
    }

    fun openFile(
        file: FileModel,
    ) {
        launch {
            if (file.isDirectory) {
                _busyState.emit(true)
                runCatching {
                    val list = getChildren(file)
                    file to list
                }.onSuccess { (file, list) ->
                    focusFile(null)
                    val route = currentTree.value.routeList + file
                    _currentTree.emit(TreeScreenItemData(route, list))
                    _resultState.emit(Result.success(AppResult.Success))
                }.onFailure {
                    _resultState.emit(Result.failure(it))
                }.also {
                    _busyState.emit(false)
                }
            } else {
                focusFile(file)
                _isViewerMode.emit(true)
            }
        }
    }

    fun openParent() {
        if (currentTree.value.isRoot) return
        launch {
            _busyState.emit(true)
            runCatching {
                val route = currentTree.value.routeList.dropLast(1)
                val list = getChildren(route.last())
                route to list
            }.onSuccess { (route, list) ->
                focusFile(currentTree.value.currentFolder)
                _currentTree.emit(TreeScreenItemData(route, list))
                _resultState.emit(Result.success(AppResult.Success))
            }.onFailure {
                _resultState.emit(Result.failure(it))
            }.also {
                _busyState.emit(false)
            }
        }
    }

    private suspend fun getChildren(file: FileModel): List<FileModel> {
        val sort = displayData.first().sort
        return storageRepository.getChildren(file).sortedWith(FileComparator(sort))
    }

    fun closeViewer() {
        launch {
            val display = displayData.value.copy(
                showOverlay = true,
                isViewerMode = false,
            )
            setDisplay(display)
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
    val sort: TreeSortModel,
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
        if (sort.type == TreeSortType.Size) {
            val comp = f1.size.compareTo(f2.size)
            if (comp != 0) return comp
        }

        // Sort by date
        if (sort.type == TreeSortType.Date) {
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
        val (s1, s2) = let {
            if (argS1.isNullOrEmpty() && argS2.isNullOrEmpty())
                return 0
            else if (argS1.isNullOrEmpty())
                return 1
            else if (argS2.isNullOrEmpty())
                return -1
            argS1 to argS2
        }

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
