package com.juniperphoton.myersplash.activity

import android.animation.Animator
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Bundle
import android.os.PersistableBundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.adapter.MainListFragmentAdapter
import com.juniperphoton.myersplash.event.ScrollToTopEvent
import com.juniperphoton.myersplash.extension.getDimenInPixel
import com.juniperphoton.myersplash.extension.pow
import com.juniperphoton.myersplash.model.UnsplashCategory
import com.juniperphoton.myersplash.utils.AnimatorListeners
import com.juniperphoton.myersplash.utils.FileUtil
import com.juniperphoton.myersplash.utils.PermissionUtil
import com.juniperphoton.myersplash.widget.ImageDetailView
import com.juniperphoton.myersplash.widget.PivotTitleBar
import com.juniperphoton.myersplash.widget.SearchView
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus

class MainActivity : BaseActivity() {
    companion object {
        private const val SAVED_NAVIGATION_INDEX = "navigation_index"
        private const val DOWNLOADS_SHORTCUT_ID = "downloads_shortcut"
    }

    private var mainListFragmentAdapter: MainListFragmentAdapter? = null

    private var handleShortcut: Boolean = false
    private var initNavigationIndex = PivotTitleBar.DEFAULT_SELECTED
    private var fabPositionX: Int = 0
    private var fabPositionY: Int = 0

    private val idMaps = mutableMapOf(
            0 to UnsplashCategory.NEW_CATEGORY_ID,
            1 to UnsplashCategory.FEATURED_CATEGORY_ID,
            2 to UnsplashCategory.HIGHLIGHTS_CATEGORY_ID)

    @BindView(R.id.toolbar_layout)
    lateinit var toolbarLayout: AppBarLayout

    @BindView(R.id.pivot_title_bar)
    lateinit var pivotTitleBar: PivotTitleBar

    @BindView(R.id.view_pager)
    lateinit var viewPager: ViewPager

    @BindView(R.id.tag_view)
    lateinit var tagView: TextView

    @BindView(R.id.detail_view)
    lateinit var imageDetailView: ImageDetailView

    @BindView(R.id.search_fab)
    lateinit var searchFab: FloatingActionButton

    @BindView(R.id.search_view)
    lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        handleShortcutsAction()
        clearSharedFiles()

        if (savedInstanceState != null) {
            initNavigationIndex = savedInstanceState.getInt(SAVED_NAVIGATION_INDEX,
                    PivotTitleBar.DEFAULT_SELECTED)
        }

