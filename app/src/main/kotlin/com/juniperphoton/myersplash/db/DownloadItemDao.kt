package com.juniperphoton.myersplash.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.juniperphoton.myersplash.model.DownloadItem
import io.reactivex.Flowable

@Dao
abstract class DownloadItemDao {
    @Query("SELECT * FROM download_item ORDER BY create_time DESC")
    abstract fun getAll(): LiveData<List<DownloadItem>>

    @Query("SELECT * FROM download_item WHERE id=:id")
    abstract fun getById(id: String): Flowable<DownloadItem>

    @Query("SELECT COUNT(id) FROM download_item WHERE id=:id")
    abstract fun getCountById(id: String): Int

    @Query("SELECT * FROM download_item WHERE download_url=:url")
    abstract fun getByUrl(url: String): LiveData<DownloadItem>

    @Query("UPDATE download_item SET status=:status WHERE id=:id")
    abstract suspend fun setStatusById(id: String, status: Int)

    @Query("UPDATE download_item SET status=:status WHERE download_url=:url")
    abstract fun setStatusByUrl(url: String, status: Int)

    @Query("UPDATE download_item SET status=${DownloadItem.DOWNLOAD_STATUS_OK}, file_path=:path, progress=1 WHERE download_url=:url")
    abstract fun setSuccess(url: String?, path: String?)

    @Query("UPDATE download_item SET status=${DownloadItem.DOWNLOAD_STATUS_FAILED}, file_path=null, progress=0 WHERE download_url=:url")
    abstract fun setFailed(url: String?)

    @Query("UPDATE download_item SET progress=:progress, status=${DownloadItem.DOWNLOAD_STATUS_DOWNLOADING} WHERE download_url=:url")
    abstract fun setProgress(url: String, progress: Int)

    @Query("DELETE FROM download_item WHERE id=:id")
    abstract suspend fun deleteById(id: String)

    @Insert(onConflict = REPLACE)
    abstract fun insertAll(vararg items: DownloadItem)

    @Delete
    abstract fun delete(item: DownloadItem)

    @Query("DELETE FROM download_item WHERE status=:status")
    abstract suspend fun deleteByStatus(status: Int)

    @Query("UPDATE download_item SET status=${DownloadItem.DOWNLOAD_STATUS_DOWNLOADING}, progress=0 WHERE id=:id")
    abstract suspend fun resetStatus(id: String)

    @Query("UPDATE download_item SET status=${DownloadItem.DOWNLOAD_STATUS_FAILED}, progress=0 WHERE status!=${DownloadItem.DOWNLOAD_STATUS_OK}")
    abstract suspend fun markAllFailed()
}