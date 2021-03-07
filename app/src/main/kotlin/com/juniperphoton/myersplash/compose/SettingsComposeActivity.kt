package com.juniperphoton.myersplash.compose

import android.os.Bundle
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.activity.BaseActivity
import com.juniperphoton.myersplash.viewmodel.AppViewModelProviders

/**
 * @author dengweichao @ Zhihu Inc.
 * @since 2021-03-07
 */
class SettingsComposeActivity : BaseActivity() {
    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_compose)
        viewModel = AppViewModelProviders.of(this).get(SettingsViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateCacheSize()
    }
}