package com.juniperphoton.myersplash.viewmodel

import android.app.Application
import android.graphics.RectF
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.juniperphoton.myersplash.model.UnsplashImage

class ImageSharedViewModel(application: Application) : BaseViewModel(application) {
    val onClickedImage = MutableLiveData<ClickData>()
    val onRequestScrollToTop = MutableLiveData<Int>()
    val onRequestRefresh = MutableLiveData<Int>()
}

data class ClickData(val rectF: RectF, val unsplashImage: UnsplashImage, val itemView: View)