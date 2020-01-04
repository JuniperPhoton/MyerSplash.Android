package com.juniperphoton.myersplash.model

import androidx.core.content.ContextCompat
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.api.Request
import com.juniperphoton.myersplash.extension.toHexString
import java.text.SimpleDateFormat
import java.util.*

object UnsplashImageFactory {
    val TODAY_THUMB_URL: String
        get() = createThumbDownloadUrl(todayDate)

    val TODAY_DOWNLOAD_URL: String
        get() = createFullDownloadUrl(todayDate)

    val TODAY_DATE_STRING: String
        get() {
            return createDateString(todayDate)
        }

    val TODAY_STRING_FOR_DISPLAY: String
        get() {
            return SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(todayDate)
        }

    private val dateFormat = SimpleDateFormat("dd", Locale.ENGLISH)

    private val todayDate: Date
        get() = Calendar.getInstance(TimeZone.getDefault()).time

    private val fallbackColor0: String by lazy {
        ContextCompat.getColor(App.instance, R.color.highlightFallbackColor0).toHexString()
    }

    private val fallbackColor1: String by lazy {
        ContextCompat.getColor(App.instance, R.color.highlightFallbackColor1).toHexString()
    }

    fun createDateString(date: Date): String {
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(date)
    }

    private fun createFullDownloadUrl(date: Date): String {
        return "${Request.AUTO_CHANGE_WALLPAPER}${createDateString(date)}.jpg"
    }

    private fun createThumbDownloadUrl(date: Date): String {
        return "${Request.AUTO_CHANGE_WALLPAPER_THUMB}${createDateString(date)}.jpg"
    }

    fun createHighlightImage(date: Date, showTodayTag: Boolean = false): UnsplashImage {
        return UnsplashImage().apply {
            isUnsplash = false
            this.showTodayTag = showTodayTag
            color = if (dateFormat.format(date).toInt() % 2 == 0) fallbackColor0 else fallbackColor1
            id = createDateString(date)
            urls = UnsplashImageUrl().apply {
                val fullUrl = createFullDownloadUrl(date)
                val thumbUrl = createThumbDownloadUrl(date)
                raw = fullUrl
                full = fullUrl
                regular = thumbUrl
                small = thumbUrl
                thumb = thumbUrl
                width = 3
                height = 2
            }
            user = UnsplashUser().apply {
                val authorName = App.instance.getString(R.string.author_default_name)
                userName = authorName
                name = authorName
                links = UnsplashProfileUrl().apply {
                    html = Request.ME_HOME_PAGE
                }
            }
        }
    }

    fun createTodayImage(): UnsplashImage {
        return createHighlightImage(todayDate, true)
    }
}