package com.snowdango.amya

import com.snowdango.amya.model.SettingsModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainViewModel : KoinComponent {

    private val settingsModel: SettingsModel by inject()
    private val appScope: CoroutineScope by inject()

    private val _isClosedMinimize = settingsModel.getIsClosedMinimize()
    val isClosedMinimize = _isClosedMinimize.stateIn(
        appScope,
        SharingStarted.WhileSubscribed(5_000),
        initialValue = true,
    )

    private val _isShowTray = settingsModel.getIsShowTray()
    val isShowTray = _isShowTray.stateIn(
        appScope,
        SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )
}
