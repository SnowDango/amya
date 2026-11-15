package com.snowdango.amya.feature.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snowdango.amya.model.SettingsModel
import com.snowdango.amya.model.SteamModel
import com.snowdango.amya.platform.AutoLaunchExtension
import com.snowdango.amya.track.Log
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingViewModel : ViewModel(), KoinComponent {

    private val settingsModel: SettingsModel by inject()
    private val steamModel: SteamModel by inject()

    private val _isClosedMinimize = settingsModel.getIsClosedMinimize()
    val isClosedMinimize = _isClosedMinimize.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        initialValue = true,
    )

    private val _isShowTray = settingsModel.getIsShowTray()
    val isShowTray = _isShowTray.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    private val _isEnableAutoLaunch: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isEnableAutoLaunch = _isEnableAutoLaunch.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    val isSteamAvailable = steamModel.isSteamAvailable

    val isSteamSyncEnabled = steamModel.getIsSteamSyncEnabled().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    val steamExcludedCount = steamModel.getExcludedCount().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        initialValue = 0,
    )

    private val _isSteamSyncing = MutableStateFlow(false)
    val isSteamSyncing = _isSteamSyncing.asStateFlow()

    init {
        viewModelScope.launch {
            _isEnableAutoLaunch.emit(AutoLaunchExtension.isEnabled())
        }
        viewModelScope.launch {
            try {
                steamModel.refreshAvailability()
            } catch (ce: CancellationException) {
                throw ce
            } catch (th: Throwable) {
                Log.e(th.message.toString())
            }
        }
    }

    fun onChangeSteamSync(value: Boolean) {
        Log.d("onChangeSteamSync: $value")
        viewModelScope.launch {
            try {
                steamModel.setIsSteamSyncEnabled(value)
            } catch (ce: CancellationException) {
                throw ce
            } catch (th: Throwable) {
                Log.e(th.message.toString())
            }
        }
    }

    fun onSyncSteamNow() {
        Log.d("onSyncSteamNow")
        if (_isSteamSyncing.value) return
        viewModelScope.launch {
            _isSteamSyncing.emit(true)
            try {
                Log.i("Steam sync result: ${steamModel.syncNow()}")
            } catch (ce: CancellationException) {
                throw ce
            } catch (th: Throwable) {
                Log.e(th.message.toString())
            } finally {
                _isSteamSyncing.emit(false)
            }
        }
    }

    fun onResetSteamExcluded() {
        Log.d("onResetSteamExcluded")
        viewModelScope.launch {
            try {
                steamModel.resetExcluded()
            } catch (ce: CancellationException) {
                throw ce
            } catch (th: Throwable) {
                Log.e(th.message.toString())
            }
        }
    }

    fun onChangeClosedMinimize(value: Boolean) {
        Log.d("onChangeClosedMinimize: $value")
        viewModelScope.launch {
            try {
                settingsModel.setIsClosedMinimize(value)
            } catch (ce: CancellationException) {
                throw ce
            } catch (th: Throwable) {
                Log.e(th.message.toString())
            }
        }
    }

    fun onChangeShowTray(value: Boolean) {
        Log.d("onChangeShowTray: $value")
        viewModelScope.launch {
            try {
                settingsModel.setIsShowTray(value)
            } catch (ce: CancellationException) {
                throw ce
            } catch (th: Throwable) {
                Log.e(th.message.toString())
            }
        }
    }

    fun onChangeAutoLaunch(value: Boolean) {
        Log.d("onChangeAutoLaunch: $value")
        viewModelScope.launch {
            try {
                if (value) {
                    AutoLaunchExtension.enable()
                } else {
                    AutoLaunchExtension.disable()
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (th: Throwable) {
                Log.e(th.message.toString())
            } finally {
                _isEnableAutoLaunch.emit(AutoLaunchExtension.isEnabled())
            }
        }
    }
}
