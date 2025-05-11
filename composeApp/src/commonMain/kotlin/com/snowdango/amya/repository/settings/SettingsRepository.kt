package com.snowdango.amya.repository.settings

import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent

class SettingsRepository(
    private val settingsDataStore: SettingsDataStore,
) {

    suspend fun setIsClosedMinimize(value: Boolean) {
        settingsDataStore.setIsClosedMinimize(value)
    }

    fun getIsClosedMinimize(): Flow<Boolean> {
        return settingsDataStore.getIsClosedMinimize()
    }

}