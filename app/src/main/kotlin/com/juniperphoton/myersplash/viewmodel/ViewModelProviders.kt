package com.juniperphoton.myersplash.viewmodel

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.juniperphoton.myersplash.App

object AppViewModelProviders {
    fun of(fragment: Fragment): ViewModelProvider {
        return ViewModelProvider(fragment, VMFactory.instance)
    }

    fun of(activity: FragmentActivity): ViewModelProvider {
        return ViewModelProvider(activity, VMFactory.instance)
    }
}

private class VMFactory(
    application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    companion object {
        val instance: VMFactory by lazy {
            VMFactory(App.instance)
        }
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val vm = super.create(modelClass)
        if (vm is BaseViewModel) {
            vm.inject<BaseViewModel>()
        }
        return vm
    }
}