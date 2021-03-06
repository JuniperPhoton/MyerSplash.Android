package com.juniperphoton.myersplash

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.juniperphoton.myersplash.activity.AboutActivity
import com.juniperphoton.myersplash.activity.BaseActivity
import com.juniperphoton.myersplash.activity.DownloadsListActivity
import com.juniperphoton.myersplash.activity.SettingsActivity
import com.juniperphoton.myersplash.adapter.MainAdapter
import com.juniperphoton.myersplash.di.AppComponent
import com.juniperphoton.myersplash.extension.pow
import com.juniperphoton.myersplash.extension.startServiceSafely
import com.juniperphoton.myersplash.service.DownloadService
import com.juniperphoton.myersplash.utils.Params
import com.juniperphoton.myersplash.utils.Pasteur
import com.juniperphoton.myersplash.utils.PermissionUtils
import com.juniperphoton.myersplash.viewmodel.AppViewModelProviders
import com.juniperphoton.myersplash.viewmodel.ImageSharedViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.abs
import kotlin.math.sqrt

class MainActivity : BaseActivity() {
    companion object {
        private const val TAG = "MainActivity"

        private const val SAVED_NAVIGATION_INDEX = "navigation_index"
        private const val DOWNLOADS_SHORTCUT_ID = "downloads_shortcut"

        private const val ACTION_SEARCH = "action.search"
        private const val ACTION_DOWNLOADS = "action.download"

        private val menuMap: Map<Int, Class<out Any>> = mapOf(
                R.id.menu_settings to SettingsActivity::class.java,
                R.id.menu_downloads to DownloadsListActivity::class.java,
                R.id.menu_about to AboutActivity::class.java
        )
    }

    private var mainAdapter: MainAdapter? = null

    private var handleShortcut: Boolean = false
    private var initNavigationIndex = 0
    private var fabPositionX: Int = 0
    private var fabPositionY: Int = 0

    private lateinit var sharedViewModel: ImageSharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedViewModel = AppViewModelProviders.of(this).get(ImageSharedViewModel::class.java).apply {
            onClickedImage.observe(this@MainActivity, Observer { data ->
                data?.consume {
                    Pasteur.info(TAG) {
                        "onClickedImage $data"
                    }

                    val rectF = it.rectF

                    val location = IntArray(2)
                    tagView.getLocationOnScreen(location)
                    if (rectF.top <= location[1] + tagView.height) {
                        tagView.animate().alpha(0f).setDuration(100).start()
                    }

                    imageDetailView.show(it)
                }
            })
        }

        handleShortcutsAction()

        if (savedInstanceState != null) {
            initNavigationIndex = savedInstanceState.getInt(SAVED_NAVIGATION_INDEX, 0)
        }

        initShortcuts()
        initMainViews()

