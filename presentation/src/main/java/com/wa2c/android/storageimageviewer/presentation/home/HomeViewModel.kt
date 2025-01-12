package com.wa2c.android.storageimageviewer.presentation.home

import androidx.lifecycle.ViewModel
import com.wa2c.android.storageimageviewer.domain.model.StorageModel
import com.wa2c.android.storageimageviewer.domain.repository.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    storageRepository: StorageRepository,
): ViewModel() {

    val storageList = MutableStateFlow(
        listOf<StorageModel>()
    )

    /**
     * Move item.
     */
    fun onItemMove(fromPosition: Int, toPosition: Int) {
        runBlocking {
            // run blocking for drag animation

        }
    }

}
