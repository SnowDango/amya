package com.snowdango.amya.model

import com.snowdango.amya.repository.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingsModel: KoinComponent {

    private val settingsRepository: SettingsRepository by inject()

    suspend fun setIsClosedMinimize(value: Boolean) {
        settingsRepository.setIsClosedMinimize(value)
    }

    suspend fun setIsShowTray(value: Boolean) {
        settingsRepository.setIsShowTray(value)
    }

    fun getIsClosedMinimize(): Flow<Boolean> {
        return settingsRepository.getIsClosedMinimize()
    }

    fun getIsShowTray(): Flow<Boolean> {
        return settingsRepository.getIsShowTray()
    }

}