package com.juniperphoton.myersplash.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.repo.ImageRepo
import com.juniperphoton.myersplash.repo.SearchImageRepo
import com.juniperphoton.myersplash.utils.Pasteur
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

open class ImageListViewModel(application: Application
) : BaseViewModel(application) {
    companion object {
        private const val TAG = "ImageListViewModel"
    }

    @Inject
    lateinit var repo: ImageRepo

    private val _showError = MutableLiveData<Boolean>()
    val showError: LiveData<Boolean>
        get() = _showError

    private val _showLoadingMoreError = MutableLiveData<Boolean>()
    val showLoadingMoreError: LiveData<Boolean>
        get() = _showLoadingMoreError

    private val _refreshing = MutableLiveData<Boolean>()
    val refreshing: LiveData<Boolean>
        get() = _refreshing

    private var loadingMore = false

    val images: LiveData<List<UnsplashImage>>
        get() = repo.images

    fun refresh() = launch {
        if (_refreshing.value == true || loadingMore) {
            return@launch
        }

        try {
            _refreshing.value = true
            repo.refresh()
        } catch (e: Exception) {
            _showError.value = true
        } finally {
            _refreshing.value = false
        }
    }

    fun loadMore() = launch {
        if (_refreshing.value == true || loadingMore
                || images.value == null || images.value?.isEmpty() == true) {
            return@launch
        }

        try {
            loadingMore = true
            repo.loadMore()
        } catch (e: Exception) {
            Pasteur.w(TAG) {
                "error on loading more: $e"
            }
            _showLoadingMoreError.value = true
        } finally {
            loadingMore = false
        }
    }

    override fun onCleared() {
        cancel()
        super.onCleared()
    }
}

class SearchImageViewModel(application: Application) : ImageListViewModel(application) {
    var searchKeyword: String? = null
        set(value) {
            field = value
            if (repo is SearchImageRepo) {
                val searchImageRepo = repo as SearchImageRepo
                searchImageRepo.keyword = value
            }
        }
}