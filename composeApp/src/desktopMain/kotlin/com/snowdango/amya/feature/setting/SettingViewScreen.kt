package com.snowdango.amya.feature.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSwitch
import com.snowdango.amya.BuildKonfig
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingViewScreen(
    navigateLicense: () -> Unit,
    viewModel: SettingViewModel = koinViewModel()
) {
    Box(
        modifier = Modifier
            .padding(all = 16.dp)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            SettingsGroup(
                modifier = Modifier
                    .fillMaxWidth(),
                title = { Text(text = "AppSettings") },
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                val isClosedMinimize by viewModel.isClosedMinimize.collectAsStateWithLifecycle()
                Box(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    SettingsSwitch(
                        state = isClosedMinimize,
                        title = { Text(text = "Minimize with Close Window") },
                        onCheckedChange = {
                            viewModel.onChangeClosedMinimize(it)
                        }
                    )
                }
                val isShowTray by viewModel.isShowTray.collectAsStateWithLifecycle()
                Box(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    SettingsSwitch(
                        state = isShowTray,
                        title = { Text(text = "Display in system tray") },
                        onCheckedChange = {
                            viewModel.onChangeShowTray(it)
                        }
                    )
                }
                val isAutoLaunch by viewModel.isEnableAutoLaunch.collectAsStateWithLifecycle()
                Box(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    SettingsSwitch(
                        state = isAutoLaunch,
                        title = { Text(text = "Application launch on startup") },
                        onCheckedChange = {
                            viewModel.onChangeAutoLaunch(it)
                        }
                    )
                }
                SteamSettings(viewModel)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    SettingsMenuLink(
                        title = { Text(text = "Show Licenses") },
                        onClick = {
                            navigateLicense.invoke()
                        },
                    )
                }
                Text(
                    text = "Version: ${BuildKonfig.appVersion}",
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(start = 18.dp)
                        .fillMaxSize()
                )
            }
        }
    }
}

/**
 * Steam同期の設定。Steamがインストールされている環境でのみ表示する
 */
@Composable
private fun SteamSettings(viewModel: SettingViewModel) {
    val isSteamAvailable by viewModel.isSteamAvailable.collectAsStateWithLifecycle()
    if (!isSteamAvailable) return

    val isSteamSyncEnabled by viewModel.isSteamSyncEnabled.collectAsStateWithLifecycle()
    val isSteamSyncing by viewModel.isSteamSyncing.collectAsStateWithLifecycle()
    val excludedCount by viewModel.steamExcludedCount.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        SettingsSwitch(
            state = isSteamSyncEnabled,
            title = { Text(text = "Show Steam games") },
            subtitle = { Text(text = "Automatically add and remove installed Steam games") },
            onCheckedChange = {
                viewModel.onChangeSteamSync(it)
            }
        )
    }
    if (!isSteamSyncEnabled) return
    Box(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        SettingsMenuLink(
            title = { Text(text = "Sync Steam games now") },
            enabled = !isSteamSyncing,
            onClick = {
                viewModel.onSyncSteamNow()
            },
        )
    }
    Box(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        SettingsMenuLink(
            title = { Text(text = "Restore deleted Steam games") },
            subtitle = { Text(text = "Steam games you deleted: $excludedCount") },
            enabled = excludedCount > 0,
            onClick = {
                viewModel.onResetSteamExcluded()
            },
        )
    }
}
