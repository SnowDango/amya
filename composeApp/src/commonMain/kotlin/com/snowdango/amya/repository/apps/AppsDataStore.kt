package com.snowdango.amya.repository.apps

import com.snowdango.amya.domain.db.AppsDatabase
import com.snowdango.amya.domain.db.entity.AppsEntity
import kotlinx.coroutines.flow.Flow

class AppsDataStore(private val database: AppsDatabase) {

    suspend fun insert(appsEntity: AppsEntity): Long {
        return database.appsDao().insert(appsEntity)
    }

    fun getAll(): Flow<List<AppsEntity>> {
        return database.appsDao().getAll()
    }

    fun getAppsByTagId(tagId: Long): Flow<List<AppsEntity>> {
        return database.appsDao().getAppsByTagId(tagId)
    }

    fun getAppsBySubTagId(tagId: Long, subTagId: Long): Flow<List<AppsEntity>> {
        return database.appsDao().getAppsBySubTagId(tagId, subTagId)
    }

    suspend fun updateApp(id: Long, name: String, path: String, imageUrl: String) {
        database.appsDao().updateApp(id, name, path, imageUrl)
    }

    suspend fun transferApp(id: Long, tagId: Long, subTagId: Long?) {
        database.appsDao().transferApp(id, tagId, subTagId)
    }

    suspend fun delete(id: Long) {
        database.appsDao().delete(id)
    }
}
