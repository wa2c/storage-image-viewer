package com.wa2c.android.storageimageviewer.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Dao
interface StorageDao {

    @Query("SELECT count(id) FROM ${SafStorageEntity.TABLE_NAME}")
    suspend fun getCount(): Int

    @Query("SELECT IFNULL(MAX(sort_order), 0) + 1 FROM ${SafStorageEntity.TABLE_NAME}")
    fun getNextSortOrder(): Int

    @Query("SELECT * FROM ${SafStorageEntity.TABLE_NAME} WHERE id = :id")
    suspend fun getEntity(id: String): SafStorageEntity?

    @Query("SELECT * FROM ${SafStorageEntity.TABLE_NAME} ORDER BY sort_order")
    fun getList(): Flow<List<SafStorageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SafStorageEntity)

    @Query("DELETE FROM ${SafStorageEntity.TABLE_NAME} WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE ${SafStorageEntity.TABLE_NAME} SET sort_order = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: String, sortOrder: Int)

    @Transaction
    suspend fun move(fromPosition: Int, toPosition: Int) {
        val list = getList().first().toMutableList()
        list.add(toPosition, list.removeAt(fromPosition))
        list.forEachIndexed { index, entity ->
            updateSortOrder(entity.id, index + 1)
        }
    }

}
