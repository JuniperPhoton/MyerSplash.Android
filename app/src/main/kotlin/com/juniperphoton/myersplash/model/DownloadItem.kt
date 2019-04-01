package com.juniperphoton.myersplash.model

import androidx.annotation.IntDef
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey

open class DownloadItem() : RealmObject() {
    companion object {
        const val DOWNLOAD_STATUS_INVALID = -1
        const val DOWNLOAD_STATUS_DOWNLOADING = 0
        const val DOWNLOAD_STATUS_FAILED = 1
        const val DOWNLOAD_STATUS_OK = 2

        const val DISPLAY_STATUS_NOT_SPECIFIED = -1

        const val ID_KEY = "id"
        const val DOWNLOAD_URL = "downloadUrl"
        const val STATUS_KEY = "status"
        const val POSITION_KEY = "position"
    }

    @IntDef(DOWNLOAD_STATUS_DOWNLOADING, DOWNLOAD_STATUS_OK, DOWNLOAD_STATUS_FAILED)
    @Retention(AnnotationRetention.SOURCE)
    annotation class DownloadStatus

    var thumbUrl: String? = null

    var downloadUrl: String? = null

    @PrimaryKey
    open var id: String? = null

    open var progress: Int = 0
        set(value) {
            field = value
            if (this.progress >= 100) {
                status = DOWNLOAD_STATUS_OK
            }
        }

    open var color: Int = 0
    open var status: Int = 0
    open var filePath: String? = null
    open var fileName: String? = null
    open var position: Int = 0

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
}