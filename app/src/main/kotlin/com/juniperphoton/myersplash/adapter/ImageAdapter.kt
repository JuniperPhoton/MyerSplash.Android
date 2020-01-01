package com.juniperphoton.myersplash.adapter

import android.animation.ValueAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.extension.addDimensions
import com.juniperphoton.myersplash.extension.getNavigationBarSize
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.utils.Pasteur
import com.juniperphoton.myersplash.widget.item.OnClickPhotoListener
import com.juniperphoton.myersplash.widget.item.OnClickQuickDownloadListener
import com.juniperphoton.myersplash.widget.item.PhotoFooterView
import com.juniperphoton.myersplash.widget.item.PhotoItemView
import kotlin.math.ceil

class ImageAdapter(
        private val context: Context
) : RecyclerView.Adapter<ImageAdapter.PhotoViewHolder>() {
    companion object {
        private const val TAG = "PhotoAdapter"

        const val ITEM_TYPE_ITEM = 0
        const val ITEM_TYPE_FOOTER = 1

        private const val ID_FOOTER = 100L

        private const val BASE_DELAY_MILLIS = 100L
        private const val ANIMATION_DURATION_MILLIS = 400L
        private const val ITEM_SLIDE_IN_TRANSLATION_X = 100

        private const val LOAD_MORE_ITEMS_THRESHOLD = 10
    }

    private var isAutoLoadMore = true

    private var recyclerView: RecyclerView? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var lastPosition = -1

    private var footerView: PhotoFooterView? = null

    private val data = mutableListOf<UnsplashImage>()

    /**
     * Invoked when photo is clicked
     */
    var onClickPhoto: OnClickPhotoListener? = null

    /**
     * Invoked when quick-download button is clicked.
     * Note that [onClickPhoto] and [onClickQuickDownload] won't happened at the same time.
     */
    var onClickQuickDownload: OnClickQuickDownloadListener? = null

    private val maxPhotoCountOnScreen: Int
        get() {
            val height = recyclerView!!.height
            val imgHeight = recyclerView!!.resources.getDimensionPixelSize(R.dimen.img_height)
            return ceil(height.toDouble() / imgHeight.toDouble()).toInt()
        }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return when (viewType) {
            ITEM_TYPE_ITEM -> {
                val view = LayoutInflater.from(context).inflate(R.layout.row_photo, parent, false)
                PhotoViewHolder(view)
            }
            ITEM_TYPE_FOOTER -> {
                footerView = (LayoutInflater.from(context)
                        .inflate(R.layout.row_footer, parent, false) as PhotoFooterView).apply {
                    val padding = context.getNavigationBarSize().y
                    addDimensions(null, padding)
                    setPadding(0, 0, 0, padding)
                    toggleCollapsed()
                }
                PhotoViewHolder(footerView!!)
            }
            else -> throw IllegalArgumentException("unknown view type")
        }
    }

    override fun getItemId(position: Int): Long {
        if (isFooterView(position)) return ID_FOOTER
        return data.getOrNull(position)?.id?.hashCode()?.toLong() ?: RecyclerView.NO_ID
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        if (holder.itemView is PhotoItemView && !isFooterView(position)) {
            holder.itemView.onBind = { v, p ->
                animateContainer(v, p)
            }
            holder.itemView.onClickPhoto = onClickPhoto
            holder.itemView.onClickQuickDownload = onClickQuickDownload
            holder.itemView.bind(data[holder.adapterPosition], position)
        }
    }

    private fun findLastVisibleItemPosition(layoutManager: RecyclerView.LayoutManager?): Int {
        return if (layoutManager is LinearLayoutManager) {
            layoutManager.findLastVisibleItemPosition()
        } else -1
    }

    private fun animateContainer(container: View, position: Int) {
        val lastItemIndex = findLastVisibleItemPosition(layoutManager)
        if (position >= maxPhotoCountOnScreen || position <= lastPosition
                || lastItemIndex >= maxPhotoCountOnScreen) {
            return
        }

        lastPosition = position

        val delay = BASE_DELAY_MILLIS * (position + 1)
        val duration = ANIMATION_DURATION_MILLIS

        container.alpha = 0f
        container.translationX = ITEM_SLIDE_IN_TRANSLATION_X.toFloat()

        val animator = ValueAnimator.ofFloat(0.0f, 1.0f)
        animator.addUpdateListener { valueAnimator ->
            container.alpha = valueAnimator.animatedValue as Float
        }
        animator.startDelay = delay
        animator.duration = duration
        animator.start()

        val animator2 = ValueAnimator.ofInt(ITEM_SLIDE_IN_TRANSLATION_X, 0)
        animator2.addUpdateListener { valueAnimator ->
            container.translationX = (valueAnimator.animatedValue as Int).toFloat()
        }
        animator2.interpolator = AccelerateDecelerateInterpolator()
        animator2.startDelay = delay
        animator2.duration = duration
        animator2.start()
    }

    override fun getItemCount(): Int = data.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (isFooterView(position)) ITEM_TYPE_FOOTER else ITEM_TYPE_ITEM
    }

    private fun isFooterView(position: Int): Boolean = position >= itemCount - 1

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        lastPosition = -1
        layoutManager = recyclerView.layoutManager
    }

    fun indicateLoadMoreError() {
        footerView?.toggleCollapsed()
    }

    fun refresh(list: List<UnsplashImage>) {
        Pasteur.i(TAG) {
            "refresh with new list: ${list.size}"
        }

        data.clear()
        setLoadMoreData(list)

        val size = data.size
        when {
            size >= 10 -> {
                isAutoLoadMore = true
                footerView?.toggleLoading()
            }
            size > 0 -> {
                isAutoLoadMore = false
                footerView?.indicateEnd()
            }
            else -> {
                isAutoLoadMore = false
                footerView?.toggleCollapsed()
            }
        }
    }

    private fun setLoadMoreData(list: List<UnsplashImage>) {
        val size = list.size
        data.addAll(list)
        when {
            data.size >= LOAD_MORE_ITEMS_THRESHOLD -> {
                isAutoLoadMore = true
                footerView?.toggleLoading()
                notifyItemInserted(size)
            }
            data.size > 0 -> {
                isAutoLoadMore = false
                footerView?.indicateEnd()
                notifyItemInserted(size)
            }
            else -> {
                isAutoLoadMore = false
                footerView?.indicateEnd()
                notifyDataSetChanged()
            }
        }
    }

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}


