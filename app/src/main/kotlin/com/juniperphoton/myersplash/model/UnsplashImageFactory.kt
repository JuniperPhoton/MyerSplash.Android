package com.juniperphoton.myersplash.model

import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.cloudservice.Request
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

    fun createDateString(date: Date): String {
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(date)
    }

    fun createFullDownloadUrl(date: Date): String {
        return "${Request.AUTO_CHANGE_WALLPAPER}${createDateString(date)}.jpg"
    }

    fun createThumbDownloadUrl(date: Date): String {
        return "${Request.AUTO_CHANGE_WALLPAPER_THUMB}${createDateString(date)}.jpg"
    }

    val TODAY_STRING_FOR_DISPLAY: String
        get() {
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(todayDate)
        }

    private val dateFormat = SimpleDateFormat("dd", Locale.getDefault())

    private val todayDate: Date
        get() = Calendar.getInstance(TimeZone.getDefault()).time

    fun createHighlightImage(date: Date): UnsplashImage {
        return UnsplashImage().apply {
            isUnsplash = false
            showTodayTag = date == todayDate
            color = if (dateFormat.format(date).toInt() % 2 == 0) "#50ffffff" else "#000000"
            id = createDateString(date)
            urls = ImageUrl().apply {
                val fullUrl = createFullDownloadUrl(date)
                val thumbUrl = createThumbDownloadUrl(date)
                raw = fullUrl
                full = fullUrl
                regular = thumbUrl
                small = thumbUrl
                thumb = thumbUrl
            }
            user = UnsplashUser().apply {
                val authorName = App.instance.getString(R.string.author_default_name)
                userName = authorName
                name = authorName
                links = ProfileUrl().apply {
                    html = Request.ME_HOME_PAGE
                }
            }
        }
    }

    fun createTodayImage(): UnsplashImage {
        return createHighlightImage(todayDate)
    }
}