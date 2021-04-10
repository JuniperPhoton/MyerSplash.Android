package com.juniperphoton.myersplash.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.ButterKnife
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.adapter.ImageAdapter
import com.juniperphoton.myersplash.di.AppComponent
import com.juniperphoton.myersplash.di.DaggerImageComponent
import com.juniperphoton.myersplash.extension.setVisible
import com.juniperphoton.myersplash.extension.usingWifi
import com.juniperphoton.myersplash.liveDataEvent
import com.juniperphoton.myersplash.model.UnsplashCategory
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.utils.*
import com.juniperphoton.myersplash.viewmodel.AppViewModelProviders
import com.juniperphoton.myersplash.viewmodel.ImageListViewModel
import com.juniperphoton.myersplash.viewmodel.ImageSharedViewModel
import com.juniperphoton.myersplash.viewmodel.SearchImageViewModel
import kotlinx.android.synthetic.main.detail_no_item.*
import kotlinx.android.synthetic.main.fragment_list.*

@Suppress("unused", "unused_parameter")
class ImageListFragment : Fragment() {
    companion object {
        private const val TAG = "ImageListFragment"
        private const val SCROLL_DETECTION_FACTOR_PX = 20
        private const val SCROLL_START_POSITION = 5

        private const val EXTRA_CATEGORY_ID = "extra_category_id"
        private const val EXTRA_QUERY = "extra_query"

        fun build(id: Int, query: String? = null) = ImageListFragment().apply {
            arguments = Bundle().apply {
                putInt(EXTRA_CATEGORY_ID, id)
                putString(EXTRA_QUERY, query)
            }
        }
    }

    private var adapter: ImageAdapter? = null

    private lateinit var viewModel: ImageListViewModel
    private lateinit var sharedViewModel: ImageSharedViewModel

    private var query: String? = null
    private var type: Int = -1

    private var fromRestore = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        type = arguments?.getInt(EXTRA_CATEGORY_ID, -1) ?: -1
        if (type == -1) {
            return
        }

        query = arguments?.getString(EXTRA_QUERY)

        sharedViewModel = AppViewModelProviders.of(requireActivity()).get(ImageSharedViewModel::class.java)

        viewModel = if (type == UnsplashCategory.SEARCH_ID) {
            AppViewModelProviders.of(this).get(SearchImageViewModel::class.java)
        } else {
            AppViewModelProviders.of(this).get(ImageListViewModel::class.java)
        }

        val component = DaggerImageComponent.builder().appComponent(AppComponent.instance)
                .type(type).build()
        component.inject(viewModel)

        if (viewModel is SearchImageViewModel) {
            (viewModel as SearchImageViewModel).searchKeyword = query
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(requireActivity()).inflate(R.layout.fragment_list, container, false)
        ButterKnife.bind(this, view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fromRestore = savedInstanceState != null

        viewModel.apply {
            val restored = init()
            if (!restored) {
                refresh()
            }

            images.observe(viewLifecycleOwner, Observer { images ->
                images ?: return@Observer

                Pasteur.info(TAG) {
                    "refresh, $this"
                }

                adapter?.refresh(images, !fromRestore)
                fromRestore = false
            })

            refreshing.observe(viewLifecycleOwner, { e ->
                e?.consume {
                    refreshLayout.isRefreshing = it
                }
            })

            refreshingWithNoData.observe(viewLifecycleOwner, { e ->
                e?.consume {
                    contentProgressBar.setVisible(it)
                }
            })

            showError.observe(viewLifecycleOwner, { e ->
                e?.consume {
                    updateNoItemVisibility(it)
                }
            })

            showLoadingMoreError.observe(viewLifecycleOwner, { e ->
                e?.consume {
                    updateNoItemVisibility(false)
                    adapter?.indicateLoadMoreError()
                    Toaster.sendShortToast(R.string.failed_to_send_request)
                }
            })
        }

        sharedViewModel.apply {
            onRequestRefresh.observe(viewLifecycleOwner, { e ->
                e?.consume {
                    if (it == type) {
                        viewModel.refresh()
                    }
                }
            })
            onRequestScrollToTop.observe(viewLifecycleOwner, { e ->
                e?.consume {
                    if (it == type) {
                        scrollToTop()
                    }
                }
            })
        }

        adapter = ImageAdapter(view.context).apply {
            onClickQuickDownload = { image ->
                AppComponent.instance.analysisHelper.logClickDownloadInList()
                download(image)
            }

            onClickPhoto = { data ->
                sharedViewModel.onClickedImage.value = data.liveDataEvent
            }

            viewModel.images.value?.let {
                refresh(it)
            }
        }

        contentRecyclerView.itemAnimator = null

        contentRecyclerView.adapter = adapter
        contentRecyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)

        LoadMoreListener {
            viewModel.loadMore()
        }.apply {
            attach(contentRecyclerView)
        }

        refreshLayout.setOnRefreshListener {
            AppComponent.instance.analysisHelper.logRefreshList()
            viewModel.refresh()
        }

        retryBtn.setOnClickListener {
            updateNoItemVisibility(false)
            viewModel.refresh()
        }
    }

    private fun scrollToTop() {
        val lm = contentRecyclerView.layoutManager as LinearLayoutManager
        val pos = lm.findFirstCompletelyVisibleItemPosition()
        if (pos > SCROLL_START_POSITION) {
            contentRecyclerView.scrollToPosition(SCROLL_START_POSITION)
        }
        contentRecyclerView.smoothScrollToPosition(0)
    }

    private fun download(image: UnsplashImage) {
        val context = context ?: return

        if (!PermissionUtils.check(context as Activity)) {
            Toaster.sendShortToast(context.getString(R.string.no_permission))
            return
        }

        val warn = LocalSettingHelper.getBoolean(context,
                context.getString(R.string.preference_key_download_via_metered_network), true)

        if (warn && !context.usingWifi()) {
            val builder = buildMeteredWarningDialog(context) {
                DownloadUtils.download(context, image)
            }
            builder.create().show()
        } else {
            DownloadUtils.download(context, image)
        }
    }

    private fun updateNoItemVisibility(show: Boolean) {
        noItemLayout.setVisible(show)
    }
}
