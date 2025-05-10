package com.snowdango.amya.domain.db

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class SettingsPreferences(
    private val dataStore: DataStore<Preferences>,
) {
    val IS_CLOSED_MINIMIZE = booleanPreferencesKey("is_closed_minimize")

    fun getIsClosedMinimize(): Flow<Boolean> {
        return dataStore.data.map {
            it[IS_CLOSED_MINIMIZE] ?: true
        }
    }

}