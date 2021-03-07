package com.juniperphoton.myersplash.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.juniperphoton.myersplash.R

object ThemeHelper {
    @Suppress("MemberVisibilityCanBePrivate")
    fun getCurrentTheme(context: Context): Int {
        return when (LocalSettingHelper.getInt(
            context,
            context.getString(R.string.preference_key_theme),
            LocalSettingHelper.DEFAULT_THEME
        )) {
            0 -> AppCompatDelegate.MODE_NIGHT_YES
            1 -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
    }

    fun switchTheme(context: Context) {
        AppCompatDelegate.setDefaultNightMode(getCurrentTheme(context))
    }

    fun isDark(): Boolean {
        return AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
    }
}