package com.juniperphoton.myersplash.compose

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.compose.theme.AppTheme
import com.juniperphoton.myersplash.utils.ThemeHelper
import com.juniperphoton.myersplash.viewmodel.AppViewModelProviders
import java.util.*

/**
 * @author dengweichao
 * @since 2021-03-07
 */
class SettingsContentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AbstractComposeView(context, attrs, defStyle) {
    private val viewModel: SettingsViewModel =
        AppViewModelProviders.of(context as FragmentActivity).get(SettingsViewModel::class.java)

    @Composable
    override fun Content() = AppTheme {
        Surface(
            modifier = Modifier
                .background(color = MaterialTheme.colors.surface)
                .padding(bottom = 50.dp)
        ) {
            SettingsContent(onSetTheme = { which ->
                viewModel.setTheme(which)
                ThemeHelper.switchTheme(context)
            }, onSetDownloadQuality = { which ->
                viewModel.setDownloadQuality(which)
            }, onSetBrowsingQuality = { which ->
                viewModel.setBrowsingQuality(which)
            })
        }
    }
}

enum class DialogType {
    NONE,
    THEME,
    DOWNLOAD_QUALITY,
    BROWSING_QUALITY
}

@Composable
fun SettingsContent(
    viewModel: SettingsViewModel = viewModel(),
    onSetTheme: (Int) -> Unit,
    onSetDownloadQuality: (Int) -> Unit,
    onSetBrowsingQuality: (Int) -> Unit
) {
    val networkState = viewModel.meteredNetworkWarning.observeAsState()
    val sponsorship = viewModel.sponsorship.observeAsState()
    val theme = viewModel.theme.observeAsState()
    val downloadQuality = viewModel.downloadQuality.observeAsState()
    val browsingQuality = viewModel.browsingQuality.observeAsState()
    val cacheSize = viewModel.cacheSizeText.observeAsState().value
        ?: stringResource(id = R.string.zero_size)

    val dialogType = remember { mutableStateOf(DialogType.NONE) }

    Column(
        modifier = Modifier
            .padding(
                vertical = dimensionResource(
                    id = R.dimen.settings_margin_mini_title_top
                )
            )
            .verticalScroll(rememberScrollState()),
    ) {
        SubTitle(text = stringResource(id = R.string.personalization))

        SwitchItem(
            title = stringResource(id = R.string.settings_download_metered_title),
            subTitle = stringResource(
                id = R.string.settings_download_metered
            ),
            hasSwitch = true,
            checked = networkState.value ?: true,
            onCheckedChange = { on ->
                viewModel.setMeteredNetworkWarning(on)
            },
            onClicked = {
                viewModel.toggleMeteredNetworkWarning()
            }
        )

        SwitchItem(
            title = stringResource(id = R.string.settings_sponsorship_title),
            subTitle = stringResource(
                id = R.string.settings_sponsorship
            ),
            hasSwitch = true,
            checked = sponsorship.value ?: true,
            onCheckedChange = { on ->
                viewModel.setSponsorship(on)
            },
            onClicked = {
                viewModel.toggleSponsorship()
            }
        )
        SwitchItem(
            title = stringResource(id = R.string.settings_theme),
            subTitle = theme.value ?: "",
            hasSwitch = false,
            onClicked = {
                dialogType.value = DialogType.THEME
            }
        )
        VerticalSpacer()
        SubTitle(text = stringResource(id = R.string.quality))
        SwitchItem(
            title = stringResource(id = R.string.settings_download_quality),
            subTitle = downloadQuality.value ?: "",
            hasSwitch = false,
            onClicked = {
                dialogType.value = DialogType.DOWNLOAD_QUALITY
            }
        )
        SwitchItem(
            title = stringResource(id = R.string.settings_loading_quality),
            subTitle = browsingQuality.value ?: "",
            hasSwitch = false,
            onClicked = {
                dialogType.value = DialogType.BROWSING_QUALITY
            }
        )
        VerticalSpacer()

        SubTitle(text = stringResource(id = R.string.clear_options))
        SwitchItem(
            title = stringResource(id = R.string.cleanup_downloads_list),
            subTitle = stringResource(id = R.string.the_will_only_clear_the_list_in_app),
            hasSwitch = false,
            onClicked = {
                viewModel.cleanUpDatabase()
            }
        )

        SwitchItem(
            title = stringResource(id = R.string.settings_clean_up_cache),
            subTitle = cacheSize,
            hasSwitch = false,
            onClicked = {
                viewModel.cleanUpCache()
            }
        )
    }

    val title = when (dialogType.value) {
        DialogType.THEME -> {
            R.string.settings_title
        }
        DialogType.BROWSING_QUALITY -> {
            R.string.settings_download_quality
        }
        DialogType.DOWNLOAD_QUALITY -> {
            R.string.settings_loading_quality
        }
        else -> {
            0
        }
    }

    val list = when (dialogType.value) {
        DialogType.THEME -> {
            listOf(
                R.string.settings_theme_dark,
                R.string.settings_theme_light,
                R.string.settings_theme_system
            )
        }
        DialogType.DOWNLOAD_QUALITY -> {
            listOf(
                R.string.settings_download_highest,
                R.string.settings_download_high,
                R.string.settings_download_medium
            )
        }
        DialogType.BROWSING_QUALITY -> {
            listOf(
                R.string.settings_browsing_large,
                R.string.settings_browsing_small,
                R.string.settings_browsing_thumb
            )
        }
        else -> {
            listOf()
        }
    }

    val onClickItem: (Int) -> Unit = when (dialogType.value) {
        DialogType.THEME -> {
            {
                onSetTheme(it)
            }
        }
        DialogType.BROWSING_QUALITY -> {
            {
                onSetBrowsingQuality(it)
            }
        }
        DialogType.DOWNLOAD_QUALITY -> {
            {
                onSetDownloadQuality(it)
            }
        }
        else -> {
            {
                // ignored
            }
        }
    }

    if (dialogType.value != DialogType.NONE) {
        SelectDialog(
            title = title,
            contentList = list,
            onDismissRequest = {
                dialogType.value = DialogType.NONE
            }, onClickItem = {
                dialogType.value = DialogType.NONE
                onClickItem(it)
            }
        )
    }
}

