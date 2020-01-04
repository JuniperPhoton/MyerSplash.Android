package com.juniperphoton.myersplash.api

import com.juniperphoton.myersplash.api.CloudService.DEFAULT_REQUEST_COUNT
import com.juniperphoton.myersplash.model.UnsplashSearchResult
import com.juniperphoton.myersplash.model.UnsplashImage
import retrofit2.http.GET
import retrofit2.http.Query

const val TAG = "PhotoService"

interface PhotoService {
    @GET("/photos")
    suspend fun getNewPhotos(@Query("page") page: Int,
                             @Query("per_page") per_page: Int = DEFAULT_REQUEST_COUNT
    ): MutableList<UnsplashImage>

    @GET("/users/juniperphoton/photos")
    suspend fun getDeveloperPhotos(@Query("page") page: Int,
                                   @Query("per_page") per_page: Int = DEFAULT_REQUEST_COUNT
    ): MutableList<UnsplashImage>

    @GET("/photos/random")
    suspend fun getRandomPhotos(@Query("per_page") per_page: Int = DEFAULT_REQUEST_COUNT,
                                @Query("count") count: Int = 30
    ): MutableList<UnsplashImage>

    @GET("/search/photos")
    suspend fun searchPhotosByQuery(
            @Query("query") query: String,
            @Query("page") page: Int,
            @Query("per_page") per_page: Int = DEFAULT_REQUEST_COUNT
    ): UnsplashSearchResult
}
