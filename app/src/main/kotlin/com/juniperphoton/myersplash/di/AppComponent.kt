package com.juniperphoton.myersplash.di

import com.juniperphoton.myersplash.api.IOService
import com.juniperphoton.myersplash.broadcastreceiver.WallpaperWidgetProvider
import com.juniperphoton.myersplash.service.DownloadService
import com.juniperphoton.myersplash.utils.DownloadReporter
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

    abstract val reporter: DownloadReporter
    abstract val ioService: IOService

    abstract fun inject(vm: DownloadListViewModel)
    abstract fun inject(vm: ImageDetailViewModel)
    abstract fun inject(service: DownloadService)
    abstract fun inject(receiver: WallpaperWidgetProvider)
}