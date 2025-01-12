package com.wa2c.android.storageimageviewer.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = SafStorageEntity.TABLE_NAME,
    indices = [
        Index(value = ["sort_order"]),
    ]
)
data class SafStorageEntity(
    /** ID */
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    /** Name */
    @ColumnInfo(name = "name")
    val name: String,
    /** URI */
    @ColumnInfo(name = "uri")
    val uri: String,
    /** Sort Order */
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,
    /** Modified Date */
    @ColumnInfo(name = "modified_date")
    val modifiedDate: Long,
) {
    companion object {
        /** Table name.  */
        const val TABLE_NAME = "saf_storage"

    }
}
