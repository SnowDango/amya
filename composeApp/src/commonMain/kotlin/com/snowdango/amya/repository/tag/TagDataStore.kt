package com.snowdango.amya.repository.tag

import com.snowdango.amya.domain.db.AppsDatabase
import com.snowdango.amya.domain.db.entity.SubTagEntity
import com.snowdango.amya.domain.db.entity.TagEntity
import com.snowdango.amya.domain.db.entity.relation.TagGroupEntity
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

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

}