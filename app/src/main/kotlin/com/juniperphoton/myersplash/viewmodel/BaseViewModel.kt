package com.juniperphoton.myersplash.viewmodel

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.di.AppComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

abstract class BaseViewModel(application: Application
) : AndroidViewModel(application), CoroutineScope by MainScope() {
    protected val app = getApplication<App>()

    fun getString(@StringRes stringRes: Int): String {
        return getApplication<Application>().getString(stringRes)
    }

    fun getString(@StringRes stringRes: Int, vararg params: Any): String {
        return getApplication<Application>().getString(stringRes, *params)
    }

    override fun onCleared() {
        cancel()
        super.onCleared()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : BaseViewModel> inject(): T {
        when (this) {
            is DownloadListViewModel -> {
                AppComponent.instance.inject(this)
            }
            is ImageDetailViewModel -> {
                AppComponent.instance.inject(this)
            }
        }

        return this as T
    }
}