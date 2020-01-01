package com.juniperphoton.myersplash.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.juniperphoton.myersplash.R

object ThemeHelper {
    fun currentTheme(context: Context): Int {
        return when (LocalSettingHelper.getInt(context, context.getString(R.string.preference_key_theme), 2)) {
            0 -> AppCompatDelegate.MODE_NIGHT_YES
            1 -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
    }

    fun switchTheme(context: Context) {
        AppCompatDelegate.setDefaultNightMode(currentTheme(context))
    }

    fun isDark(): Boolean {
        return AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
    }
}