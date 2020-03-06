package com.example.wallet.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.wallet.data.dao.LocationDAO
import com.example.wallet.data.entity.DLocation

// Annotates class to be a Room Database with a table (entity) of the Location class
@Database(entities = arrayOf(DLocation::class), version = 3, exportSchema = false)
public abstract class WalletRoomDatabase : RoomDatabase() {

    abstract fun LocationDAO(): LocationDAO

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: WalletRoomDatabase? = null

        fun getDatabase(context: Context): WalletRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WalletRoomDatabase::class.java,
                    "wallet"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}

