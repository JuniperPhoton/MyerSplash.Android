package com.juniperphoton.myersplash.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.adapter.CategoryAdapter
import com.juniperphoton.myersplash.fragment.ImageListFragment
import com.juniperphoton.myersplash.model.UnsplashCategory
import com.juniperphoton.myersplash.utils.Toaster
import kotlin.math.abs

@Suppress("unused")
class SearchView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    companion object {
        private const val TAG = "SearchView"
    }

    @BindView(R.id.detail_search_et)
    lateinit var editText: EditText

    @BindView(R.id.detail_search_root_rl)
    lateinit var rootRL: ViewGroup

    @BindView(R.id.search_result_root)
    lateinit var resultRoot: FrameLayout

    @BindView(R.id.search_detail_view)
    lateinit var detailView: ImageDetailView

    @BindView(R.id.detail_search_btn)
    lateinit var searchBtn: View

    @BindView(R.id.detail_clear_btn)
    lateinit var clearBtn: View

    @BindView(R.id.search_tag)
    lateinit var tagView: TextView

    @BindView(R.id.search_toolbar_layout)
    lateinit var appBarLayout: AppBarLayout

    @BindView(R.id.search_box)
    lateinit var searchBox: View

    @BindView(R.id.category_list)
    lateinit var categoryList: RecyclerView

    private var categoryAdapter: CategoryAdapter? = null

    private var mainListFragment: ImageListFragment? = null
    private var animating: Boolean = false

    init {
        LayoutInflater.from(context).inflate(R.layout.search_layout, this)
        ButterKnife.bind(this)
        initUi()
    }

    private fun initUi() {
        rootRL.setOnTouchListener { _, _ -> true }
        editText.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                onClickSearch()
                return@setOnKeyListener true
            }
            false
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable) {
                if (editText.text != null && editText.text.toString() != "") {
                    if (searchBtn.scaleX != 1f) {
                        toggleSearchButtons(show = true, animation = true)
                    }
                } else {
                    if (searchBtn.scaleX != 0f) {
                        // Ignore
                    }
                }
            }
        })

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val fraction = abs(verticalOffset) * 1.0f / appBarLayout.height
            tagView.alpha = fraction
        })

        initCategoryList()
    }

    @SuppressLint("DefaultLocale")
    private fun constructFragment() {
        val activity = context as? AppCompatActivity ?: return

        mainListFragment = ImageListFragment.build(UnsplashCategory.SEARCH_ID,
                editText.text.toString().toLowerCase())

        activity.supportFragmentManager
                .beginTransaction()
                .replace(R.id.search_result_root, mainListFragment!!)
                .commitAllowingStateLoss()
    }

    private fun destroyFragment() {
        val activity = context as? AppCompatActivity ?: return
        val fragment = mainListFragment ?: return

        activity.supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commitAllowingStateLoss()

        mainListFragment = null
    }

    private fun initCategoryList() {
        categoryAdapter = CategoryAdapter(context, CategoryAdapter.KEYWORDS)
        categoryAdapter!!.onClickItem = { name ->
            editText.setText(name, TextView.BufferType.EDITABLE)
            editText.setSelection(name.length, name.length)
            onClickSearch()
        }
        categoryList.layoutManager = FlexboxLayoutManager(context)
        categoryList.adapter = categoryAdapter
    }

    private fun toggleSearchButtons(show: Boolean, animation: Boolean) {
        if (!animation) {
            searchBtn.scaleX = if (show) 1f else 0f
            searchBtn.scaleY = if (show) 1f else 0f
            clearBtn.scaleX = if (show) 1f else 0f
            clearBtn.scaleY = if (show) 1f else 0f
        } else {
            if (animating) return
            animating = true
            searchBtn.animate().scaleX(if (show) 1f else 0f).scaleY(if (show) 1f else 0f).setDuration(200)
                    .setStartDelay(100)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(a: Animator) {
                            animating = false
                        }
                    })
                    .start()
            clearBtn.animate().scaleX(if (show) 1f else 0f).scaleY(if (show) 1f else 0f).setDuration(200)
                    .start()
        }

        searchBtn.visibility = if (show) View.VISIBLE else View.GONE
        clearBtn.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    fun onShowing() {
        toggleSearchButtons(false, animation = false)
    }

    fun onHiding() {
        hideKeyboard()
        toggleSearchButtons(false, animation = false)
        tagView.animate().alpha(0f).setDuration(100).start()
    }

    fun onShown() {
        val layoutParams = searchBox.layoutParams as AppBarLayout.LayoutParams
        layoutParams.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        searchBox.layoutParams = layoutParams
    }

    fun reset() {
        destroyFragment()

        val layoutParams = searchBox.layoutParams as AppBarLayout.LayoutParams
        layoutParams.scrollFlags = 0
        searchBox.layoutParams = layoutParams
        editText.setText("")

        categoryList.animate()?.alpha(1f)?.setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(a: Animator?) {
                categoryList.visibility = View.VISIBLE
            }
        })?.start()

        resultRoot.visibility = View.GONE
    }

    fun tryShowKeyboard() {
        editText.post {
            editText.requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, SHOW_IMPLICIT)
        }
    }

    @OnClick(R.id.detail_search_btn)
    fun onClickSearch() {
        hideKeyboard()
        if (editText.text.toString().isEmpty()) {
            Toaster.sendShortToast(R.string.search_empty_hint)
            return
        }
        resultRoot.visibility = View.VISIBLE
        tagView.text = "# ${editText.text.toString().toUpperCase()}"

        constructFragment()

        categoryList.animate().alpha(0f).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(a: Animator?) {
                categoryList.visibility = View.GONE
            }
        })?.start()
    }

    @OnClick(R.id.detail_clear_btn)
    fun onClickClear() {
        editText.setText("")
        toggleSearchButtons(show = false, animation = true)
    }

    fun tryHide(): Boolean = detailView.tryHide()
}
