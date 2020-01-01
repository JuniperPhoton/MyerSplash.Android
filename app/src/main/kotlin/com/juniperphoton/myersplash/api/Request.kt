package com.juniperphoton.myersplash.api

object Request {
    const val MY_DOMAIN = "juniperphoton.dev"

    const val BASE_URL = "https://api.unsplash.com/"
    const val PHOTO_URL = "https://api.unsplash.com/photos?"
    const val DEVELOPER_PHOTO_URL = "https://api.unsplash.com/users/juniperphoton/photos?"
    const val HIGHLIGHTS_PHOTO_URL = "http://$MY_DOMAIN/myersplash/thumbs"
    const val RANDOM_PHOTOS_URL = "https://api.unsplash.com/photos/random?"
    const val SEARCH_URL = "https://api.unsplash.com/search/photos?"

    const val AUTO_CHANGE_WALLPAPER = "https://$MY_DOMAIN/myersplash/wallpapers/"
    const val AUTO_CHANGE_WALLPAPER_THUMB = "https://$MY_DOMAIN/myersplash/wallpapers/thumbs/"

    const val ME_HOME_PAGE = "https://unsplash.com/@juniperphoton"
}