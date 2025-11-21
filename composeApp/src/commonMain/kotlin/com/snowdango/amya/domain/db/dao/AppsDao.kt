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

    @Query(
        "select * from ${AppsEntity.TABLE_NAME} where ${AppsEntity.COLUMN_TAG_ID} = :tagId and ${AppsEntity.COLUMN_SUB_TAG_ID} = :subTagId"
    )
    fun getAppsBySubTagId(tagId: Long, subTagId: Long): Flow<List<AppsEntity>>

    @Query(
        "update ${AppsEntity.TABLE_NAME} set ${AppsEntity.COLUMN_NAME} = :name, ${AppsEntity.COLUMN_PATH} = :path, ${AppsEntity.COLUMN_ARGS} = :args, ${AppsEntity.COLUMN_IMAGE_URL} = :imageUrl, ${AppsEntity.COLUMN_ROOT} = :root where ${AppsEntity.COLUMN_ID} = :id"
    )
    suspend fun updateApp(id: Long, name: String, path: String, args: String?, imageUrl: String, root: Boolean)

    @Query(
        "update ${AppsEntity.TABLE_NAME} set ${AppsEntity.COLUMN_TAG_ID} = :tagId, ${AppsEntity.COLUMN_SUB_TAG_ID} = :subTagId where ${AppsEntity.COLUMN_ID} = :id"
    )
    suspend fun transferApp(id: Long, tagId: Long, subTagId: Long?)

    @Query("delete from ${AppsEntity.TABLE_NAME} where ${AppsEntity.COLUMN_ID} = :id")
    suspend fun delete(id: Long)
}
