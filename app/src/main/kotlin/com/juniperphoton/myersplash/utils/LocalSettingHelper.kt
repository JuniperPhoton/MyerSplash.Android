package com.juniperphoton.myersplash.utils

import android.content.Context
import android.content.SharedPreferences
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R

@Suppress("unused")
object LocalSettingHelper {
    private const val CONFIG_NAME = "config"

    const val DEFAULT_THEME = 2
    const val DEFAULT_SAVING_QUALITY = 1
    const val DEFAULT_BROWSING_QUALITY = 0

    val KEY_THEME = App.instance.getString(R.string.preference_key_theme)
    val KEY_DOWNLOAD_QUALITY = App.instance.getString(R.string.preference_key_download_quality)
    val KEY_BROWSING_QUALITY = App.instance.getString(R.string.preference_key_browsing_quality)

    private fun getSharedPreference(context: Context): SharedPreferences {
        return context.getSharedPreferences(CONFIG_NAME, Context.MODE_PRIVATE)
    }

    fun getBoolean(context: Context, key: String, defValue: Boolean): Boolean {
        val sharedPreferences = getSharedPreference(context)
        return sharedPreferences.getBoolean(key, defValue)
    }

    fun getInt(context: Context, key: String, defaultValue: Int): Int {
        val sharedPreferences = getSharedPreference(context)
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun checkKey(context: Context, key: String): Boolean {
        val sharedPreferences = getSharedPreference(context)
        return sharedPreferences.contains(key)
    }

    fun getString(context: Context, key: String): String {
        val sharedPreferences = getSharedPreference(context)
        return sharedPreferences.getString(key, null) ?: ""
    }

    fun putString(context: Context, key: String, value: String): Boolean {
        val sharedPreference = getSharedPreference(context)
        val editor = sharedPreference.edit()
        editor.putString(key, value)
        return editor.commit()
    }

    fun putBoolean(context: Context, key: String, value: Boolean?): Boolean {
        val sharedPreference = getSharedPreference(context)
        val editor = sharedPreference.edit()
        editor.putBoolean(key, value!!)
        return editor.commit()
    }

    fun putInt(context: Context, key: String, value: Int): Boolean {
        val sharedPreference = getSharedPreference(context)
        val editor = sharedPreference.edit()
        editor.putInt(key, value)
        return editor.commit()
    }

    fun deleteKey(context: Context, key: String): Boolean {
        val sharedPreferences = getSharedPreference(context)
        val editor = sharedPreferences.edit()
        editor.remove(key)
        return editor.commit()
    }
}

