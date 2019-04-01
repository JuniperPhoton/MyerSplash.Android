package com.juniperphoton.myersplash.extension

import android.content.Context
import com.juniperphoton.myersplash.utils.Pasteur
import java.io.File

fun File.getFolderLengthInMb(): Long {
    if (!exists()) return 0
    return listFiles().map(File::length).sum() / 1024 / 1024
}

fun File.getLengthInKB(): Long = length() / 1024 / 1024

fun File.notifyFileUpdated(ctx: Context) {
    insertToMedia(ctx, this)
}

private fun insertToMedia(context: Context, file: File) {
    val uri = MediaStoreExtensions.insertImage(context.contentResolver, file.path, file.name, "")
    Pasteur.info("insertToMedia", "insertToMedia success: $uri, original file: $file")
}