package com.snowdango.amya.repository.steam

import kotlinx.coroutines.flow.Flow

class SteamExcludedRepository(
    private val steamExcludedDataStore: SteamExcludedDataStore,
) {

    suspend fun insert(appId: Long) {
        steamExcludedDataStore.insert(appId)
    }

    suspend fun getAllAppIds(): Set<Long> {
        return steamExcludedDataStore.getAllAppIds().toSet()
    }

    fun count(): Flow<Int> {
        return steamExcludedDataStore.count()
    }

    suspend fun deleteAll() {
        steamExcludedDataStore.deleteAll()
    }
}
