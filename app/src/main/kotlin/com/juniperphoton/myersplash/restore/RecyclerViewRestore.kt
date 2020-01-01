package com.juniperphoton.myersplash.restore

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

private const val SAVE_POSITION_KEY = "save_position"
private const val SAVE_OFFSET_KEY = "save_offset"

private const val TAG = "RecyclerViewRestore"

fun RecyclerView.savePosition(
        outState: Bundle,
        findPosition: ((RecyclerView.LayoutManager) -> Int)? = null
) {
    savePositionBy(outState, findPosition)
}

@Suppress("UNCHECKED_CAST")
fun <T : RecyclerView.LayoutManager> RecyclerView.savePositionBy(
        outState: Bundle,
        findPosition: ((T) -> Int)? = null
) {
    val manager = layoutManager as? T ?: return

    var offset = 0

    val position = if (findPosition != null) {
        findPosition(manager)
    } else {
        val itemAdapterPosition = when (manager) {
            is LinearLayoutManager -> {
                manager.findFirstVisibleItemPosition()
            }
            is StaggeredGridLayoutManager -> {
                manager.findFirstVisibleItemPositions(null).getOrNull(0) ?: RecyclerView.NO_POSITION
            }
            else -> {
                RecyclerView.NO_POSITION
            }
        }

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val childAdapterPosition = getChildAdapterPosition(child)
            if (childAdapterPosition == itemAdapterPosition) {
                offset = child.top
                val marginLp = child.layoutParams as? ViewGroup.MarginLayoutParams
                marginLp?.let {
                    offset -= marginLp.topMargin
                }
                break
            }
        }

        itemAdapterPosition
    }

    if (position >= 0) {
        Log.i(TAG, "saving position: $position, offset: $offset")
        outState.putInt(SAVE_POSITION_KEY, position)
        outState.putInt(SAVE_OFFSET_KEY, offset)
    }
}

fun RecyclerView.restorePosition(savedInstanceState: Bundle?,
                                 block: ((RecyclerView.LayoutManager) -> Unit)? = null) {
    savedInstanceState ?: return
    adapter ?: return

    val manager = layoutManager ?: return

    val position = savedInstanceState.getInt(SAVE_POSITION_KEY, -1)
    val offset = savedInstanceState.getInt(SAVE_OFFSET_KEY)

    savedInstanceState.remove(SAVE_POSITION_KEY)
    savedInstanceState.remove(SAVE_OFFSET_KEY)

    if (position >= 0) {
        Log.i(TAG, "restoring position: $position with offset $offset")

        if (block != null) {
            block(manager)
        } else {
            manager.scrollToPosition(position)
            post {
                scrollBy(0, -offset)
            }
        }
    }
}