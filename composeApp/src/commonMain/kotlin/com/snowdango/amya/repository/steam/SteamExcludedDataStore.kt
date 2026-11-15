package com.snowdango.amya.repository.steam

import com.snowdango.amya.domain.db.AppsDatabase
import com.snowdango.amya.domain.db.entity.SteamExcludedEntity
import kotlinx.coroutines.flow.Flow

class SteamExcludedDataStore(private val database: AppsDatabase) {

    suspend fun insert(appId: Long) {
        database.steamExcludedDao().insert(SteamExcludedEntity(appId))
    }

    suspend fun getAllAppIds(): List<Long> {
        return database.steamExcludedDao().getAllAppIds()
    }

    fun count(): Flow<Int> {
        return database.steamExcludedDao().count()
    }

    suspend fun deleteAll() {
        database.steamExcludedDao().deleteAll()
    }
}
