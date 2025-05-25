package com.snowdango.amya.repository.apps

import com.snowdango.amya.domain.db.entity.AppsEntity
import kotlinx.coroutines.flow.Flow

class AppsRepository(
    private val appsDataStore: AppsDataStore,
) {

    suspend fun insert(appsEntity: AppsEntity): Long {
        return appsDataStore.insert(appsEntity)
    }

    fun getAll(): Flow<List<AppsEntity>> {
        return appsDataStore.getAll()
    }

    fun getAppsByTagId(tagId: Long): Flow<List<AppsEntity>> {
        return appsDataStore.getAppsByTagId(tagId)
    }

    fun getAppsBySubTagId(tagId: Long, subTagId: Long): Flow<List<AppsEntity>> {
        return appsDataStore.getAppsBySubTagId(tagId, subTagId)
    }

    suspend fun updateApp(id: Long, name: String, path: String, imageUrl: String) {
        appsDataStore.updateApp(id, name, path, imageUrl)
    }

    suspend fun transferApp(id: Long, tagId: Long, subTagId: Long?) {
        appsDataStore.transferApp(id, tagId, subTagId)
    }

    suspend fun delete(id: Long) {
        appsDataStore.delete(id)
    }
}
