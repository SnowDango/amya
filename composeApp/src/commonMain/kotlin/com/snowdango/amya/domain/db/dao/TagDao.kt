package com.snowdango.amya.domain.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.snowdango.amya.domain.db.entity.TagEntity
import com.snowdango.amya.domain.db.entity.relation.TagGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Insert
    suspend fun insert(tagEntity: TagEntity): Long

    @Transaction
    @Query("select * from ${TagEntity.TABLE_NAME}")
    fun getAllGroup(): Flow<List<TagGroupEntity>>

    @Update
    suspend fun update(tagEntity: TagEntity)

    @Query("delete from ${TagEntity.TABLE_NAME} where id = :id")
    suspend fun delete(id: Long)
}
