package com.snowdango.amya.domain.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.snowdango.amya.domain.db.entity.AppsEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface AppsDao {

    @Insert
    suspend fun insert(appsEntity: AppsEntity): Long

    @Query("select * from ${AppsEntity.TABLE_NAME}")
    fun getAll(): Flow<List<AppsEntity>>

    @Query("select * from ${AppsEntity.TABLE_NAME} where ${AppsEntity.COLUMN_TAG_ID} = :tagId")
    fun getAppsByTagId(tagId: Long): Flow<List<AppsEntity>>

    @Query("select * from ${AppsEntity.TABLE_NAME} where ${AppsEntity.COLUMN_TAG_ID} = :tagId and ${AppsEntity.COLUMN_SUB_TAG_ID} = :subTagId")
    fun getAppsBySubTagId(tagId: Long, subTagId: Long): Flow<List<AppsEntity>>

}