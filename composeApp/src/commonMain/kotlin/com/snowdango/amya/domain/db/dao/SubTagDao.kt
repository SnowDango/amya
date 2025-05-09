package com.snowdango.amya.domain.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.snowdango.amya.domain.db.entity.SubTagEntity


@Dao
interface SubTagDao {

    @Insert
    suspend fun insert(subTagEntity: SubTagEntity): Long

    @Update
    suspend fun update(subTagEntity: SubTagEntity)

    @Query("delete from ${SubTagEntity.TABLE_NAME} where id = :id")
    suspend fun delete(id: Long): Int

}