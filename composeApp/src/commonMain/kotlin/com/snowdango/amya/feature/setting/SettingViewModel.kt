package com.snowdango.amya.feature.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snowdango.amya.model.SettingsModel
import com.snowdango.amya.platform.AutoLaunchExtension
import com.snowdango.amya.platform.Log
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingViewModel: ViewModel(), KoinComponent {

    private val settingsModel: SettingsModel by inject()


    private val _isClosedMinimize = settingsModel.getIsClosedMinimize()
    val isClosedMinimize = _isClosedMinimize.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        initialValue = true,
    )

    private val _isEnableAutoLaunch: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isEnableAutoLaunch = _isEnableAutoLaunch.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    init {
        viewModelScope.launch {
            _isEnableAutoLaunch.emit(AutoLaunchExtension.isEnabled())
        }
    }

    fun onChangeClosedMinimize(value: Boolean) {
        viewModelScope.launch {
            settingsModel.setIsClosedMinimize(value)
        }
    }

    fun onChangeAutoLaunch(value: Boolean) {
        viewModelScope.launch {
            try {

                if (value) {
                    AutoLaunchExtension.enable()
                } else {
                    AutoLaunchExtension.disable()
                }
            }catch (ce: CancellationException) {
                throw ce
            }catch (th: Throwable){
                Log.e(th.message.toString())
            }finally {
                _isEnableAutoLaunch.emit(AutoLaunchExtension.isEnabled())
            }
        }
    }

}