package com.snowdango.amya.repository.settings

import com.snowdango.amya.domain.db.SettingsPreferences

class SettingsDataStore(
    private val settingsPreferences: SettingsPreferences
) {

    suspend fun setIsClosedMinimize(value: Boolean) {
        settingsPreferences.setIsClosedMinimize(value)
    }

    suspend fun setIsShowTray(value: Boolean) {
        settingsPreferences.setIsShowTray(value)
    }

    fun getIsClosedMinimize() = settingsPreferences.getIsClosedMinimize()

    fun getIsShowTray() = settingsPreferences.getIsShowTray()

}