package com.juniperphoton.myersplash.di

import com.juniperphoton.myersplash.api.PhotoService
import com.juniperphoton.myersplash.model.UnsplashCategory
import com.juniperphoton.myersplash.repo.*
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Named

@Module(includes = [AppModule::class])
@DisableInstallInCheck
class ImageModule {
    @ImageScope
    @Provides
    fun provideImageRepo(service: PhotoService, @Named("type") type: Int): ImageRepo {
        return when (type) {
            UnsplashCategory.NEW_CATEGORY_ID -> {
                NewImageRepo(service)
            }
            UnsplashCategory.DEVELOPER_ID -> {
                DeveloperImageRepo(service)
            }
            UnsplashCategory.HIGHLIGHTS_CATEGORY_ID -> {
                HighlightImageRepo()
            }
            UnsplashCategory.SEARCH_ID -> {
                SearchImageRepo(service)
            }
            UnsplashCategory.RANDOM_CATEGORY_ID -> {
                RandomImageRepo(service)
            }
            else -> {
                throw IllegalArgumentException("Unknown type of $type")
            }
        }
    }
}