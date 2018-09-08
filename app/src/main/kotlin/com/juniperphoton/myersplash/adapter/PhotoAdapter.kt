package com.juniperphoton.myersplash.adapter

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.RectF
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.widget.item.PhotoFooterView
import com.juniperphoton.myersplash.widget.item.PhotoItemView

class PhotoAdapter(private val imageData: MutableList<UnsplashImage>,
                   private val context: Context
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {
    companion object {
        const val ITEM_TYPE_ITEM = 0
        const val ITEM_TYPE_FOOTER = 1
    }

    private var isAutoLoadMore = true

    private var recyclerView: RecyclerView? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var lastPosition = -1

    private var footerView: PhotoFooterView? = null

    /**
     * Invoked when photo is clicked
     */
    var onClickPhoto: ((rectF: RectF, unsplashImage: UnsplashImage, itemView: View) -> Unit)? = null

    /**
     * Invoked when quick-download button is clicked.
     * Note that [onClickPhoto] and [onClickQuickDownload] won't happened at the same time.
     */
    var onClickQuickDownload: ((image: UnsplashImage) -> Unit)? = null

    val firstImage: UnsplashImage?
        get() {
            return if (imageData.size > 0) {
                imageData[0]
            } else null
        }

    init {
        lastPosition = -1
        val size = imageData.size
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
                footerView?.toggleCollasped()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return when (viewType) {
            ITEM_TYPE_ITEM -> {
                val view = LayoutInflater.from(context).inflate(R.layout.row_photo, parent, false)
                PhotoViewHolder(view)
            }
            ITEM_TYPE_FOOTER -> {
                footerView = LayoutInflater.from(context)
                        .inflate(R.layout.row_footer, parent, false) as PhotoFooterView
                PhotoViewHolder(footerView!!)
            }
            else -> throw IllegalArgumentException("unknown view type")
        }
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        if (holder.itemView is PhotoItemView && !isFooterView(position)) {
            holder.itemView.onBind = { v, p ->
                animateContainer(v, p)
            }
            holder.itemView.onClickPhoto = onClickPhoto
            holder.itemView.onClickQuickDownload = onClickQuickDownload
            holder.itemView.bind(imageData[holder.adapterPosition], position)
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

        val delay = 300 * (position + 1)
        val duration = 800

        container.alpha = 0f
        container.translationX = 300f

        val animator = ValueAnimator.ofFloat(0.0f, 1.0f)
        animator.addUpdateListener { valueAnimator -> container.alpha = valueAnimator.animatedValue as Float }
        animator.startDelay = delay.toLong()
        animator.duration = duration.toLong()
        animator.start()

        val animator2 = ValueAnimator.ofInt(300, 0)
        animator2.addUpdateListener { valueAnimator -> container.translationX = (valueAnimator.animatedValue as Int).toFloat() }
        animator2.interpolator = DecelerateInterpolator()
        animator2.startDelay = delay.toLong()
        animator2.duration = duration.toLong()
        animator2.start()
    }

    private val maxPhotoCountOnScreen: Int
        get() {
            val height = recyclerView!!.height
            val imgHeight = recyclerView!!.resources.getDimensionPixelSize(R.dimen.img_height)
            return Math.ceil(height.toDouble() / imgHeight.toDouble()).toInt()
        }

    override fun getItemCount(): Int = imageData.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (isFooterView(position)) ITEM_TYPE_FOOTER else ITEM_TYPE_ITEM
    }

    private fun isFooterView(position: Int): Boolean = position >= itemCount - 1

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        lastPosition = -1
        layoutManager = recyclerView!!.layoutManager
    }

    fun clear() {
        footerView?.toggleCollasped()
        imageData.clear()
        notifyDataSetChanged()
    }

    fun setLoadMoreData(data: MutableList<UnsplashImage>) {
        val size = this.imageData.size
        this.imageData.addAll(data)
        when {
            data.size >= 10 -> {
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


