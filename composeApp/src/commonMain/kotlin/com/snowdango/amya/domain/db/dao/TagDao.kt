package com.snowdango.amya.domain.db.dao

import androidx.room.Dao
import androidx.room.Insert
import com.snowdango.amya.domain.db.entity.TagEntity


@Dao
interface TagDao {

    @Insert
    suspend fun insert(tagEntity: TagEntity): Long

}