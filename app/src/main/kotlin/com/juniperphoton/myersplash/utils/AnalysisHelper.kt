package com.juniperphoton.myersplash.utils

import com.microsoft.appcenter.analytics.Analytics

/**
 * @author JuniperPhoton @ Zhihu Inc.
 * @since 2019-03-26
 */
object AnalysisHelper {
    fun logTabSelected(name: String) {
        Analytics.trackEvent("Tab selected", mapOf("Name" to name))
    }

    fun logEnterDownloads() {
        Analytics.trackEvent("Enter downloads")
    }

    fun logEnterSearch() {
        Analytics.trackEvent("Enter search")
    }

    fun logClickDownloadInList() {
        Analytics.trackEvent("Click download in list")
    }

    fun logClickDownloadInDetails() {
        Analytics.trackEvent("Click download in details")
    }

    fun logClickCancelDownloadInDetails() {
        Analytics.trackEvent("Click cancel download in details")
    }

    fun logClickSetAsInDetails() {
        Analytics.trackEvent("Click set-as button in details")
    }

    fun logToggleImageDetails() {
        Analytics.trackEvent("Toggle image details")
    }

    fun logRefreshList() {
        Analytics.trackEvent("Refresh list")
    }

    fun logApplyEdit(dimProgress: Boolean) {
        Analytics.trackEvent("Apply edit", mapOf("Dim progress" to dimProgress.toString()))
    }

    fun logEditShowPreview() {
        Analytics.trackEvent("Edit show preview")
    }
}