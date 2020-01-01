package com.juniperphoton.myersplash.di

import com.juniperphoton.myersplash.api.CloudService
import com.juniperphoton.myersplash.api.PhotoService
import com.juniperphoton.myersplash.api.ReportService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {
    @Provides
    fun provideImageService(): PhotoService {
        return CloudService.createService()
    }

    @Singleton
    @Provides
    fun provideIOService(): ReportService {
        return CloudService.createService()
    }
}