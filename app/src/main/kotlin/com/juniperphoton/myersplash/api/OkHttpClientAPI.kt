package com.juniperphoton.myersplash.api

import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.di.hilt.GlobalEntryPoint
import dagger.hilt.android.EntryPointAccessors
import okhttp3.OkHttpClient

object OkHttpClientAPI {
    private val entryPoint = EntryPointAccessors.fromApplication(
        App.instance,
        GlobalEntryPoint::class.java
    )

    fun createClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addNetworkInterceptor { chain ->
                val startMs = System.currentTimeMillis()
                val request = chain.request()

                try {
                    val response = chain.proceed(request)
                    val durationMs = System.currentTimeMillis() - startMs
                    entryPoint.analysisHelper().logDownloadSuccess(durationMs)
                    return@addNetworkInterceptor response
                } catch (e: Exception) {
                    val durationMs = System.currentTimeMillis() - startMs
                    entryPoint.analysisHelper().logDownloadFailed(e, durationMs)
                    throw e
                }
            }
        return builder.build()
    }
}