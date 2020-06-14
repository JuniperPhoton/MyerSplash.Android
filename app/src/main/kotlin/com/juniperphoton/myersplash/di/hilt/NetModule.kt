package com.juniperphoton.myersplash.di.hilt

import com.juniperphoton.myersplash.api.CloudService
import com.juniperphoton.myersplash.api.IOService
import com.juniperphoton.myersplash.api.PhotoService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class NetModule {
    @Singleton
    @Provides
    fun provideImageService(): PhotoService {
        return CloudService.createService()
    }

    @Singleton
    @Provides
    fun provideIOService(): IOService {
        return CloudService.createService()
    }
}