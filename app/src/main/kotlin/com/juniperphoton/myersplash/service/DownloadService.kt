package com.juniperphoton.myersplash.service

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.RealmCache
import com.juniperphoton.myersplash.cloudservice.CloudService
import com.juniperphoton.myersplash.extension.notifyFileUpdated
import com.juniperphoton.myersplash.model.DownloadItem
import com.juniperphoton.myersplash.utils.*
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.ListCompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class DownloadService : Service() {
    private class LocalBinder : Binder()

    override fun onBind(intent: Intent?): IBinder = binder

    companion object {
        private const val TAG = "DownloadService"
        private const val REPORT_FINISHED_TIMEOUT_MS = 500L
    }

    private var binder: LocalBinder = LocalBinder()
    private var disposablesForTimeout = ListCompositeDisposable()

    // A map storing download url to downloading disposable object
    private val downloadUrlToDisposableMap = HashMap<String, Disposable>()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Pasteur.info(TAG, "on start command")
        onHandleIntent(intent)
        return START_NOT_STICKY
    }

    private fun onHandleIntent(intent: Intent?) {
        intent ?: return
        val canceled = intent.getBooleanExtra(Params.CANCELED_KEY, false)
        val downloadUrl = intent.getStringExtra(Params.URL_KEY)
        val fileName = intent.getStringExtra(Params.NAME_KEY)
        val previewUrl = intent.getStringExtra(Params.PREVIEW_URI)
        val isUnsplash = intent.getBooleanExtra(Params.IS_UNSPLASH_WALLPAPER, true)
        if (!isUnsplash) {
            ToastService.sendShortToast("Downloading...")
        }

        val previewUri: Uri? = if (previewUrl.isNullOrEmpty()) null else {
            Uri.parse(previewUrl)
        }

        if (canceled) {
            Pasteur.d(TAG, "on handle intent cancelled")
            val subscriber = downloadUrlToDisposableMap[downloadUrl]
            if (subscriber != null) {
                subscriber.dispose()
                NotificationUtil.cancelNotification(Uri.parse(downloadUrl))
                ToastService.sendShortToast(getString(R.string.cancelled_download))
            }
        } else {
            Pasteur.d(TAG, "on handle intent progress")
            downloadImage(downloadUrl, fileName, previewUri, isUnsplash)
        }
    }

    private fun downloadImage(url: String, fileName: String,
                              previewUri: Uri?, isUnsplash: Boolean): String {
        val file = DownloadUtil.getFileToSave(fileName)
        val observer = object : DisposableObserver<ResponseBody>() {
            var outputFile: File? = null

            override fun onComplete() {
                if (outputFile == null) {
                    NotificationUtil.showErrorNotification(Uri.parse(url), fileName,
                            url, previewUri)
                    RealmCache.getInstance().executeTransaction { realm ->
                        val downloadItem = realm.where(DownloadItem::class.java)
                                .equalTo(DownloadItem.DOWNLOAD_URL, url).findFirst()
                        if (downloadItem != null) {
                            downloadItem.status = DownloadItem.DOWNLOAD_STATUS_FAILED
                        }
                    }
                } else {
                    Pasteur.d(TAG, "output file:" + outputFile!!.absolutePath)

                    val newFile = File("${outputFile!!.path.replace(" ", "")}.jpg")
                    outputFile!!.renameTo(newFile)

                    Pasteur.d(TAG, "renamed file:" + newFile.absolutePath)
                    newFile.notifyFileUpdated(App.instance)

                    val realm = RealmCache.getInstance()

                    val downloadItem = realm.where(DownloadItem::class.java)
                            .equalTo(DownloadItem.DOWNLOAD_URL, url).findFirst()
                    if (downloadItem != null) {
                        realm.beginTransaction()
                        downloadItem.status = DownloadItem.DOWNLOAD_STATUS_OK
                        downloadItem.filePath = newFile.path
                        realm.commitTransaction()
                    }
                    scheduleToReportFinished(url, previewUri, isUnsplash, newFile)
                }
                Pasteur.d(TAG, getString(R.string.completed))
            }

            override fun onError(e: Throwable) {
                e.printStackTrace()
                Pasteur.d(TAG, "on handle intent error " + e.message + ",url:" + url)
                NotificationUtil.showErrorNotification(Uri.parse(url), fileName, url, null)

                val realm = RealmCache.getInstance()
                realm.beginTransaction()

                val downloadItem = realm.where(DownloadItem::class.java)
                        .equalTo(DownloadItem.DOWNLOAD_URL, url).findFirst()
                if (downloadItem != null) {
                    downloadItem.status = DownloadItem.DOWNLOAD_STATUS_FAILED
                }

                realm.commitTransaction()
            }

            override fun onNext(responseBody: ResponseBody) {
                Pasteur.d(TAG, "outputFile download onNext,size" + responseBody.contentLength())
                this.outputFile = DownloadUtil.writeToFile(responseBody, file!!.path) {
                    RealmCache.getInstance().executeTransaction { realm ->
                        val downloadItem = realm.where(DownloadItem::class.java)
                                .equalTo(DownloadItem.DOWNLOAD_URL, url).findFirst()
                        if (downloadItem != null) {
                            downloadItem.progress = it
                        }
                    }
                }
            }
        }

        val disposable = CloudService.downloadPhoto(url).subscribeWith(observer)
        downloadUrlToDisposableMap[url] = disposable

        return file!!.path
    }

    /**
     * It seams that notifying notifications to frequently will ignore the latest one.
     * We schedule a timer to report the finished event.
     */
    private fun scheduleToReportFinished(url: String, previewUri: Uri?,
                                         isUnsplash: Boolean, newFile: File) {
        Observable.timer(REPORT_FINISHED_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Long> {
                    override fun onSubscribe(d: Disposable) {
                        disposablesForTimeout.add(d)
                    }

                    override fun onNext(t: Long) {
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                    }

                    override fun onComplete() {
                        NotificationUtil.showCompleteNotification(Uri.parse(url), previewUri,
                                if (isUnsplash) null else newFile.absolutePath)
                    }
                })
    }

    override fun onDestroy() {
        disposablesForTimeout.dispose()
        super.onDestroy()
    }
}
