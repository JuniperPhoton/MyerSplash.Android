package com.juniperphoton.myersplash.utils

import android.net.Uri
import android.os.Environment
import com.facebook.binaryresource.FileBinaryResource
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory
import com.facebook.imagepipeline.core.ImagePipelineFactory
import com.facebook.imagepipeline.request.ImageRequest
import com.juniperphoton.myersplash.App
import java.io.File

object FileUtil {
    val galleryPath: String?
        get() {
            val filesDir = App.instance.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val dir = File(filesDir, "MyerSplash")

            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    return null
                }
            }

            return dir.absolutePath
        }

    val cachedPath: String?
        get() = App.instance.cacheDir.absolutePath

    fun getCachedFile(url: String): File? {
        val cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(
                ImageRequest.fromUri(Uri.parse(url)), null)

        var localFile: File? = null

        if (cacheKey != null) {
            if (ImagePipelineFactory.getInstance().mainFileCache.hasKey(cacheKey)) {
                val resource = ImagePipelineFactory.getInstance().mainFileCache.getResource(cacheKey)
                localFile = (resource as FileBinaryResource).file
            }
        }

        return localFile
    }
}