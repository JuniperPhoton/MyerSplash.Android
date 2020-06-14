package com.juniperphoton.myersplash.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Observer
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.juniperphoton.flipperlayout.FlipperLayout
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.activity.EditActivity
import com.juniperphoton.myersplash.extension.*
import com.juniperphoton.myersplash.misc.Action
import com.juniperphoton.myersplash.misc.guard
import com.juniperphoton.myersplash.model.DownloadItem
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.model.getDisplayRatio
import com.juniperphoton.myersplash.model.getDisplayRatioF
import com.juniperphoton.myersplash.utils.*
import com.juniperphoton.myersplash.viewmodel.AppViewModelProviders
import com.juniperphoton.myersplash.viewmodel.ClickData
import com.juniperphoton.myersplash.viewmodel.ImageDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
@Suppress("unused")
class ImageDetailView(context: Context, attrs: AttributeSet
) : FrameLayout(context, attrs) {
    companion object {
        private const val TAG = "ImageDetailView"
        private const val RESULT_CODE = 10000

        private const val DOWNLOAD_FLIPPER_LAYOUT_STATUS_DOWNLOAD = 0
        private const val DOWNLOAD_FLIPPER_LAYOUT_STATUS_DOWNLOADING = 1
        private const val DOWNLOAD_FLIPPER_LAYOUT_STATUS_DOWNLOAD_OK = 2

        private const val RESET_THRESHOLD = 150
        private const val MOVE_THRESHOLD = 10

        private const val ANIMATION_DURATION_FAST_MILLIS = 300L
        private const val ANIMATION_DURATION_SLOW_MILLIS = 400L
        private const val ANIMATION_DURATION_VERY_SLOW_MILLIS = 500L

        private const val URL_COPIED_DELAY_MILLIS = 2000L
    }

    private var listPositionY = 0f

    private var clickedView: View? = null

    private var scope: CoroutineScope? = null

    /**
     * Invoked when the display animation is started.
     */
    var onShowing: Action? = null

    /**
     * Invoke when the view is fully displayed.
     */
    var onShown: Action? = null

    /**
     * Invoked when the view is about to hide.
     */
    var onHiding: Action? = null

    /**
     * Invoked when the view is invisible to user.
     */
    var onHidden: Action? = null

    @BindView(R.id.detail_root_sv)
    lateinit var detailRootScrollView: ViewGroup

    @BindView(R.id.detail_hero_view)
    lateinit var photoView: SimpleDraweeView

    @BindView(R.id.detail_backgrd_rl)
    lateinit var detailInfoRootLayout: ViewGroup

    @BindView(R.id.detail_img_rl)
    lateinit var detailImgRL: ViewGroup

    @BindView(R.id.detail_name_tv)
    lateinit var nameTextView: TextView

    @BindView(R.id.detail_name_line)
    lateinit var lineView: View

    @BindView(R.id.detail_photo_by_tv)
    lateinit var photoByTextView: TextView

    @BindView(R.id.detail_download_fab)
    lateinit var downloadFAB: FloatingActionButton

    @BindView(R.id.detail_cancel_download_fab)
    lateinit var cancelDownloadFAB: FloatingActionButton

    @BindView(R.id.detail_share_fab)
    lateinit var shareFAB: FloatingActionButton

    @BindView(R.id.copy_url_tv)
    lateinit var copyUrlTextView: TextView

    @BindView(R.id.copied_url_tv)
    lateinit var copiedUrlTextView: TextView

    @BindView(R.id.copy_url_fl)
    lateinit var copyLayout: FrameLayout

    @BindView(R.id.copied_url_fl)
    lateinit var copiedLayout: FrameLayout

    @BindView(R.id.copy_url_flipper_layout)
    lateinit var copyUrlFlipperLayout: FlipperLayout

    @BindView(R.id.download_flipper_layout)
    lateinit var downloadFlipperLayout: FlipperLayout

    @BindView(R.id.detail_progress_ring)
    lateinit var progressView: RingProgressView

    @BindView(R.id.detail_set_as_fab)
    lateinit var setAsFAB: FloatingActionButton

    private lateinit var viewModel: ImageDetailViewModel

    private val shareButtonHideOffset: Int
        get() = resources.getDimensionPixelSize(R.dimen.share_btn_margin_right_hide)

    private val downloadFlipperLayoutHideOffset: Int
        get() = resources.getDimensionPixelSize(R.dimen.download_btn_margin_right_hide)

    private var animating: Boolean = false
    private var copied: Boolean = false

    private var downX: Float = 0f
    private var downY: Float = 0f

    private var startX: Float = 0f
    private var startY: Float = 0f

    private var pointerDown: Boolean = false

    @Inject
    lateinit var analysisHelper: AnalysisHelper

    init {
        LayoutInflater.from(context).inflate(R.layout.detail_content, this, true)
        ButterKnife.bind(this, this)

        initDetailViews()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initDetailViews() {
        val activity = context as AppCompatActivity

        viewModel = AppViewModelProviders.of(activity)
                .get(ImageDetailViewModel::class.java)
                .apply {
                    navigateToAuthorPage.observe(activity, Observer { e ->
                        e?.consume {
                            navigateToAuthorPage(it)
                        }
                    })
                    share.observe(activity, Observer { e ->
                        e?.consume {
                            doShare(it)
                        }
                    })
                    launchEdit.observe(activity, Observer { e ->
                        e?.consume {
                            launchEditActivity(it)
                        }
                    })
                }

        detailRootScrollView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                tryHide()
            }
            true
        }

        detailRootScrollView.visibility = View.INVISIBLE

        detailInfoRootLayout.translationY = (-resources.getDimensionPixelSize(R.dimen.img_detail_info_height)).toFloat()
        downloadFlipperLayout.translationX = resources.getDimensionPixelSize(R.dimen.download_btn_margin_right_hide).toFloat()
        shareFAB.translationX = resources.getDimensionPixelSize(R.dimen.share_btn_margin_right_hide).toFloat()

        ValueAnimator.ofFloat(0f, 360f).apply {
            addUpdateListener { animation -> progressView.rotation = animation.animatedValue as Float }
            interpolator = LinearInterpolator()
            duration = 1200
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            start()
        }

        photoView.setOnTouchListener { _, e ->
            if (animating) {
                return@setOnTouchListener false
            }
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = e.rawX
                    downY = e.rawY

                    startX = detailImgRL.translationX
                    startY = detailImgRL.translationY

                    pointerDown = true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!pointerDown) {
                        return@setOnTouchListener false
                    }
                    if (downX == 0f || downY == 0f) {
                        downX = e.rawX
                        downY = e.rawY

                        startX = detailImgRL.translationX
                        startY = detailImgRL.translationY
                    }

                    val dx = e.rawX - downX
                    val dy = e.rawY - downY

                    if (abs(dx) >= MOVE_THRESHOLD || abs(dy) >= MOVE_THRESHOLD) {
                        toggleFadeAnimation(false)
                    }

                    detailImgRL.translationX = startX + dx
                    detailImgRL.translationY = startY + dy
                }
                MotionEvent.ACTION_UP -> {
                    if (!pointerDown) {
                        return@setOnTouchListener false
                    }

                    if (abs(e.rawY - downY) >= RESET_THRESHOLD || abs(e.rawX - downX) >= RESET_THRESHOLD) {
                        tryHide()
                    } else {
                        detailImgRL.animate().translationX(startX).translationY(startY).setDuration(ANIMATION_DURATION_FAST_MILLIS).start()
                        toggleFadeAnimation(true)
                    }

                    pointerDown = false
                }
            }
            true
        }
    }

    private fun toggleFadeAnimation(show: Boolean) {
        if (show) {
            if (detailInfoRootLayout.alpha == 1f) {
                return
            }
        } else if (detailInfoRootLayout.alpha == 0f) {
            return
        }

        ValueAnimator.ofFloat(if (show) 1f else 0f).apply {
            addUpdateListener {
                detailInfoRootLayout.alpha = it.animatedValue as Float
                shareFAB.alpha = it.animatedValue as Float
                downloadFlipperLayout.alpha = it.animatedValue as Float
            }
            duration = ANIMATION_DURATION_FAST_MILLIS
            start()
        }
    }

    private fun resetStatus() {
        shareFAB.alpha = 1f
        detailInfoRootLayout.alpha = 1f
        downloadFlipperLayout.alpha = 1f

        shareFAB.translationX = shareButtonHideOffset.toFloat()
        downloadFlipperLayout.translationX = downloadFlipperLayoutHideOffset.toFloat()
    }

    private fun toggleHeroViewAnimation(startY: Float, endY: Float, show: Boolean) {
        if (!show) {
            downloadFlipperLayout.updateIndex(DOWNLOAD_FLIPPER_LAYOUT_STATUS_DOWNLOAD)
        } else {
            detailImgRL.translationX = 0f
        }

        Pasteur.info(TAG, "toggleHeroViewAnimation: from $startY to $endY, show: $show")

        val startX = detailImgRL.translationX

        ValueAnimator.ofFloat(startY, endY).apply {
            duration = ANIMATION_DURATION_FAST_MILLIS
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener {
                detailImgRL.translationX = startX * (1 - it.animatedFraction)
                detailImgRL.translationY = it.animatedValue as Float
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?, isReverse: Boolean) {
                    super.onAnimationStart(animation, isReverse)
                    animating = true
                }

                override fun onAnimationEnd(a: Animator) {
                    animating = false

                    if (!show && clickedView != null) {
                        clickedView!!.visibility = View.VISIBLE
                        toggleMaskAnimation(false)
                        clickedView = null
                        quickReset()
                    } else {
                        toggleDetailLayoutAnimation(show = true, oneshot = false)
                        toggleDownloadFlipperLayoutAnimation(show = true, oneshot = false)
                        toggleShareBtnAnimation(show = true, oneshot = false)
                    }

                    removeAllUpdateListeners()
                    removeAllListeners()
                }
            })
            start()
        }
    }

    private fun checkDownloadStatus(item: DownloadItem): Boolean {
        val path = item.filePath ?: return false

        val file = File(path)
        return file.exists() && file.canRead()
    }

    private fun getTargetY(ratio: Float): Float {
        val decorView = (context as Activity).window.decorView
        val height = decorView.height
        val width = decorView.width
        val detailHeight = (width / ratio).toInt() +
                context.resources.getDimensionPixelSize(R.dimen.img_detail_info_height)
        return (height - detailHeight) / 2f
    }

    private fun toggleDetailLayoutAnimation(show: Boolean, oneshot: Boolean) {
        Pasteur.info(TAG) {
            "toggleDetailRLAnimation, show: $show, oneshot: $oneshot"
        }

        val startY = if (show) -resources.getDimensionPixelSize(R.dimen.img_detail_info_height) else 0
        val endY = if (show) 0 else -resources.getDimensionPixelSize(R.dimen.img_detail_info_height)

        if (oneshot) {
            animating = false
            detailInfoRootLayout.translationY = endY.toFloat()
            return
        }

        detailInfoRootLayout.translationY = startY.toFloat()

        ValueAnimator().apply {
            setFloatValues(startY.toFloat(), endY.toFloat())
            duration = if (oneshot) 0 else ANIMATION_DURATION_SLOW_MILLIS
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener { animation ->
                detailInfoRootLayout.translationY = animation.animatedValue as Float
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(a: Animator) {
                    animating = true
                }

                override fun onAnimationEnd(a: Animator) {
                    animating = false

                    if (!show) {
                        toggleHeroViewAnimation(detailImgRL.translationY, listPositionY, false)
                    }

                    removeAllUpdateListeners()
                    removeAllListeners()
                }
            })
            start()
        }
    }

    private fun toggleDownloadFlipperLayoutAnimation(show: Boolean, oneshot: Boolean) {
        val hideX = downloadFlipperLayoutHideOffset

        if (oneshot) {
            animating = false
            downloadFlipperLayout.translationX = if (show) 0f else hideX.toFloat()
            return
        }

        val start = if (show) hideX else 0
        val end = if (show) 0 else hideX

        ValueAnimator.ofFloat(start.toFloat(), end.toFloat()).apply {
            duration = if (oneshot) 0 else ANIMATION_DURATION_VERY_SLOW_MILLIS
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation -> downloadFlipperLayout.translationX = animation.animatedValue as Float }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    animating = false
                    removeAllUpdateListeners()
                    removeAllListeners()
                }

                override fun onAnimationStart(animation: Animator?) {
                    super.onAnimationStart(animation)
                    animating = true
                }
            })
            start()
        }
    }

    private fun toggleShareBtnAnimation(show: Boolean, oneshot: Boolean) {
        val hideX = shareButtonHideOffset

        if (oneshot) {
            animating = false
            shareFAB.translationX = if (show) 0f else hideX.toFloat()
            return
        }

        val start = if (show) hideX else 0
        val end = if (show) 0 else hideX

        ValueAnimator.ofFloat(start.toFloat(), end.toFloat()).apply {
            duration = ANIMATION_DURATION_VERY_SLOW_MILLIS
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation -> shareFAB.translationX = animation.animatedValue as Float }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    animating = false
                    removeAllUpdateListeners()
                    removeAllListeners()
                }

                override fun onAnimationStart(animation: Animator?) {
                    super.onAnimationStart(animation)
                    animating = true
                }
            })
            start()
        }
    }

    private fun toggleMaskAnimation(show: Boolean) {
        ValueAnimator.ofArgb(if (show) {
            Color.TRANSPARENT
        } else {
            ContextCompat.getColor(context, R.color.MaskColor)
        }, if (show) {
            ContextCompat.getColor(context, R.color.MaskColor)
        } else {
            Color.TRANSPARENT
        }).apply {
            duration = ANIMATION_DURATION_FAST_MILLIS
            addUpdateListener { animation ->
                detailRootScrollView.background = ColorDrawable(animation.animatedValue as Int)
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(a: Animator) {
                    animating = true

                    if (show) {
                        onShowing?.invoke()
                    } else {
                        onHiding?.invoke()
                    }
                }

                override fun onAnimationEnd(a: Animator) {
                    animating = false

                    if (show) {
                        onShown?.invoke()
                    } else {
                        resetStatus()
                        detailRootScrollView.visibility = View.INVISIBLE
                        onHidden?.invoke()
                    }

                    removeAllUpdateListeners()
                    removeAllListeners()
                }
            })
            start()
        }
    }

    private fun hideDetailPanel() {
        if (animating) {
            return
        }

        val oneshot = detailInfoRootLayout.alpha == 0f
        toggleDetailLayoutAnimation(false, oneshot)
        toggleDownloadFlipperLayoutAnimation(false, oneshot)
        toggleShareBtnAnimation(false, oneshot)

        if (oneshot) {
            toggleHeroViewAnimation(detailImgRL.translationY, listPositionY, false)
        }
    }

    private fun extractThemeColor(image: UnsplashImage) = scope?.launch {
        val color = image.extractThemeColor()
        if (color != Int.MIN_VALUE) {
            updateThemeColor(color)
        } else {
            updateThemeColor(ContextCompat.getColor(context, R.color.primaryBackgroundColor))
        }
    }

    private fun updateThemeColor(themeColor: Int) {
        detailInfoRootLayout.background = ColorDrawable(themeColor)
        // change the color
        if (!themeColor.isLightColor()) {
            copyUrlTextView.setTextColor(Color.BLACK)
            val backColor = Color.argb(200, Color.red(Color.WHITE),
                    Color.green(Color.WHITE), Color.blue(Color.WHITE))
            copyLayout.setBackgroundColor(backColor)

            nameTextView.setTextColor(Color.WHITE)
            lineView.background = ColorDrawable(Color.WHITE)
            photoByTextView.setTextColor(Color.WHITE)
        } else {
            copyUrlTextView.setTextColor(Color.WHITE)
            val backColor = Color.argb(200, Color.red(Color.BLACK),
                    Color.green(Color.BLACK), Color.blue(Color.BLACK))
            copyLayout.setBackgroundColor(backColor)

            nameTextView.setTextColor(Color.BLACK)
            lineView.background = ColorDrawable(Color.BLACK)
            photoByTextView.setTextColor(Color.BLACK)
        }
    }

    @OnClick(R.id.detail_name_tv)
    fun onClickName() {
        viewModel.navigateToAuthorPage()
    }

    @OnClick(R.id.copy_url_flipper_layout)
    fun onClickCopy() {
        copyInternal()
    }

    private fun copyInternal() = scope?.launch {
        if (copied) return@launch
        copied = true

        copyUrlFlipperLayout.next()

        analysisHelper.logClickCopyUrl()

        viewModel.copyUrlToClipboard()
        delay(URL_COPIED_DELAY_MILLIS)
        copyUrlFlipperLayout.next()
        copied = false
    }

    @OnClick(R.id.detail_share_fab)
    fun onClickShare() {
        viewModel.share()
    }

    private fun doShare(image: UnsplashImage) {
        val context = context ?: return

        val file = FileUtils.getCachedFile(image.listUrl!!)

        if (file == null || !file.exists()) {
            Toaster.sendShortToast(context.getString(R.string.something_wrong))
            return
        }

        val shareText = if (image.isUnsplash) {
            context.getString(R.string.share_text, image.userName, image.downloadUrl)
        } else {
            context.getString(R.string.share_text_highlights,  image.downloadUrl)
        }

        val contentUri = FileProvider.getUriForFile(context,
                context.getString(R.string.authorities), file)
        launchShare(contentUri, shareText)
    }

    @OnClick(R.id.detail_download_fab)
    fun onClickDownload() {
        if (!PermissionUtils.check(context as Activity)) {
            Toaster.sendShortToast(context.getString(R.string.no_permission))
            return
        }

        val warn = LocalSettingHelper.getBoolean(context,
                context.getString(R.string.preference_key_download_via_metered_network), true)

        if (warn && !context.usingWifi()) {
            val builder = buildMeteredWarningDialog(context) {
                viewModel.download()
            }
            builder.create().show()
        } else {
            viewModel.download()
        }
    }

    @OnClick(R.id.detail_cancel_download_fab)
    fun onClickCancelDownload() {
        if (viewModel.cancelDownload()) {
            downloadFlipperLayout.updateIndex(DOWNLOAD_FLIPPER_LAYOUT_STATUS_DOWNLOAD)
        }
    }

    @OnClick(R.id.detail_set_as_fab)
    fun onClickSetAsFAB() {
        viewModel.setAs()
    }

    private fun launchEditActivity(uri: Uri) {
        val intent = Intent(context, EditActivity::class.java)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        context.startActivity(intent)
    }

    private fun navigateToAuthorPage(url: String) = guard {
        val uri = Uri.parse(url)

        val intentBuilder = CustomTabsIntent.Builder()

        intentBuilder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
        intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))

        intentBuilder.setStartAnimations(context, R.anim.in_from_right, R.anim.out_from_left)
        intentBuilder.setExitAnimations(context, R.anim.in_from_left, R.anim.out_from_right)

        val customTabsIntent = intentBuilder.build()

        customTabsIntent.launchUrl(context, uri)
    }

    private fun launchShare(uri: Uri, text: String) {
        val intent = Intent(Intent.ACTION_SEND)

        intent.apply {
            action = Intent.ACTION_SEND
            type = "image/*"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Share")
            putExtra(Intent.EXTRA_TEXT, text)
        }

        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_title)))
    }

    private var disposable: Disposable? = null

    /**
     * Show detailed image
     * @param clickData    clicked data info, including the rectF of clicked area and image.
     */
    fun show(clickData: ClickData) {
        if (animating) {
            return
        }

        val (rectF, unsplashImage, itemView) = clickData

        analysisHelper.logToggleImageDetails()

        if (clickedView != null) {
            return
        }

        val lp = photoView.layoutParams as ConstraintLayout.LayoutParams
        lp.dimensionRatio = unsplashImage.getDisplayRatio(context)
        photoView.layoutParams = lp

        photoView.setImageURI(unsplashImage.listUrl)

        scope?.cancel()
        scope = CoroutineScope(Dispatchers.Main)

        viewModel.unsplashImage = unsplashImage

        clickedView = itemView.apply {
            visibility = View.INVISIBLE
        }

        val themeColor = unsplashImage.themeColor

        if (!unsplashImage.isUnsplash) {
            photoByTextView.text = context.getString(R.string.recommended_by)

            extractThemeColor(unsplashImage)
        } else {
            photoByTextView.text = context.getString(R.string.photo_by)
            detailInfoRootLayout.background = ColorDrawable(themeColor)
        }

        updateThemeColor(themeColor)

        nameTextView.text = unsplashImage.userName
        progressView.progress = 5
        detailRootScrollView.visibility = View.VISIBLE

        val heroImagePosition = IntArray(2)
        detailImgRL.getLocationOnScreen(heroImagePosition)

        listPositionY = rectF.top

        disposable = viewModel.associatedDownloadItem
                ?.distinctUntilChanged { prev, current ->
                    prev == current
                }
                ?.delay { item ->
                    return@delay if (item.status == DownloadItem.DOWNLOAD_STATUS_OK && !animating) {
                        Flowable.just(item).delay(FlipperLayout.DEFAULT_DURATION_MILLIS, TimeUnit.MILLISECONDS)
                    } else {
                        Flowable.just(item)
                    }
                }
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { item ->
                    updateByItem(item)
                }

        toggleMaskAnimation(true)
        toggleHeroViewAnimation(listPositionY, getTargetY(unsplashImage.getDisplayRatioF(context)), true)
    }

    @UiThread
    private fun updateByItem(item: DownloadItem?) {
        Pasteur.info(TAG, "observe on new value: $item")
        when (item?.status) {
            DownloadItem.DOWNLOAD_STATUS_DOWNLOADING -> {
                progressView.progress = item.progress
                downloadFlipperLayout.updateIndex(DOWNLOAD_FLIPPER_LAYOUT_STATUS_DOWNLOADING)
            }
            DownloadItem.DOWNLOAD_STATUS_FAILED -> {
                downloadFlipperLayout.updateIndex(DOWNLOAD_FLIPPER_LAYOUT_STATUS_DOWNLOAD)
            }
            DownloadItem.DOWNLOAD_STATUS_OK -> {
                if (checkDownloadStatus(item)) {
                    val index = DOWNLOAD_FLIPPER_LAYOUT_STATUS_DOWNLOAD_OK
                    downloadFlipperLayout.updateIndex(index)
                }
            }
        }
    }

    private fun quickReset() {
        copyUrlFlipperLayout.updateIndexWithoutAnimation(0)
        downloadFlipperLayout.updateIndexWithoutAnimation(DOWNLOAD_FLIPPER_LAYOUT_STATUS_DOWNLOAD)
        copied = false
    }

    /**
     * Try to hide this view. If this view is fully displayed to user.
     */
    fun tryHide(): Boolean {
        if (animating) {
            return false
        }

        scope?.cancel()
        disposable?.dispose()
        disposable = null
        viewModel.onHide()
        if (detailRootScrollView.visibility == View.VISIBLE) {
            hideDetailPanel()
            return true
        }
        return false
    }
}
