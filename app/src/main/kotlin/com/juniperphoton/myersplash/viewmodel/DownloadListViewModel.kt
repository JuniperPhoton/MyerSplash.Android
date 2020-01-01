package com.juniperphoton.myersplash.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import com.juniperphoton.myersplash.model.DownloadItem
import com.juniperphoton.myersplash.repo.DownloadItemsRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

class DownloadListViewModel(application: Application
) : BaseViewModel(application) {
    @Inject
    lateinit var repository: DownloadItemsRepo

    val downloadItems: LiveData<List<DownloadItem>>
        get() = repository.downloadItems

    fun deleteByStatus(status: Int) = launch(Dispatchers.IO) {
        repository.deleteByStatus(status)
    }

    fun updateItemStatus(id: String, status: Int) = launch(Dispatchers.IO) {
        repository.updateStatus(id, status)
    }

    fun resetItemStatus(id: String) = launch(Dispatchers.IO) {
        repository.resetStatus(id)
    }

    fun deleteItem(id: String) = launch(Dispatchers.IO) {
        repository.deleteById(id)
    }

    override fun onCleared() {
        cancel()
        super.onCleared()
    }
}