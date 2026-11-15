package com.snowdango.amya.model

import com.snowdango.amya.repository.settings.SettingsRepository
import com.snowdango.amya.repository.steam.SteamExcludedRepository
import com.snowdango.amya.steam.SteamSyncService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SteamModel : KoinComponent {

    private val steamSyncService: SteamSyncService by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val steamExcludedRepository: SteamExcludedRepository by inject()

    val isSteamAvailable: StateFlow<Boolean> get() = steamSyncService.isSteamAvailable

    suspend fun refreshAvailability(): Boolean = steamSyncService.refreshAvailability()

    fun getIsSteamSyncEnabled(): Flow<Boolean> = settingsRepository.getIsSteamSyncEnabled()

    suspend fun setIsSteamSyncEnabled(value: Boolean) {
        settingsRepository.setIsSteamSyncEnabled(value)
    }

    suspend fun syncNow(): SteamSyncService.SyncResult = steamSyncService.sync(force = true)

    fun getExcludedCount(): Flow<Int> = steamExcludedRepository.count()

    /** 手動で削除したSteamゲームの除外をリセットし、次回の同期で再登録されるようにする */
    suspend fun resetExcluded() {
        steamExcludedRepository.deleteAll()
        steamSyncService.requestSync(force = true)
    }
}
