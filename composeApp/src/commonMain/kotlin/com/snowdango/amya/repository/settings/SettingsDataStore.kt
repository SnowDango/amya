package com.snowdango.amya.repository.settings

import com.snowdango.amya.domain.db.SettingsPreferences

class SettingsDataStore(
    private val settingsPreferences: SettingsPreferences
) {

    suspend fun setIsClosedMinimize(value: Boolean) {
        settingsPreferences.setIsClosedMinimize(value)
    }

    fun getIsClosedMinimize() = settingsPreferences.getIsClosedMinimize()

}