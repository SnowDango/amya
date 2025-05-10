package com.snowdango.amya.feature.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsSwitch
import com.snowdango.amya.BuildKonfig
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun SettingViewScreen(
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