package com.snowdango.amya.repository.apps

import com.snowdango.amya.domain.db.entity.AppsEntity
import kotlinx.coroutines.flow.Flow

class AppsRepository(
    private val appsDataStore: AppsDataStore,
) {

    suspend fun insert(appsEntity: AppsEntity): Long {
        return appsDataStore.insert(appsEntity)
    }

    suspend fun getAll(): Flow<List<AppsEntity>> {
        return appsDataStore.getAll()
    }

    suspend fun getAppsByTagId(tagId: Long): Flow<List<AppsEntity>> {
        return appsDataStore.getAppsByTagId(tagId)
    }

    suspend fun getAppsBySubTagId(tagId: Long, subTagId: Long): Flow<List<AppsEntity>> {
        return appsDataStore.getAppsBySubTagId(tagId, subTagId)
    }
}