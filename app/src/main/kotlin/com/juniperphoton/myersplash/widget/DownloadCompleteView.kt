package com.juniperphoton.myersplash.widget

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.activity.EditActivity
import com.juniperphoton.myersplash.extension.isLightColor
import com.juniperphoton.myersplash.utils.AnalysisHelper
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
@Suppress("unused")
class DownloadCompleteView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    @BindView(R.id.widget_set_as_rl)
    lateinit var setAsBtn: RelativeLayout

    @BindView(R.id.widget_set_as_root_rl)
    lateinit var setAsRL: RelativeLayout

    @BindView(R.id.set_as_tv)
    lateinit var setAsTextView: TextView

    var filePath: String? = null

    @Inject
    lateinit var analysisHelper: AnalysisHelper

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_download_complete_view, this)
        ButterKnife.bind(this)
    }

    @OnClick(R.id.widget_set_as_rl)
    fun setAs() {
        analysisHelper.logClickSetAsInDownloadList()
        filePath?.let {
            val intent = Intent(context, EditActivity::class.java)
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(it)))
            context.startActivity(intent)
        }
    }

    fun setThemeBackColor(color: Int) {
        setAsRL.background = ColorDrawable(color)
        setAsTextView.setTextColor(if (color.isLightColor()) Color.BLACK else Color.WHITE)
    }
}
