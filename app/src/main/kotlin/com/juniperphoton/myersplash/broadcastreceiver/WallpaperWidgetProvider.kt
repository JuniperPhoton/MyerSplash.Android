package com.juniperphoton.myersplash.broadcastreceiver

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.RemoteViews
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.activity.MainActivity
import com.juniperphoton.myersplash.cloudservice.CloudService
import com.juniperphoton.myersplash.extension.getLengthInKb
import com.juniperphoton.myersplash.model.UnsplashImageFactory
import com.juniperphoton.myersplash.service.DownloadService
import com.juniperphoton.myersplash.utils.*
import okhttp3.ResponseBody
import java.io.File

class WallpaperWidgetProvider : AppWidgetProvider() {
    companion object {
        private const val TAG = "WallpaperWidgetProvider"
    }

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        if (appWidgetIds == null || context == null) {
            return
        }
        Pasteur.debug(TAG, "onUpdate")

        val file = File(FileUtil.cachedPath, "${UnsplashImageFactory.TODAY_THUMB_URL.hashCode()}.jpg")
        if (file.exists() && file.getLengthInKb() > 100) {
            AppWidgetUtil.doWithWidgetId {
                updateWidget(App.instance, it, file.absolutePath)
            }
            return
        }

        val observer = object : ResponseObserver<ResponseBody>() {
            var outputFile: File? = null

            override fun onUnknownError(e: Throwable) {
                e.printStackTrace()
            }

            override fun onComplete() {
                outputFile?.let {
                    AppWidgetUtil.doWithWidgetId {
                        updateWidget(App.instance, it, outputFile!!.absolutePath)
                    }
                }
            }

            override fun onNext(data: ResponseBody) {
                outputFile = DownloadUtil.writeToFile(data, file.path, null)
            }
        }

        CloudService.downloadPhoto(UnsplashImageFactory.TODAY_THUMB_URL).subscribeWith(observer)
    }

    private fun updateWidget(context: Context, widgetId: Int, filePath: String) {
        val manager = AppWidgetManager.getInstance(context)
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_layout)
        remoteViews.setTextViewText(R.id.widget_update_time_text, UnsplashImageFactory.TODAY_STRING_FOR_DISPLAY)
        val bm = BitmapFactory.decodeFile(filePath)
        remoteViews.setImageViewBitmap(R.id.widget_center_image, bm)

        Log.d(TAG, "pending to download: $UnsplashImageFactory.DOWNLOAD_URL")

        val intent = Intent(context, DownloadService::class.java)
        intent.putExtra(Params.URL_KEY, UnsplashImageFactory.TODAY_DOWNLOAD_URL)
        intent.putExtra(Params.NAME_KEY, UnsplashImageFactory.TODAY_DATE_STRING)
        intent.putExtra(Params.PREVIEW_URI, filePath)
        intent.putExtra(Params.IS_UNSPLASH_WALLPAPER, false)
        val pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.widget_download_btn, pendingIntent)

        val mainIntent = Intent(context, MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.widget_center_image, mainPendingIntent)

        manager.updateAppWidget(widgetId, remoteViews)
    }
}