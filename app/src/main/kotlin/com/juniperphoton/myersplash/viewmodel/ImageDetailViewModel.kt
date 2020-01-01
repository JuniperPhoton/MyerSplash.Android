package com.juniperphoton.myersplash.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.model.DownloadItem
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.repo.DetailImageRepo
import com.juniperphoton.myersplash.utils.AnalysisHelper
import com.juniperphoton.myersplash.utils.DownloadUtils
import io.reactivex.Flowable
import kotlinx.coroutines.runBlocking
import java.io.File
import javax.inject.Inject

class ImageDetailViewModel(app: Application) : BaseViewModel(app) {
    @Inject
    lateinit var repo: DetailImageRepo

    var unsplashImage: UnsplashImage? = null

    private var downloadItem: DownloadItem? = null

    var associatedDownloadItem: Flowable<DownloadItem>? = null
        get() {
            if (field == null) {
                field = repo.retrieveAssociatedItem(unsplashImage?.id ?: "")
            }
            return field?.doOnNext {
                downloadItem = it
            }
        }

    private val _navigateToAuthorPage = MutableLiveData<String>()
    val navigateToAuthorPage: LiveData<String>
        get() = _navigateToAuthorPage

    private val _share = MutableLiveData<UnsplashImage>()
    val share: LiveData<UnsplashImage>
        get() = _share

    private val _launchEdit = MutableLiveData<Uri>()
    val launchEdit: LiveData<Uri>
        get() = _launchEdit

    fun navigateToAuthorPage() {
        unsplashImage?.userHomePage?.let {
            _navigateToAuthorPage.value = it
        }
    }

    fun copyUrlToClipboard() {
        val clipboard = app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(app.getString(R.string.app_name), unsplashImage?.downloadUrl)
        clipboard.setPrimaryClip(clip)
    }

    fun share() {
        unsplashImage?.let {
            _share.value = it
        }
    }

    fun download() {
        AnalysisHelper.logClickDownloadInDetails()
        unsplashImage?.let {
            DownloadUtils.download(app, it)
        }
    }

    fun cancelDownload(): Boolean = runBlocking {
        AnalysisHelper.logClickCancelDownloadInDetails()
        val image = unsplashImage ?: return@runBlocking false

        repo.setStatusById(image.id!!, DownloadItem.DOWNLOAD_STATUS_FAILED)

        DownloadUtils.cancelDownload(app, image)
        return@runBlocking true
    }

    fun setAs() {
        AnalysisHelper.logClickSetAsInDetails()
        val url = "${downloadItem?.filePath}"
        _launchEdit.value = Uri.fromFile(File(url))
    }

    fun onHide() {
        associatedDownloadItem = null
        unsplashImage = null
    }
}
