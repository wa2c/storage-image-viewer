package com.wa2c.android.storageimageviewer.presentation.ui.tree

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wa2c.android.storageimageviewer.common.result.AppException
import com.wa2c.android.storageimageviewer.common.result.AppResult
import com.wa2c.android.storageimageviewer.common.values.TreeFolderOrder
import com.wa2c.android.storageimageviewer.common.values.TreeSortType
import com.wa2c.android.storageimageviewer.domain.model.FileModel
import com.wa2c.android.storageimageviewer.domain.repository.SettingsRepository
import com.wa2c.android.storageimageviewer.domain.repository.StorageRepository
import com.wa2c.android.storageimageviewer.presentation.ui.common.MainCoroutineScope
import com.wa2c.android.storageimageviewer.presentation.ui.common.ScreenParam
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeScreenItemData
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeScreenOption
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeScreenTreeOption
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeScreenViewerOption
import com.wa2c.android.storageimageviewer.presentation.ui.tree.model.TreeSortModel
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
    private val settingsRepository: SettingsRepository,
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

    private val fileSortFlow = combine(
        settingsRepository.treeSortType.data,
        settingsRepository.treeSortDescending.data,
        settingsRepository.treeSortIgnoreCase.data,
        settingsRepository.treeSortNumber.data,
    ) { type, descending, ignoreCase, number ->
        TreeSortModel(
            type = type,
            isDescending = descending,
            isIgnoreCase = ignoreCase,
            isNumberSort = number,
        )
    }

    private val folderSortFlow = combine(
        settingsRepository.treeFolderSortType.data,
        settingsRepository.treeFolderSortDescending.data,
        settingsRepository.treeFolderSortIgnoreCase.data,
        settingsRepository.treeFolderSortNumber.data,
    ) { type, descending, ignoreCase, number ->
        TreeSortModel(
            type = type,
            isDescending = descending,
            isIgnoreCase = ignoreCase,
            isNumberSort = number,
        )
    }

    private val treeOptionFlow = combine(
        settingsRepository.treeFolderOrder.data,
        settingsRepository.treeViewType.data,
    ) { folderOrder, viewType ->
        TreeScreenTreeOption(
            folderOrder = folderOrder,
            viewType = viewType,
        )
    }

    private val viewerOptionFlow = combine(
        settingsRepository.viewShowPage.data,
        settingsRepository.viewShowAnimation.data,
        settingsRepository.viewVolumeScroll.data,
        settingsRepository.viewShowOverlay.data,
    ) { page, animation, volumeScroll, overlay ->
        TreeScreenViewerOption(
            showPage = page,
            showAnimation = animation,
            volumeScroll = volumeScroll,
            showOverlay = overlay,
        )
    }

    val screenOption = combine(
        fileSortFlow,
        folderSortFlow,
        treeOptionFlow,
        viewerOptionFlow,
        _isViewerMode,
    ) { fileSort, folderSort, treeOption, viewerOption, isViewerMode ->
        TreeScreenOption(
            fileSort = fileSort,
            folderSort = folderSort,
            treeOption = treeOption,
            viewerOption = viewerOption,
            isViewerMode = isViewerMode,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = TreeScreenOption()
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

    fun setOption(option: TreeScreenOption) {
        launch {
            if (option.fileSort != screenOption.value.fileSort || option.folderSort != screenOption.value.folderSort || option.treeOption != screenOption.value.treeOption) {
                currentTree.value.let { tree ->
                    _currentTree.emit(tree.copy(fileList = tree.fileList.sortedWith(FileComparator(option))))
                }
            }

            // File Sort
            settingsRepository.treeSortType.set(option.fileSort.type)
            settingsRepository.treeSortDescending.set(option.fileSort.isDescending)
            settingsRepository.treeSortIgnoreCase.set(option.fileSort.isIgnoreCase)
            settingsRepository.treeSortNumber.set(option.fileSort.isNumberSort)
            // FolderSort
            settingsRepository.treeFolderSortType.set(option.folderSort.type)
            settingsRepository.treeFolderSortDescending.set(option.folderSort.isDescending)
            settingsRepository.treeFolderSortIgnoreCase.set(option.folderSort.isIgnoreCase)
            settingsRepository.treeFolderSortNumber.set(option.folderSort.isNumberSort)
            // Tree
            settingsRepository.treeFolderOrder.set(option.treeOption.folderOrder)
            settingsRepository.treeViewType.set(option.treeOption.viewType)
            // Viewer
            settingsRepository.viewShowPage.set(option.viewerOption.showPage)
            settingsRepository.viewShowAnimation.set(option.viewerOption.showAnimation)
            settingsRepository.viewVolumeScroll.set(option.viewerOption.volumeScroll)
            settingsRepository.viewShowOverlay.set(option.viewerOption.showOverlay)

            _isViewerMode.value = option.isViewerMode
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
        val option = screenOption.first()
        return storageRepository.getChildren(file).sortedWith(FileComparator(option))
    }

    fun closeViewer() {
        launch {
            val option = screenOption.value.copy(
                viewerOption = screenOption.value.viewerOption.copy(showOverlay = true),
                isViewerMode = false,
            )
            setOption(option)
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
    val option: TreeScreenOption,
): Comparator<FileModel> {
    override fun compare(f1: FileModel, f2: FileModel): Int {
        // Sort by folder
        if (option.treeOption.folderOrder != TreeFolderOrder.Mix) {
            val comp = f1.isDirectory.compareTo(f2.isDirectory)
            if (comp != 0) {
                if (option.treeOption.folderOrder == TreeFolderOrder.First) return -comp
                else if (option.treeOption.folderOrder == TreeFolderOrder.Last) return comp
            }
            if (f1.isDirectory && f2.isDirectory) {
                return option.folderSort.compareFile(f1, f2)
            }
        }
        // Sort by File
        return option.fileSort.compareFile(f1, f2)
    }

    private fun TreeSortModel.compareFile(f1: FileModel, f2: FileModel): Int {
        // Sort by size
        if (type == TreeSortType.Size) {
            val comp = f1.size.compareTo(f2.size)
            if (comp != 0) return comp.toDescend(isDescending)
        }

        // Sort by date
        if (type == TreeSortType.Date) {
            val comp = f1.dateModified.compareTo(f2.dateModified)
            if (comp != 0) return comp.toDescend(isDescending)
        }

        // Sort by name
        val s1 = if (isIgnoreCase) normalize(f1.name) else f1.name
        val s2 = if (isIgnoreCase) normalize(f2.name) else f2.name
        return (if (isNumberSort) compareWithNumber(s1, s2) else s1.compareTo(s2))
            .toDescend(isDescending)
    }

    private fun Int.toDescend(isDescend: Boolean): Int {
        return if (isDescend) this * -1 else this
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
