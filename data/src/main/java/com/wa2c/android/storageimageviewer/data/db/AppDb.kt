package com.wa2c.android.storageimageviewer.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SafStorageEntity::class,
    ],
    version = AppDatabase.DB_VERSION,
    exportSchema = true,
)
internal abstract class AppDatabase : RoomDatabase() {

    abstract fun getStorageSettingDao(): StorageDao

    companion object {
        /** DB name */
        private const val DB_NAME = "app.db"
        /** DB version */
        const val DB_VERSION = 1

        /**
         * Build DB
         */
        fun buildDb(context: Context) : AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                .build()
        }
    }
}
