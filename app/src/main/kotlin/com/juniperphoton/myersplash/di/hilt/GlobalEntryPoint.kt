package com.juniperphoton.myersplash.di.hilt

import com.juniperphoton.myersplash.utils.AnalysisHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent

@EntryPoint
@InstallIn(ApplicationComponent::class)
interface GlobalEntryPoint {
    fun analysisHelper(): AnalysisHelper
}