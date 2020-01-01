package com.juniperphoton.myersplash.utils

import com.microsoft.appcenter.analytics.Analytics

object AnalysisHelper {
    fun logTabSelected(name: String) {
        Analytics.trackEvent("Tab selected", mapOf("Name" to name))
    }

    fun logClickCopyUrl() {
        Analytics.trackEvent("URL copied")
    }

    fun logEnterDownloads() {
        Analytics.trackEvent("Enter downloads")
    }

    fun logDownloadSuccess(durationMs: Long) {
        Analytics.trackEvent("Download success", mapOf("Duration" to durationMs.toString()))
    }

    fun logDownloadFailed(e: Exception, durationMs: Long) {
        Analytics.trackEvent("Download failed",
                mapOf("Duration" to durationMs.toString(), "Error" to e.toString()))
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

    fun logClickSetAsInDownloadList() {
        Analytics.trackEvent("Click set-as button in download list")
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

    fun logClickMoreButtonInDownloadList() {
        Analytics.trackEvent("Click more button in download list")
    }
}