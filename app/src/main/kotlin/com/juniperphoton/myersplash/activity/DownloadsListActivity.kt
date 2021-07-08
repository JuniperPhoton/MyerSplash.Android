package com.juniperphoton.myersplash.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.adapter.DownloadsListAdapter
import com.juniperphoton.myersplash.di.AppComponent
import com.juniperphoton.myersplash.extension.setVisible
import com.juniperphoton.myersplash.model.DownloadItem
import com.juniperphoton.myersplash.service.DownloadService
import com.juniperphoton.myersplash.utils.NotificationUtils
import com.juniperphoton.myersplash.utils.Params
import com.juniperphoton.myersplash.utils.Pasteur
import com.juniperphoton.myersplash.viewmodel.AppViewModelProviders
import com.juniperphoton.myersplash.viewmodel.DownloadListViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_manage_download.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

@Suppress("unused")
class DownloadsListActivity : BaseActivity(), DownloadsListAdapter.Callback, CoroutineScope by MainScope() {
    companion object {
        private const val TAG = "DownloadsListActivity"
        private const val DEFAULT_SPAN = 2
        private const val SCREEN_WIDTH_WITH_DEFAULT_SPAN = 1200
    }

    private lateinit var adapter: DownloadsListAdapter
    private lateinit var viewModel: DownloadListViewModel

    private val spanCount: Int
        get() {
            val width = window.decorView.width
            return if (width <= SCREEN_WIDTH_WITH_DEFAULT_SPAN) {
                DEFAULT_SPAN
            } else {
                val min = resources.getDimensionPixelSize(R.dimen.download_item_min_width)
                (width / min)
            }
        }

    private val deleteOptionsMap = mapOf(
            0 to DownloadItem.DOWNLOAD_STATUS_DOWNLOADING,
            1 to DownloadItem.DOWNLOAD_STATUS_OK,
            2 to DownloadItem.DOWNLOAD_STATUS_FAILED
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_download)

        viewModel = AppViewModelProviders.of(this).get(DownloadListViewModel::class.java)

        AppComponent.instance.analysisHelper.logEnterDownloads()

        moreFab.setOnClickListener(this)

        adapter = DownloadsListAdapter(this@DownloadsListActivity)
        adapter.callback = this@DownloadsListActivity
        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        val layoutManager = StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        // We don't change the item animator so we cast it directly
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        updateNoItemVisibility()

        viewModel.downloadItems.observe(this, Observer { items ->
            Pasteur.info(TAG) {
                "refresh items: ${items.size}"
            }
            adapter.refresh(items)
            updateNoItemVisibility()
        })

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent ?: return
        val id = intent.getIntExtra(NotificationUtils.EXTRA_NOTIFICATION_ID, Int.MIN_VALUE)
        if (id != Int.MIN_VALUE) {
            NotificationUtils.cancelNotification(id)
        }
    }

    override fun onClickView(v: View) {
        when (v.id) {
            R.id.moreFab -> {
                onClickMore()
            }
        }
    }

    private fun onClickMore() {
        AppComponent.instance.analysisHelper.logClickMoreButtonInDownloadList()

        AlertDialog.Builder(this).setTitle(R.string.clear_options_title)
                .setItems(R.array.delete_options) { _, i ->
                    viewModel.deleteByStatus(deleteOptionsMap[i]
                            ?: DownloadItem.DOWNLOAD_STATUS_INVALID)
                }
                .create()
                .show()
    }

    private fun updateNoItemVisibility() {
        noItemView.setVisible(adapter.data.isEmpty())
    }

    override fun onClickRetry(item: DownloadItem) {
        viewModel.resetItemStatus(item.id)

        val intent = Intent(this@DownloadsListActivity, DownloadService::class.java).apply {
            putExtra(Params.NAME_KEY, item.fileName)
            putExtra(Params.URL_KEY, item.downloadUrl)
        }
        startService(intent)
    }

    override fun onClickDelete(item: DownloadItem) {
        viewModel.deleteItem(item.id)

        val intent = Intent(this@DownloadsListActivity, DownloadService::class.java).apply {
            putExtra(Params.CANCELED_KEY, true)
            putExtra(Params.URL_KEY, item.downloadUrl)
        }

        startService(intent)
    }

    override fun onClickCancel(item: DownloadItem) {
        viewModel.updateItemStatus(item.id, DownloadItem.DOWNLOAD_STATUS_FAILED)

        val intent = Intent(this@DownloadsListActivity, DownloadService::class.java).apply {
            putExtra(Params.CANCELED_KEY, true)
            putExtra(Params.URL_KEY, item.downloadUrl)
        }

        startService(intent)
    }

    override fun onApplySystemInsets(top: Int, bottom: Int) {
        val params = moreFab.layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin = bottom + resources.getDimensionPixelSize(R.dimen.fab_margin)
        moreFab.layoutParams = params
    }
}
