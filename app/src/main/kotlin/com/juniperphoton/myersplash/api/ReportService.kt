package com.juniperphoton.myersplash.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface ReportService {
    @Streaming
    @GET
    suspend fun downloadFileAsync(@Url fileUrl: String): ResponseBody

    @GET
    suspend fun reportDownloadAsync(@Url url: String): ResponseBody
}