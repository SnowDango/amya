package com.snowdango.amya.domain.db.dao

import androidx.room.Dao
import androidx.room.Insert
import com.snowdango.amya.domain.db.entity.AppsEntity


@Dao
interface AppsDao {

    @Insert
    suspend fun insert(appsEntity: AppsEntity): Long


}