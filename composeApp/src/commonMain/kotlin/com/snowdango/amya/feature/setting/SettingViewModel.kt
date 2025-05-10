package com.snowdango.amya.feature.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snowdango.amya.model.SettingsModel
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

    fun onChangeClosedMinimize(value: Boolean) {
        viewModelScope.launch {
            settingsModel.setIsClosedMinimize(value)
        }
    }


}