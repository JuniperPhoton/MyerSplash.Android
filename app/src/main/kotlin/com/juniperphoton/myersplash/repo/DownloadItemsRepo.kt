package com.juniperphoton.myersplash.repo

import androidx.lifecycle.LiveData
import com.juniperphoton.myersplash.db.DownloadItemDao
import com.juniperphoton.myersplash.model.DownloadItem
import javax.inject.Inject

class DownloadItemsRepo @Inject constructor(
        private val dao: DownloadItemDao
) {
    val downloadItems: LiveData<List<DownloadItem>>
        get() = dao.getAll()

    suspend fun deleteByStatus(status: Int) {
        dao.deleteByStatus(status)
    }

    suspend fun updateStatus(id: String, status: Int) {
        dao.setStatusById(id, status)
    }

    suspend fun resetStatus(id: String) {
        dao.resetStatus(id)
    }

    suspend fun deleteById(id: String) {
        dao.deleteById(id)
    }
}