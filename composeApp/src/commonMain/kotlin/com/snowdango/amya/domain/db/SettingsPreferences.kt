package com.snowdango.amya.domain.db

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsPreferences(
    private val dataStore: DataStore<Preferences>,
) {
    val IS_CLOSED_MINIMIZE = booleanPreferencesKey("is_closed_minimize")
    val IS_SHOW_TRAY = booleanPreferencesKey("is_show_tray")
    val IS_STEAM_SYNC_ENABLED = booleanPreferencesKey("steam_sync_enabled")
    val STEAM_TAG_ID = longPreferencesKey("steam_tag_id")

    fun getIsClosedMinimize(): Flow<Boolean> {
        return dataStore.data.map {
            it[IS_CLOSED_MINIMIZE] ?: true
        }
    }

    fun getIsShowTray(): Flow<Boolean> {
        return dataStore.data.map {
            it[IS_SHOW_TRAY] ?: false
        }
    }

    suspend fun setIsClosedMinimize(value: Boolean) {
        dataStore.edit {
            it[IS_CLOSED_MINIMIZE] = value
        }
    }

    suspend fun setIsShowTray(value: Boolean) {
        dataStore.edit {
            it[IS_SHOW_TRAY] = value
        }
    }

    fun getIsSteamSyncEnabled(): Flow<Boolean> {
        return dataStore.data.map {
            it[IS_STEAM_SYNC_ENABLED] ?: false
        }
    }

    suspend fun setIsSteamSyncEnabled(value: Boolean) {
        dataStore.edit {
            it[IS_STEAM_SYNC_ENABLED] = value
        }
    }

    fun getSteamTagId(): Flow<Long?> {
        return dataStore.data.map {
            it[STEAM_TAG_ID]
        }
    }

    suspend fun setSteamTagId(value: Long?) {
        dataStore.edit {
            if (value == null) {
                it.remove(STEAM_TAG_ID)
            } else {
                it[STEAM_TAG_ID] = value
            }
        }
    }
}
