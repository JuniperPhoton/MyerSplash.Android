package com.juniperphoton.myersplash.model

import com.google.gson.annotations.SerializedName

class UnsplashSearchResult {
    @SerializedName("results")
    val list: MutableList<UnsplashImage>? = null
}