        initShortcuts()
        initMainViews()
    }

    private fun initShortcuts() {
        @TargetApi(android.os.Build.VERSION_CODES.N_MR1)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            val shortcutManager = getSystemService(ShortcutManager::class.java)
            if (shortcutManager.dynamicShortcuts.size > 0) {
                return
            }
            val intent = Intent(this, ManageDownloadActivity::class.java)
            intent.action = ManageDownloadActivity.ACTION
            val shortcut = ShortcutInfo.Builder(this, DOWNLOADS_SHORTCUT_ID)
                    .setShortLabel(getString(R.string.downloadLowercase))
                    .setLongLabel(getString(R.string.downloadLowercase))
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_download_shortcut))
                    .setIntent(intent)
                    .build()
            shortcutManager.dynamicShortcuts = listOf(shortcut)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        val index = viewPager.currentItem
        if (index in 0..2) {
            outState?.putInt(SAVED_NAVIGATION_INDEX, viewPager.currentItem)
        }
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onResume() {
        super.onResume()
        imageDetailView.registerEventBus()
        searchView.registerEventBus()

        PermissionUtil.checkAndRequest(this@MainActivity)
    }

    override fun onPause() {
        super.onPause()
        imageDetailView.unregisterEventBus()
        searchView.unregisterEventBus()
    }

    private fun toggleSearchView(show: Boolean, useAnimation: Boolean) {
        if (show) {
            searchFab.hide()
        } else {
            searchFab.show()
        }

        val location = IntArray(2)
        searchFab.getLocationOnScreen(location)

        if (show) {
            fabPositionX = (location[0] + searchFab.width / 2f).toInt()
            fabPositionY = (location[1] + searchFab.height / 2f).toInt()
        }

        val width = window.decorView.width
        val height = window.decorView.height

        val radius = Math.sqrt(width.pow() + height.pow()).toInt()
        val animator = ViewAnimationUtils.createCircularReveal(searchView,
                fabPositionX, fabPositionY,
                (if (show) 0 else radius).toFloat(), (if (show) radius else 0).toFloat())
        animator.addListener(object : AnimatorListeners.End() {
            override fun onAnimationEnd(a: Animator) {
                if (!show) {
                    searchView.reset()
                    searchView.visibility = View.GONE
                } else {
                    searchView.onShown()
                }
            }
        })

        searchView.visibility = View.VISIBLE

        if (show) {
            searchView.tryShowKeyboard()
            searchView.onShowing()
        } else {
            searchView.onHiding()
        }
        if (useAnimation) {
            animator.start()
        }
    }

    private fun clearSharedFiles() {
        if (!PermissionUtil.check(this)) {
            return
        }
        Completable.create {
            FileUtil.clearFilesToShared()
        }
                .subscribeOn(Schedulers.io())
                .subscribe({
                    // do nothing
                }, { e -> e.printStackTrace() })
    }

    private fun initMainViews() {
        imageDetailView.apply {
            onShowing = {
                searchFab.hide()
            }
            onHidden = {
                searchFab.show()
                if (toolbarLayout.height - Math.abs(toolbarLayout.top) < 0.01) {
                    tagView.animate().alpha(1f).setDuration(300).start()
                }
            }
        }

        searchFab.setOnClickListener {
            toggleSearchView(true, true)
        }

        pivotTitleBar.apply {
            onSingleTap = {
                viewPager.currentItem = it
                EventBus.getDefault().post(ScrollToTopEvent(idMaps[it]!!, false))
            }
            onDoubleTap = {
                viewPager.currentItem = it
                EventBus.getDefault().post(ScrollToTopEvent(idMaps[it]!!, true))
            }
            selectedItem = initNavigationIndex
        }

        mainListFragmentAdapter = MainListFragmentAdapter({ rectF, unsplashImage, itemView ->
            val location = IntArray(2)
            tagView.getLocationOnScreen(location)
            if (rectF.top <= location[1] + tagView.height) {
                tagView.animate().alpha(0f).setDuration(100).start()
            }
            imageDetailView.show(rectF, unsplashImage, itemView)
        }, supportFragmentManager)

        viewPager.apply {
            adapter = mainListFragmentAdapter
            currentItem = initNavigationIndex
            offscreenPageLimit = 1
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int,
                                            positionOffset: Float,
                                            positionOffsetPixels: Int) = Unit

                override fun onPageSelected(position: Int) {
                    pivotTitleBar.selectedItem = position
                    tagView.text = "# ${pivotTitleBar.selectedString}"
                }

                override fun onPageScrollStateChanged(state: Int) = Unit
            })
        }

        toolbarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (Math.abs(verticalOffset) - appBarLayout.height == 0) {
                tagView.animate().alpha(1f).setDuration(300).start()
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE
                searchFab.hide()
            } else {
                tagView.animate().alpha(0f).setDuration(100).start()
                window.decorView.systemUiVisibility = 0
                searchFab.show()
            }
        }

        tagView.setOnClickListener { EventBus.getDefault().post(ScrollToTopEvent(idMaps[pivotTitleBar.selectedItem]!!, false)) }
    }

    override fun onConfigNavigationBar(hasNavigationBar: Boolean) {
        if (!hasNavigationBar) {
            val params = searchFab.layoutParams as RelativeLayout.LayoutParams
            params.setMargins(0, 0, getDimenInPixel(24), getDimenInPixel(24))
            searchFab.layoutParams = params
        }
    }

    private fun handleShortcutsAction() {
        if (handleShortcut) {
            return
        }
        val action = intent.action
        if (action != null) {
            when (action) {
                "action.search" -> {
                    handleShortcut = true
                    toolbarLayout.post { toggleSearchView(true, false) }
                }
                "action.download" -> {
                    val intent = Intent(this, ManageDownloadActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onBackPressed() {
        if (searchView.visibility == View.VISIBLE) {
            if (searchView.tryHide()) {
                return
            }
            toggleSearchView(false, true)
            return
        }
        if (imageDetailView.tryHide()) {
            return
        }
        super.onBackPressed()
    }
}
