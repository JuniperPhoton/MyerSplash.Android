package com.juniperphoton.myersplash.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.extension.isLightColor
import com.juniperphoton.myersplash.misc.Action

@Suppress("unused")
class DownloadingView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    @BindView(R.id.downloading_progress_pv)
    lateinit var progressView: ProgressView

    @BindView(R.id.downloading_progress_tv)
    lateinit var progressTV: TextView

    @BindView(R.id.downloading_root_rl)
    lateinit var rootRL: RelativeLayout

    @BindView(R.id.downloading_cancel_rl)
    lateinit var cancelRL: RelativeLayout

    @BindView(R.id.cancel_ic)
    lateinit var cancelImageView: ImageView

    var onClickCancel: Action? = null

    var themeColor: Int = Color.TRANSPARENT
        set(value) {
            rootRL.background = ColorDrawable(value)
            progressView.color = value
            if (value.isLightColor()) {
                progressTV.setTextColor(Color.BLACK)
                cancelImageView.setImageResource(R.drawable.vector_ic_clear_black)
            } else {
                progressTV.setTextColor(Color.WHITE)
            }
        }

    var progress: Int = 0
        set(value) {
            field = value
            progressView.progress = value
            progressTV.text = "$value%"
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_downloading_view, this)
        ButterKnife.bind(this)
    }

    @OnClick(R.id.downloading_cancel_rl)
    fun onCancel() {
        onClickCancel?.invoke()
    }
}
