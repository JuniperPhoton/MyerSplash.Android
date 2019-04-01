package com.juniperphoton.myersplash.extension

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import androidx.annotation.Nullable
import java.io.File

/**
 * @author JuniperPhoton @ Zhihu Inc.
 * @since 2019-04-02
 */
object MediaStoreExtensions {
    @Nullable
    fun insertImage(cr: ContentResolver, filePath: String, title: String?, desc: String?): Uri? {
        val file = File(filePath)
        if (!file.exists()) {
            return null
        }

        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, title)
        values.put(MediaStore.Images.Media.DESCRIPTION, desc)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg") //todo extract mime types
        values.put(MediaStore.MediaColumns.DATA, file.absolutePath) //todo transfer stream

        return cr.insert(EXTERNAL_CONTENT_URI, values)
    }
}