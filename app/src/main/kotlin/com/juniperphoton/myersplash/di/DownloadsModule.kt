package com.juniperphoton.myersplash.di

import com.juniperphoton.myersplash.db.AppDatabase
import com.juniperphoton.myersplash.db.DownloadItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Singleton

@Module
@DisableInstallInCheck
class DownloadsModule {
    @Singleton
    @Provides
    fun providesDownloadsDao(): DownloadItemDao {
        return AppDatabase.instance.downloadItemDao()
    }
}