private fun Modifier.forThemeText(): Modifier {
    return this
        .padding(12.dp)
        .fillMaxWidth()
}

@Composable
fun SelectDialog(
    @StringRes title: Int,
    @StringRes contentList: List<Int>,
    onDismissRequest: () -> Unit,
    onClickItem: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        buttons = {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                repeat(contentList.size) {
                    Text(
                        text = stringResource(id = contentList[it]),
                        modifier = Modifier
                            .clickable {
                                onClickItem(it)
                            }
                            .forThemeText()
                    )
                }
            }
        },
        title = {
            Text(text = stringResource(id = title), fontWeight = FontWeight.Bold)
        },
    )
}

@Composable
fun VerticalSpacer() {
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun SubTitle(text: String) {
    Text(
        text = text.toUpperCase(Locale.getDefault()),
        fontWeight = FontWeight.Bold,
        color = colorResource(id = R.color.MyerSplashThemeColor),
        fontSize = 12.sp,
        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.settings_margin_left))
    )
}

@Composable
fun SwitchItem(
    title: String,
    subTitle: String,
    hasSwitch: Boolean,
    checked: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClicked: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(
                vertical = 4.dp,
            )
            .clickable(enabled = onClicked != null) {
                onClicked?.invoke()
            }) {
        Spacer(
            modifier = Modifier.width(
                dimensionResource(id = R.dimen.settings_margin_left)
            )
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.settings_margin_up_and_down)),
            modifier = Modifier
                .weight(2f, fill = true)
                .padding(vertical = dimensionResource(id = R.dimen.settings_margin_up_and_down))
        ) {
            Text(text = title, color = MaterialTheme.colors.onPrimary, fontSize = 15.sp)
            Text(
                text = subTitle,
                color = MaterialTheme.colors.onPrimary.copy(alpha = 0.5f),
                fontSize = 13.sp
            )
        }
        if (hasSwitch) {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colorResource(id = R.color.MyerSplashThemeColor),
                    uncheckedThumbColor = Color.White
                )
            )
        }
        Spacer(
            modifier = Modifier.width(
                dimensionResource(id = R.dimen.settings_margin_left)
            )
        )
    }
}

@Preview(widthDp = 300, heightDp = 400)
@Composable
fun Preview() {
    SettingsContent(onSetTheme = {}, onSetBrowsingQuality = {}, onSetDownloadQuality = {})
}
