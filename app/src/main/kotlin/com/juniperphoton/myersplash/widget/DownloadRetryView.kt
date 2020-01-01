package com.juniperphoton.myersplash.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.extension.isLightColor

@Suppress("unused")
class DownloadRetryView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    @BindView(R.id.widget_retry_rl)
    lateinit var retryRL: RelativeLayout

    @BindView(R.id.retry_tv)
    lateinit var retryTextView: TextView

    @BindView(R.id.widget_retry_btn)
    lateinit var retryBtn: View

    @BindView(R.id.delete_btn)
    lateinit var deleteView: ImageView

    @BindView(R.id.delete_btn_root)
    lateinit var deleteRoot: View

    var onClickDelete: Action? = null
    var onClickRetry: Action? = null

    var themeColor: Int = Color.TRANSPARENT
        set(color) {
            retryRL.background = ColorDrawable(color)
            retryTextView.setTextColor(if (color.isLightColor()) Color.BLACK else Color.WHITE)
            if (color.isLightColor()) {
                deleteView.setImageResource(R.drawable.vector_ic_delete_black)
            }
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_download_retry_view, this)
        ButterKnife.bind(this)
    }

    @OnClick(R.id.delete_btn_root)
    fun onClickDelete() {
        onClickDelete?.invoke()
    }

    @OnClick(R.id.widget_retry_btn)
    fun onClickRetry() {
        onClickRetry?.invoke()
    }
}
