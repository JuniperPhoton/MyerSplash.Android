package com.juniperphoton.myersplash.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.model.DownloadItem

@Database(entities = [DownloadItem::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        private const val DB_NAME = "myersplash_db"

        val instance: AppDatabase by lazy {
            Room.databaseBuilder(
                    App.instance,
                    AppDatabase::class.java,
                    DB_NAME
            ).fallbackToDestructiveMigration().build()
        }
    }

    abstract fun downloadItemDao(): DownloadItemDao
}