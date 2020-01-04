package com.juniperphoton.myersplash.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.juniperphoton.flipperlayout.FlipperLayout
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.model.DownloadItem
import com.juniperphoton.myersplash.utils.Pasteur
import com.juniperphoton.myersplash.widget.DownloadCompleteView
import com.juniperphoton.myersplash.widget.DownloadRetryView
import com.juniperphoton.myersplash.widget.DownloadingView

class DownloadsListAdapter(private val context: Context) :
        RecyclerView.Adapter<DownloadsListAdapter.DownloadItemViewHolder>() {
    companion object {
        private const val TAG = "DownloadsListAdapter"
        private const val ITEM_TYPE_ITEM = 0
        private const val MAX_DIMENSION_PREVIEW_PX = 500
    }

    interface Callback {
        fun onClickRetry(item: DownloadItem)
        fun onClickDelete(item: DownloadItem)
        fun onClickCancel(item: DownloadItem)
    }

    val data: MutableList<DownloadItem> = mutableListOf()
    var callback: Callback? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        val item = data.getOrNull(position) ?: return RecyclerView.NO_ID
        return item.id.hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadItemViewHolder {
        return when (viewType) {
            ITEM_TYPE_ITEM -> {
                val view = LayoutInflater.from(context)
                        .inflate(R.layout.row_download_item, parent, false)
                DownloadItemViewHolder(view)
            }
            else -> throw IllegalArgumentException("unknown view type")
        }
    }

    override fun onBindViewHolder(holder: DownloadItemViewHolder, position: Int) {
        holder.bind(data[holder.adapterPosition])
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int {
        return ITEM_TYPE_ITEM
    }

    fun refresh(items: List<DownloadItem>) {
        Pasteur.info(TAG, "refresh items: ${items.size}")
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    inner class DownloadItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var draweeView: SimpleDraweeView? = itemView.findViewById(R.id.row_download_item_dv)
        private var flipperLayout: FlipperLayout? = itemView.findViewById(R.id.row_download_flipper_layout)
        private var downloadingView: DownloadingView? = itemView.findViewById(R.id.row_downloading_view)
        private var downloadRetryView: DownloadRetryView? = itemView.findViewById(R.id.row_download_retry_view)
        private var downloadCompleteView: DownloadCompleteView? = itemView.findViewById(R.id.row_download_complete_view)

        private var downloadItem: DownloadItem? = null

        init {
            downloadRetryView?.onClickDelete = onDelete@{
                downloadItem?.let {
                    callback?.onClickDelete(it)
                }
            }

            downloadRetryView?.onClickRetry = onRetry@{
                downloadItem?.let {
                    flipperLayout?.next(DownloadItem.DOWNLOAD_STATUS_DOWNLOADING)
                    callback?.onClickRetry(it)
                }
            }

            downloadingView?.onClickCancel = onCancel@{
                downloadItem?.let {
                    flipperLayout?.next(DownloadItem.DOWNLOAD_STATUS_FAILED)
                    callback?.onClickCancel(it)
                }
            }
        }

        fun bind(item: DownloadItem) {
            if (this.downloadItem == item) {
                return
            }

            this.downloadItem = item

            draweeView?.let {
                val request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(item.thumbUrl))
                        .setResizeOptions(ResizeOptions(MAX_DIMENSION_PREVIEW_PX,
                                MAX_DIMENSION_PREVIEW_PX)).build()
                val controller = Fresco.newDraweeControllerBuilder().setImageRequest(request)
                        .setOldController(it.controller).build()
                it.controller = controller

                val lp = it.layoutParams as? ConstraintLayout.LayoutParams
                lp?.let { param ->
                    val w = item.width
                    val h = item.height
                    if (w > 0 && h > 0) {
                        param.dimensionRatio = "$w:$h"
                        it.layoutParams = param
                    }
                }
            }

            downloadingView?.progress = item.progress

            downloadCompleteView?.let {
                it.filePath = item.filePath
                it.setThemeBackColor(item.color)
            }

            downloadRetryView?.let {
                it.themeColor = item.color
            }

            downloadingView?.let {
                it.progress = item.progress
                it.themeColor = item.color
            }

            val last = item.lastStatus
            if (last != item.status && last != DownloadItem.DISPLAY_STATUS_NOT_SPECIFIED) {
                flipperLayout?.next(item.status)
                item.syncStatus()
            } else {
                flipperLayout?.next(item.status, false)
                item.syncStatus()
            }
        }
    }
}