        if (savedInstanceState == null) {
            startServiceToCheck()
        }
    }

    private fun startServiceToCheck() {
        val intent = Intent(this, DownloadService::class.java).apply {
            putExtra(Params.CHECK_STATUS, true)
        }
        startServiceSafely(intent)
    }

    private fun initShortcuts() {
        @TargetApi(android.os.Build.VERSION_CODES.N_MR1)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            val shortcutManager = getSystemService(ShortcutManager::class.java) ?: return
            if (shortcutManager.dynamicShortcuts.size > 0) {
                shortcutManager.removeAllDynamicShortcuts()
            }
            val intent = Intent(this, DownloadsListActivity::class.java)
            intent.action = DownloadsListActivity.ACTION
            val shortcut = ShortcutInfo.Builder(this, DOWNLOADS_SHORTCUT_ID)
                    .setShortLabel(getString(R.string.downloadLowercase))
                    .setLongLabel(getString(R.string.downloadLowercase))
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_download_shortcut))
                    .setIntent(intent)
                    .build()
            shortcutManager.dynamicShortcuts = listOf(shortcut)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val index = viewPager.currentItem
        if (index in 0..2) {
            outState.putInt(SAVED_NAVIGATION_INDEX, viewPager.currentItem)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        PermissionUtils.checkAndRequest(this@MainActivity)
    }

    private fun toggleSearchView(show: Boolean, useAnimation: Boolean) {
        if (show) {
            searchFab.hide()
        } else {
            AppComponent.instance.analysisHelper.logEnterSearch()
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

        val radius = sqrt(width.pow() + height.pow()).toInt()
        val animator = ViewAnimationUtils.createCircularReveal(searchView,
                fabPositionX, fabPositionY,
                (if (show) 0 else radius).toFloat(), (if (show) radius else 0).toFloat())
        animator.addListener(object : AnimatorListenerAdapter() {
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

    private fun initMainViews() {
        imageDetailView.apply {
            onShowing = {
                searchFab.hide()
            }
            onHidden = {
                searchFab.show()
                if (toolbarLayout.height - abs(toolbarLayout.top) < 0.01) {
                    tagView.animate().alpha(1f).setDuration(300).start()
                }
            }
        }

        searchFab.setOnClickListener {
            toggleSearchView(show = true, useAnimation = true)
        }

        mainAdapter = MainAdapter(supportFragmentManager)

        viewPager.apply {
            adapter = mainAdapter
            currentItem = initNavigationIndex
            offscreenPageLimit = 3
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int,
                                            positionOffset: Float,
                                            positionOffsetPixels: Int) = Unit

                override fun onPageSelected(position: Int) {
                    val text = tabLayout.getTabAt(position)?.text
                    val title = "# $text"
                    tagView.text = title
                    AppComponent.instance.analysisHelper.logTabSelected(title)
                }

                override fun onPageScrollStateChanged(state: Int) = Unit
            })
        }

        tabLayout.tabRippleColor = null
        tabLayout.setupWithViewPager(viewPager)

        tagView.text = "# ${getString(R.string.pivot_new)}"

        toolbarLayout.addOnOffsetChangedListener(
                AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                    if (searchView.visibility == View.VISIBLE) {
                        return@OnOffsetChangedListener
                    }
                    if (abs(verticalOffset) - appBarLayout.height == 0) {
                        //todo extract duration
                        tagView.animate().alpha(1f).setDuration(300).start()
                        val currentFlag = window.decorView.systemUiVisibility
                        window.decorView.systemUiVisibility = currentFlag or View.SYSTEM_UI_FLAG_LOW_PROFILE
                        searchFab.hide()
                    } else {
                        tagView.animate().alpha(0f).setDuration(100).start()
                        val currentFlag = window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LOW_PROFILE.inv()
                        window.decorView.systemUiVisibility = currentFlag
                        searchFab.show()
                    }
                })

        tagView.setOnClickListener {
            sharedViewModel.onRequestScrollToTop.value = tabLayout.selectedTabPosition.liveDataEvent
        }

        moreBtn.setOnClickListener {
            val popupMenu = PopupMenu(this, moreBtn)
            popupMenu.inflate(R.menu.main)
            popupMenu.gravity = Gravity.END
            popupMenu.setOnMenuItemClickListener { item ->
                val intent: Intent?
                intent = Intent(this, menuMap[item.itemId])
                this.startActivity(intent)
                true
            }
            popupMenu.show()
        }
    }

    override fun onApplySystemInsets(top: Int, bottom: Int) {
        val params = searchFab.layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin = bottom + resources.getDimensionPixelSize(R.dimen.fab_margin)
        searchFab.layoutParams = params
    }

    private fun handleShortcutsAction() {
        if (handleShortcut) {
            return
        }
        val action = intent.action
        if (action != null) {
            when (action) {
                ACTION_SEARCH -> {
                    handleShortcut = true
                    toolbarLayout.post { toggleSearchView(show = true, useAnimation = false) }
                }
                ACTION_DOWNLOADS -> {
                    val intent = Intent(this, DownloadsListActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onBackPressed() {
        if (imageDetailView.tryHide()) {
            return
        }

        if (searchView.visibility == View.VISIBLE) {
            toggleSearchView(show = false, useAnimation = true)
            return
        }

        super.onBackPressed()
    }
}
