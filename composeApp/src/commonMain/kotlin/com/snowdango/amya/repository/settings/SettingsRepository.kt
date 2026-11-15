package com.snowdango.amya.repository.settings

import kotlinx.coroutines.flow.Flow

class SettingsRepository(
    private val settingsDataStore: SettingsDataStore,
) {

    suspend fun setIsClosedMinimize(value: Boolean) {
        settingsDataStore.setIsClosedMinimize(value)
    }

    suspend fun setIsShowTray(value: Boolean) {
        settingsDataStore.setIsShowTray(value)
    }

    fun getIsClosedMinimize(): Flow<Boolean> {
        return settingsDataStore.getIsClosedMinimize()
    }

    fun getIsShowTray(): Flow<Boolean> {
        return settingsDataStore.getIsShowTray()
    }

    suspend fun setIsSteamSyncEnabled(value: Boolean) {
        settingsDataStore.setIsSteamSyncEnabled(value)
    }

    fun getIsSteamSyncEnabled(): Flow<Boolean> {
        return settingsDataStore.getIsSteamSyncEnabled()
    }

    suspend fun setSteamTagId(value: Long?) {
        settingsDataStore.setSteamTagId(value)
    }

    fun getSteamTagId(): Flow<Long?> {
        return settingsDataStore.getSteamTagId()
    }
}
