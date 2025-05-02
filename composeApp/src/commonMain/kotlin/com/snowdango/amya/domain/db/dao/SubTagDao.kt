package com.snowdango.amya.domain.db.dao

import androidx.room.Dao
import androidx.room.Insert
import com.snowdango.amya.domain.db.entity.SubTagEntity


@Dao
interface SubTagDao {

    @Insert
    suspend fun insert(subTagEntity: SubTagEntity): Long

}