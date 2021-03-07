package com.juniperphoton.myersplash.model

import androidx.annotation.IntDef
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "download_item")
open class DownloadItem() {
    companion object {
        const val DOWNLOAD_STATUS_INVALID = -1
        const val DOWNLOAD_STATUS_DOWNLOADING = 0
        const val DOWNLOAD_STATUS_FAILED = 1
        const val DOWNLOAD_STATUS_OK = 2

        const val DISPLAY_STATUS_NOT_SPECIFIED = -1
    }

    @IntDef(DOWNLOAD_STATUS_DOWNLOADING, DOWNLOAD_STATUS_OK, DOWNLOAD_STATUS_FAILED)
    @Retention(AnnotationRetention.SOURCE)
    annotation class DownloadStatus

    @ColumnInfo(name = "thumb_url")
    var thumbUrl: String? = null

    @ColumnInfo(name = "download_url")
    var downloadUrl: String? = null

    @PrimaryKey
    @ColumnInfo(name = "id")
    @field:NonNull
    lateinit var id: String

    @ColumnInfo(name = "progress")
    var progress: Int = 0
        set(value) {
            field = value
            if (this.progress >= 100) {
                status = DOWNLOAD_STATUS_OK
            }
        }

    @ColumnInfo(name = "create_time")
    var createTime: Long = System.currentTimeMillis()

    @ColumnInfo(name = "color")
    var color: Int = 0

    @DownloadStatus
    @ColumnInfo(name = "status")
    var status: Int = 0

    @ColumnInfo(name = "file_path")
    var filePath: String? = null

    @ColumnInfo(name = "file_name")
    var fileName: String? = null

    @ColumnInfo(name = "width")
    var width = 0

    @ColumnInfo(name = "height")
    var height = 0

    @Ignore
    open var lastStatus = DISPLAY_STATUS_NOT_SPECIFIED

    constructor(id: String, thumbUrl: String, downloadUrl: String, fileName: String) : this() {
        this.id = id
        this.thumbUrl = thumbUrl
        this.downloadUrl = downloadUrl
        this.status = DOWNLOAD_STATUS_DOWNLOADING
        this.fileName = fileName.replace(" ", "")
    }

    open fun syncStatus() {
        lastStatus = status
    }

    override fun toString(): String {
        return "DownloadItem(thumbUrl=$thumbUrl, id='$id', progress=$progress, status=$status, filePath=$filePath)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DownloadItem) return false

        if (thumbUrl != other.thumbUrl) return false
        if (downloadUrl != other.downloadUrl) return false
        if (id != other.id) return false
        if (progress != other.progress) return false
        if (createTime != other.createTime) return false
        if (color != other.color) return false
        if (status != other.status) return false
        if (filePath != other.filePath) return false
        if (fileName != other.fileName) return false
        if (lastStatus != other.lastStatus) return false

        return true
    }

    override fun hashCode(): Int {
        var result = thumbUrl?.hashCode() ?: 0
        result = 31 * result + (downloadUrl?.hashCode() ?: 0)
        result = 31 * result + id.hashCode()
        result = 31 * result + progress
        result = 31 * result + createTime.hashCode()
        result = 31 * result + color
        result = 31 * result + status
        result = 31 * result + (filePath?.hashCode() ?: 0)
        result = 31 * result + (fileName?.hashCode() ?: 0)
        result = 31 * result + lastStatus
        return result
    }
}