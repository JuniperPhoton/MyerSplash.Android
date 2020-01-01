package com.juniperphoton.myersplash.di

import com.juniperphoton.myersplash.viewmodel.DownloadListViewModel
import com.juniperphoton.myersplash.viewmodel.ImageDetailViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AppModule::class,
    DownloadsModule::class
])
abstract class AppComponent {
    companion object {
        val instance: AppComponent by lazy {
            return@lazy DaggerAppComponent.builder().build()
        }
    }

    abstract fun inject(vm: DownloadListViewModel)
    abstract fun inject(vm: ImageDetailViewModel)
}