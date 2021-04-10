package com.juniperphoton.myersplash.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.fragment.ImageListFragment
import com.juniperphoton.myersplash.model.UnsplashCategory

@Suppress("DEPRECATION")
class MainAdapter(fm: FragmentManager
) : FragmentStatePagerAdapter(fm) {
    companion object {
        private val titlesMap = mutableMapOf(
                0 to R.string.pivot_new,
                1 to R.string.pivot_highlights,
                2 to R.string.pivot_random,
                3 to R.string.pivot_developer
        )
    }

    override fun getItem(position: Int): Fragment {
        return ImageListFragment.build(when (position) {
            0 -> {
                UnsplashCategory.NEW_CATEGORY_ID
            }
            1 -> {
                UnsplashCategory.HIGHLIGHTS_CATEGORY_ID
            }
            2 -> {
                UnsplashCategory.RANDOM_CATEGORY_ID
            }
            3 -> {
                UnsplashCategory.DEVELOPER_ID
            }
            else -> throw IllegalArgumentException("Unknown position: $position")
        })
    }

    override fun getCount(): Int = titlesMap.size

    override fun getPageTitle(position: Int): CharSequence {
        return App.instance.getString(titlesMap[position]!!)
    }
}