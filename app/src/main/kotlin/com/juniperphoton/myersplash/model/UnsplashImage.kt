package com.juniperphoton.myersplash.model

import android.app.Activity
import android.content.Context
import android.graphics.Color
import com.google.gson.annotations.SerializedName
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.utils.LocalSettingHelper

@Suppress("unused")
class UnsplashImage {
    companion object {
        private val savingQualitySettingsKey = App.instance.getString(R.string.preference_key_download_quality)
        private val listQualitySettingsKey = App.instance.getString(R.string.preference_key_browsing_quality)
    }

    @SerializedName("id")
    var id: String? = null

    @SerializedName("created_at")
    private val createdAt: String? = null

    @SerializedName("color")
    var color: String? = null

    @SerializedName("likes")
    private val likes: Int = 0

    @SerializedName("user")
    var user: UnsplashUser? = null

    @SerializedName("urls")
    var urls: UnsplashImageUrl? = null

    @SerializedName("links")
    private val links: UnsplashImageLinks? = null

    @SerializedName("width")
    var width = 0

    @SerializedName("height")
    var height = 0

    @SerializedName("sponsorship")
    var sponsorship: UnsplashSponsorship? = null

    val downloadLocationLink: String?
        get() = links?.downloadLocation

    var isUnsplash: Boolean = true

    var showTodayTag: Boolean = false

    val fileNameForDownload: String
        get() = "${user!!.name} - $id - $tagForDownloadUrl"

    val themeColor: Int
        get() = try {
            Color.parseColor(color)
        } catch (e: Exception) {
            Color.BLACK
        }

    val userName: String?
        get() = user?.name

    val userHomePage: String?
        get() = user?.homeUrl

    val listUrl: String?
        get() {
            val urls = urls ?: return null
            return when (LocalSettingHelper.getInt(App.instance, listQualitySettingsKey, 0)) {
                0 -> urls.regular
                1 -> urls.small
                2 -> urls.thumb
                else -> null
            }
        }

    val downloadUrl: String?
        get() {
            val urls = urls ?: return null
            return when (LocalSettingHelper.getInt(App.instance, savingQualitySettingsKey, 1)) {
                0 -> urls.raw
                1 -> urls.full
                2 -> urls.small
                else -> null
            }
        }

    private val tagForDownloadUrl: String
        get() {
            return when (LocalSettingHelper.getInt(App.instance, savingQualitySettingsKey, 1)) {
                0 -> "raw"
                1 -> "regular"
                2 -> "small"
                else -> ""
            }
        }
}

fun UnsplashImage.getDisplayRatioF(context: Context): Float {
    val ratioStr = getDisplayRatio(context)
    val array = ratioStr.split(":")
    val w = array.getOrNull(0)?.toFloatOrNull() ?: 3f
    val h = array.getOrNull(1)?.toFloatOrNull() ?: 2f
    return w / h
}

@Suppress("UnnecessaryVariable")
fun UnsplashImage.getDisplayRatio(context: Context): String {
    if (context is Activity) {
        val fixedInfoHeight = context.resources
                .getDimensionPixelSize(R.dimen.img_detail_info_height)

        val fixedMargin = context.resources
                .getDimensionPixelSize(R.dimen.detail_fixed_top_bottom_margin)

        val decorViewWidth = context.window.decorView.width
        val decorViewHeight = context.window.decorView.height

        val availableHeight = decorViewHeight - fixedMargin * 2

        val imageRatio = this.width / this.height.toFloat()
        val wantedWidth = decorViewWidth
        val wantedHeight = (wantedWidth / imageRatio) + fixedInfoHeight

        val targetWidth = wantedWidth
        var targetHeight = wantedHeight
        if (wantedHeight > availableHeight) {
            targetHeight = availableHeight.toFloat() - fixedInfoHeight
        } else {
            targetHeight -= fixedInfoHeight
        }

        return "$targetWidth:$targetHeight"
    } else {
        return "3:2"
    }
}

class UnsplashImageLinks {
    @SerializedName("download_location")
    var downloadLocation: String? = null
}

class UnsplashImageUrl {
    @SerializedName("raw")
    var raw: String? = null

    @SerializedName("full")
    var full: String? = null

    @SerializedName("regular")
    var regular: String? = null

    @SerializedName("small")
    var small: String? = null

    @SerializedName("thumb")
    var thumb: String? = null
}

class UnsplashSponsorship