package com.juniperphoton.myersplash.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.juniperphoton.myersplash.fragment.ImageListFragment
import com.juniperphoton.myersplash.model.UnsplashCategory

@Suppress("DEPRECATION")
class MainAdapter(fm: FragmentManager
) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return ImageListFragment.build(when (position) {
            0 -> {
                UnsplashCategory.NEW_CATEGORY_ID
            }
            1 -> {
                UnsplashCategory.DEVELOP_ID
            }
            2 -> {
                UnsplashCategory.HIGHLIGHTS_CATEGORY_ID
            }
            else -> throw IllegalArgumentException("Unknown position: $position")
        })
    }

    override fun getCount(): Int = 3
}