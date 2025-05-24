package com.snowdango.amya.repository.tag

import com.snowdango.amya.domain.db.AppsDatabase
import com.snowdango.amya.domain.db.entity.SubTagEntity
import com.snowdango.amya.domain.db.entity.TagEntity
import com.snowdango.amya.domain.db.entity.relation.TagGroupEntity
import kotlinx.coroutines.flow.Flow

class TagDataStore(private val database: AppsDatabase) {

    suspend fun insert(tagEntity: TagEntity): Long {
        return database.tagDao().insert(tagEntity)
    }

    suspend fun insert(subTagEntity: SubTagEntity): Long {
        return database.subTagDao().insert(subTagEntity)
    }

    fun getAllGroup(): Flow<List<TagGroupEntity>> {
        return database.tagDao().getAllGroup()
    }

    suspend fun update(tagEntity: TagEntity) {
        database.tagDao().update(tagEntity = tagEntity)
    }

    suspend fun update(subTagEntity: SubTagEntity) {
        database.subTagDao().update(subTagEntity = subTagEntity)
    }

    suspend fun deleteParentTag(id: Long) {
        database.tagDao().delete(id)
    }

    suspend fun deleteChildTag(id: Long) {
        database.subTagDao().delete(id)
    }
}
