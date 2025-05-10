package com.snowdango.amya

import androidx.lifecycle.ViewModel
import com.snowdango.amya.model.SettingsModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainViewModel: ViewModel(), KoinComponent {

    private val settingsModel: SettingsModel by inject()

    fun isClosedMinimize() = settingsModel.getIsClosedMinimize()

}