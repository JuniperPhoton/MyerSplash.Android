package com.juniperphoton.myersplash.repo

import com.juniperphoton.myersplash.db.DownloadItemDao
import com.juniperphoton.myersplash.model.DownloadItem
import io.reactivex.Flowable
import javax.inject.Inject

class DetailImageRepo @Inject constructor(private val dao: DownloadItemDao) {
    fun retrieveAssociatedItem(id: String): Flowable<DownloadItem> {
        return dao.getById(id)
    }

    suspend fun setStatusById(id: String, status: Int) {
        dao.setStatusById(id, status)
    }
}