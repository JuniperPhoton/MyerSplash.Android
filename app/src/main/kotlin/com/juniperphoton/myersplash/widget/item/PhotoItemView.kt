package com.juniperphoton.myersplash.widget.item

import android.content.Context
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.extension.getDarker
import com.juniperphoton.myersplash.extension.toHexString
import com.juniperphoton.myersplash.extension.updateVisibility
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.utils.LocalSettingHelper
import com.juniperphoton.myersplash.utils.extractThemeColor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*

typealias OnClickPhotoListener = ((rectF: RectF, unsplashImage: UnsplashImage, itemView: View) -> Unit)
typealias OnClickQuickDownloadListener = ((image: UnsplashImage) -> Unit)
typealias OnBindListener = ((View, Int) -> Unit)

class PhotoItemView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs), View.OnClickListener {
    @BindView(R.id.row_photo_iv)
    lateinit var simpleDraweeView: SimpleDraweeView

    @BindView(R.id.row_photo_root)
    lateinit var rootView: ViewGroup

    @BindView(R.id.row_photo_download_rl)
    lateinit var downloadRL: ViewGroup

    @BindView(R.id.row_photo_ripple_mask_rl)
    lateinit var rippleMaskRL: ViewGroup

    @BindView(R.id.row_photo_today_tag)
    lateinit var todayTag: View

    var onClickPhoto: OnClickPhotoListener? = null
    var onClickQuickDownload: OnClickQuickDownloadListener? = null
    var onBind: OnBindListener? = null

    private var unsplashImage: UnsplashImage? = null
    private var job: Job? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        ButterKnife.bind(this, this)
        rippleMaskRL.setOnClickListener(this)
    }

    @OnClick(R.id.row_photo_download_rl)
    fun onClickQuickDownload() {
        unsplashImage?.let {
            onClickQuickDownload?.invoke(it)
        }
    }

    fun bind(image: UnsplashImage?, pos: Int) {
        if (image == null) return

        job?.cancel()

        unsplashImage = image

        if (!image.isUnsplash) {
            tryUpdateThemeColor()
        }

        val showDownloadButton = LocalSettingHelper.getBoolean(context,
                context.getString(R.string.preference_key_quick_download), true)
        downloadRL.visibility = if (showDownloadButton) View.VISIBLE else View.GONE

        todayTag.updateVisibility(image.showTodayTag)
        rootView.background = ColorDrawable(image.themeColor.getDarker(0.7f))
        simpleDraweeView.setImageURI(image.listUrl)

        onBind?.invoke(rootView, pos)
    }

    override fun onClick(v: View?) {
        val url = unsplashImage?.listUrl ?: return

        if (!Fresco.getImagePipeline().isInBitmapMemoryCache(Uri.parse(url))) {
            return
        }

        val location = IntArray(2)
        simpleDraweeView.getLocationOnScreen(location)
        onClickPhoto?.invoke(RectF(
                location[0].toFloat(),
                location[1].toFloat(),
                simpleDraweeView.width.toFloat(),
                simpleDraweeView.height.toFloat()), unsplashImage!!, rootView)
    }

    private fun tryUpdateThemeColor() {
        job = GlobalScope.launch(Dispatchers.Main) {
            try {
                val colorJob = async {
                    unsplashImage?.extractThemeColor()
                }
                val color = colorJob.await() ?: return@launch
                if (color != Int.MIN_VALUE) {
                    unsplashImage?.color = color.toHexString()
                }
            } catch (e: CancellationException) {
                // ignore cancellation
            }
        }
    }
}