package com.juniperphoton.myersplash.activity

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.WorkerThread
import androidx.core.content.FileProvider
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.backends.pipeline.PipelineDraweeController
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.juniperphoton.flipperlayout.FlipperLayout
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.extension.getScreenHeight
import com.juniperphoton.myersplash.utils.*
import com.juniperphoton.myersplash.widget.edit.PreviewDraweeLayout
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream

class EditActivity : BaseActivity() {
    companion object {
        private const val TAG = "EditActivity"
        private const val SAVED_FILE_NAME = "final_dim_image.jpg"
    }

    @BindView(R.id.edit_seek_bar_brightness)
    lateinit var brightnessSeekBar: SeekBar

    @BindView(R.id.edit_image_preview)
    lateinit var previewImageView: SimpleDraweeView

    @BindView(R.id.edit_image_preview_layout)
    lateinit var previewDraweeLayout: PreviewDraweeLayout

    @BindView(R.id.edit_mask)
    lateinit var maskView: View

    @BindView(R.id.edit_flipper_layout)
    lateinit var flipperLayout: FlipperLayout

    @BindView(R.id.edit_progress_ring)
    lateinit var progressView: View

    @BindView(R.id.edit_home_preview)
    lateinit var homePreview: View

    @BindView(R.id.edit_progress_text)
    lateinit var progressText: TextView

    @BindView(R.id.edit_bottom_bar)
    lateinit var bottomBar: ViewGroup

    private val fileUri: Uri by lazy {
        return@lazy intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
                ?: throw IllegalArgumentException("image url should not be null")
    }

    private var showingPreview: Boolean = false
        set(value) {
            field = value
            homePreview.alpha = if (value) 1f else 0f
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        ButterKnife.bind(this)

        loadImage()
        initView()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
        loadImage()
    }

    override fun onResume() {
        super.onResume()
        // Reset to the initial state anyway
        flipperLayout.next(0)
    }

    @OnClick(R.id.edit_confirm_fab)
    fun onClickConfirm() {
        AnalysisHelper.logApplyEdit(brightnessSeekBar.progress > 0)
        composeMask()
    }

    @OnClick(R.id.edit_preview_fab)
    fun onClickPreview() {
        if (!showingPreview) {
            AnalysisHelper.logEditShowPreview()
        }
        showingPreview = !showingPreview
    }

    private fun loadImage() {
        previewImageView.post {
            updatePreviewImage()
        }
    }

    private fun initView() {
        brightnessSeekBar.setOnSeekBarChangeListener(object : SimpleOnSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                progressText.text = progress.toString()
                maskView.alpha = progress * 1f / 100
            }
        })

        val valueAnimator = ValueAnimator.ofFloat(0f, 360f)
        valueAnimator.addUpdateListener { animation ->
            progressView.rotation = animation.animatedValue as Float
        }
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.duration = 1200
        valueAnimator.repeatMode = ValueAnimator.RESTART
        valueAnimator.repeatCount = ValueAnimator.INFINITE
        valueAnimator.start()
    }

    private fun updatePreviewImage() {
        val resize = Math.max(previewImageView.height,
                previewImageView.width)

        val request = ImageRequestBuilder.newBuilderWithSource(fileUri)
                .setResizeOptions(ResizeOptions(resize, resize))
                .build()
        val controller = Fresco.newDraweeControllerBuilder()
                .setOldController(previewImageView.controller)
                .setImageRequest(request)
                .setControllerListener(object : SimpleControllerListener() {
                    override fun onFinalImageSet(id: String?,
                                                 imageInfo: ImageInfo?,
                                                 animatable: Animatable?) {
                        val rect = RectF()
                        previewImageView.hierarchy.getActualImageBounds(rect)
                        previewDraweeLayout.updateContentScale(rect)
                    }
                })
                .build() as PipelineDraweeController

        previewImageView.controller = controller
    }

    private fun setAs(file: File) {
        Pasteur.d(TAG, "set as, file path:${file.absolutePath}")

        val uri = FileProvider.getUriForFile(App.instance,
                App.instance.getString(R.string.authorities), file)

        try {
            val intent = IntentUtil.getSetAsWallpaperIntent(uri)
            startActivity(intent)
        } catch (e: IllegalArgumentException) {
            val bm = MediaStore.Images.Media.getBitmap(contentResolver, uri);
            WallpaperManager.getInstance(this).setBitmap(bm)
        }
    }

    private fun composeMask() {
        flipperLayout.next()
        Observable.just(fileUri)
                .subscribeOn(Schedulers.io())
                .map {
                    composeMaskInternal() ?: throw RuntimeException("Error")
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SimpleObserver<File>() {
                    override fun onError(e: Throwable) {
                        flipperLayout.next()
                        super.onError(e)
                        if (e is OutOfMemoryError) {
                            ToastService.sendShortToast(resources.getString(R.string.oom_toast))
                        }
                    }

                    override fun onNext(data: File) {
                        setAs(data)
                    }
                })
    }

    override fun onApplySystemInsets(top: Int, bottom: Int) {
        bottomBar.setPadding(0, 0, 0, bottomBar.paddingBottom + bottom)
    }

    @SuppressLint("WrongThread")
    @WorkerThread
    private fun composeMaskInternal(): File? {
        val opt = BitmapFactory.Options()
        opt.inJustDecodeBounds = true

        // First decode bounds to get width and height
        val inputStream = contentResolver.openInputStream(fileUri)
        inputStream.use {
            BitmapFactory.decodeStream(inputStream, null, opt)
        }

        val originalHeight = opt.outHeight

        val screenHeight = getScreenHeight()
        opt.inSampleSize = originalHeight / screenHeight
        opt.inJustDecodeBounds = false
        opt.inMutable = true

        // Decode file with specified sample size
        val bm = decodeBitmapFromFile(fileUri, opt)
                ?: throw IllegalStateException("Can't decode file")

        Pasteur.d(TAG, "file decoded, sample size:${opt.inSampleSize}, " +
                "originalHeight=$originalHeight, screenH=$screenHeight")

        Pasteur.d(TAG, "decoded size: ${bm.width} x ${bm.height}")

        val c = Canvas(bm)

        val paint = Paint()
        paint.isDither = true

        val alpha = maskView.alpha
        paint.color = Color.argb((255 * alpha).toInt(), 0, 0, 0)
        paint.style = Paint.Style.FILL

        // Draw the mask
        c.drawRect(0f, 0f, bm.width.toFloat(), bm.height.toFloat(), paint)

        Pasteur.d(TAG, "final bitmap drawn")

        val finalFile = File(FileUtil.galleryPath, SAVED_FILE_NAME)
        val fos = FileOutputStream(finalFile)
        fos.use {
            bm.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }

        bm.recycle()
        inputStream?.close()

        return finalFile
    }

    private fun decodeBitmapFromFile(fileUri: Uri?, opt: BitmapFactory.Options?): Bitmap? {
        fileUri ?: return null

        val inputStream = contentResolver.openInputStream(fileUri)
        var bm: Bitmap? = null
        inputStream.use {
            bm = BitmapFactory.decodeStream(inputStream, null, opt)
        }
        return bm
    }
}