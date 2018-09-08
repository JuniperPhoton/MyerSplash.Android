package com.juniperphoton.myersplash.cloudservice

import android.annotation.SuppressLint
import android.util.Log
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.model.UnsplashImageFactory
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Suppress("DEPRECATION")
object CloudService {
    private const val DEFAULT_TIMEOUT = 10
    private const val DEFAULT_REQUEST_COUNT = 10
    private const val DEFAULT_HIGHLIGHTS_COUNT = 60

    private const val TAG = "CloudService"

    private val endDate = SimpleDateFormat("yyyy/MM/dd").parse("2018/07/20")

    private val retrofit: Retrofit
    private val photoService: PhotoService
    private val downloadService: DownloadService
    private val builder: OkHttpClient.Builder = OkHttpClient.Builder()

    private fun networkTransformer(): ObservableTransformer<MutableList<UnsplashImage>, MutableList<UnsplashImage>> {
        return ObservableTransformer {
            return@ObservableTransformer it.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    init {
        val ctx = SSLContext.getInstance("SSL")

        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()

            @SuppressLint("TrustAllX509TrustManager")
            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit

            @SuppressLint("TrustAllX509TrustManager")
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
        })

        ctx.init(null, trustAllCerts, SecureRandom())

        builder.sslSocketFactory(ctx.socketFactory)

        builder.connectTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .addInterceptor(CustomInterceptor())

        retrofit = Retrofit.Builder()
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(Request.BASE_URL)
                .build()

        photoService = retrofit.create(PhotoService::class.java)
        downloadService = retrofit.create(DownloadService::class.java)
    }

    fun getPhotos(url: String,
                  page: Int): Observable<MutableList<UnsplashImage>> {
        return photoService.getPhotos(url, page, DEFAULT_REQUEST_COUNT)
                .compose(networkTransformer())
    }

    fun getRandomPhotos(url: String): Observable<MutableList<UnsplashImage>> {
        return photoService.getRandomPhotos(url, DEFAULT_REQUEST_COUNT)
                .compose(networkTransformer())
    }

    fun getFeaturedPhotos(url: String,
                          page: Int): Observable<MutableList<UnsplashImage>> {
        return photoService
                .getFeaturedPhotos(url, page, DEFAULT_REQUEST_COUNT)
                .map { images ->
                    images.map { it.image!! }.toMutableList()
                }
                .compose(networkTransformer())
    }

    fun getHighlightsPhotos(page: Int): Observable<MutableList<UnsplashImage>> {
        return Observable.create<MutableList<UnsplashImage>> {
            val calendar = Calendar.getInstance(TimeZone.getDefault())
            calendar.add(Calendar.DATE, -(page - 1) * DEFAULT_HIGHLIGHTS_COUNT)

            val list = mutableListOf<UnsplashImage>()

            for (i in 0 until DEFAULT_HIGHLIGHTS_COUNT) {
                calendar.add(Calendar.DATE, -1)
                val date = calendar.time
                if (date > endDate) {
                    list.add(UnsplashImageFactory.createHighlightImage(calendar.time))
                } else {
                    Log.w(TAG, "the date: $date is before end date $endDate")
                }
            }

            it.onNext(list)
            it.onComplete()
        }.delay(200, TimeUnit.MILLISECONDS).compose(networkTransformer())
    }

    fun searchPhotos(url: String,
                     page: Int,
                     query: String): Observable<MutableList<UnsplashImage>> {
        return photoService
                .searchPhotosByQuery(url, page, DEFAULT_REQUEST_COUNT, query)
                .map { searchResults ->
                    searchResults.list!!
                }
                .compose(networkTransformer())
    }

    fun downloadPhoto(url: String): Observable<ResponseBody> {
        return downloadService
                .downloadFileWithDynamicUrlSync(url).timeout(30, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
    }

    fun reportDownload(url: String): Observable<ResponseBody> {
        return downloadService.reportDownload(url)
                .subscribeOn(Schedulers.io())
    }
}
