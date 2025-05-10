package com.snowdango.amya.platform

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.snowdango.amya.domain.db.SettingsPreferences
import java.io.File


actual fun getSettingsPreferences(): SettingsPreferences {
    return SettingsPreferences(
        PreferenceDataStoreFactory.create {
            File(getAppDirs().getUserDataDir(), "settings.preferences_pb")
        }
    )
}