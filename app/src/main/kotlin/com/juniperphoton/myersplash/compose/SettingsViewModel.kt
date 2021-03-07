package com.juniperphoton.myersplash.compose

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.facebook.imagepipeline.core.ImagePipelineFactory
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.db.AppDatabase
import com.juniperphoton.myersplash.utils.ImageIO
import com.juniperphoton.myersplash.utils.LocalSettingHelper
import com.juniperphoton.myersplash.utils.Toaster
import com.juniperphoton.myersplash.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author dengweichao @ Zhihu Inc.
 * @since 2021-03-08
 */
class SettingsViewModel(application: Application) : BaseViewModel(application) {
    private val _cacheSizeText = MutableLiveData<String>()
    val cacheSizeText: LiveData<String>
        get() = _cacheSizeText

    private val _meteredNetworkWarning = MutableLiveData<Boolean>()
    val meteredNetworkWarning: LiveData<Boolean>
        get() = _meteredNetworkWarning

    private val _sponsorship = MutableLiveData<Boolean>()
    val sponsorship: LiveData<Boolean>
        get() = _sponsorship

    private val _theme = MutableLiveData<String>()
    val theme: LiveData<String>
        get() = _theme

    private val _downloadQuality = MutableLiveData<String>()
    val downloadQuality: LiveData<String>
        get() = _downloadQuality

    private val _browsingQuality = MutableLiveData<String>()
    val browsingQuality: LiveData<String>
        get() = _browsingQuality

    val themeStrings: Array<String> = arrayOf(
        getString(R.string.settings_theme_dark),
        getString(R.string.settings_theme_light),
        getString(R.string.settings_theme_system)
    )

    val downloadQualityStrings: Array<String> = arrayOf(
        getString(R.string.settings_download_highest),
        getString(R.string.settings_download_high),
        getString(R.string.settings_download_medium)
    )

    val browsingQualityStrings: Array<String> = arrayOf(
        getString(R.string.settings_browsing_large),
        getString(R.string.settings_browsing_small),
        getString(R.string.settings_browsing_thumb)
    )

    init {
        _meteredNetworkWarning.value = LocalSettingHelper.getBoolean(
            app,
            getString(R.string.preference_key_download_via_metered_network),
            true
        )

        _sponsorship.value = LocalSettingHelper.getBoolean(
            app,
            getString(R.string.preference_key_show_sponsorship),
            true
        )

        val theme = LocalSettingHelper.getInt(
            app,
            LocalSettingHelper.KEY_THEME,
            LocalSettingHelper.DEFAULT_THEME
        )

        val downloadQuality = LocalSettingHelper.getInt(
            app,
            LocalSettingHelper.KEY_DOWNLOAD_QUALITY,
            LocalSettingHelper.DEFAULT_SAVING_QUALITY
        )

        val browsingQuality = LocalSettingHelper.getInt(
            app,
            LocalSettingHelper.KEY_BROWSING_QUALITY,
            LocalSettingHelper.DEFAULT_BROWSING_QUALITY
        )

        _theme.value = themeStrings[theme]
        _downloadQuality.value = downloadQualityStrings[downloadQuality]
        _browsingQuality.value = browsingQualityStrings[browsingQuality]
    }

    fun setTheme(index: Int) {
        LocalSettingHelper.putInt(app, LocalSettingHelper.KEY_THEME, index)
        _theme.value = themeStrings[index]
    }

    fun setDownloadQuality(index: Int) {
        LocalSettingHelper.putInt(app, LocalSettingHelper.KEY_DOWNLOAD_QUALITY, index)
        _downloadQuality.value = downloadQualityStrings[index]
    }

    fun setBrowsingQuality(index: Int) {
        LocalSettingHelper.putInt(app, LocalSettingHelper.KEY_BROWSING_QUALITY, index)
        _browsingQuality.value = browsingQualityStrings[index]
    }

    fun toggleMeteredNetworkWarning() {
        setMeteredNetworkWarning(!(_meteredNetworkWarning.value ?: true))
    }

    fun setMeteredNetworkWarning(on: Boolean) {
        if (_meteredNetworkWarning.value == on) {
            return
        }
        LocalSettingHelper.putBoolean(
            app,
            getString(R.string.preference_key_download_via_metered_network),
            on
        )
        _meteredNetworkWarning.value = on
    }

    fun toggleSponsorship() {
        setSponsorship(!(_sponsorship.value ?: true))
    }

    fun setSponsorship(on: Boolean) {
        if (_sponsorship.value == on) {
            return
        }
        LocalSettingHelper.putBoolean(
            app,
            getString(R.string.preference_key_show_sponsorship),
            on
        )
        _sponsorship.value = on
    }

    fun cleanUpCache() {
        ImageIO.clearCache()
        Toaster.sendShortToast(R.string.all_clear)
        _cacheSizeText.value = getString(R.string.zero_size)
    }

    fun cleanUpDatabase() {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                AppDatabase.instance.clearAllTables()
            }

            Toaster.sendShortToast(R.string.all_clear)
        }
    }

    fun updateCacheSize() {
        var length = ImagePipelineFactory.getInstance().mainFileCache.size / 1024f / 1024
        if (length < 0f) {
            length = 0f
        }
        _cacheSizeText.value = "${String.format("%.2f", length)} MB"
    }
}