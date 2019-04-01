package com.juniperphoton.myersplash.utils

import android.app.WallpaperManager
import android.content.Intent
import android.net.Uri
import com.juniperphoton.myersplash.App

object IntentUtil {
    fun getSetAsWallpaperIntent(uri: Uri): Intent {
        Pasteur.info("IntentUtil", "getSetAsWallpaperIntent: $uri")
        val intent = WallpaperManager.getInstance(App.instance).getCropAndSetWallpaperIntent(uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }
}