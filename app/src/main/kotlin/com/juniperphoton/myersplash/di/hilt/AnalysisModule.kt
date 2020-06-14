package com.juniperphoton.myersplash.di.hilt

import com.juniperphoton.myersplash.utils.AnalysisHelper
import com.juniperphoton.myersplash.utils.AnalysisHelperImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class AnalysisModule {
    @Singleton
    @Provides
    fun provideAnalysisHelper(): AnalysisHelper {
        return AnalysisHelperImpl()
    }
}