package com.yushin.lockapplication.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yushin.lockapplication.dao.LockDao
import com.yushin.lockapplication.entities.LockEntity

@Database(entities = [LockEntity::class], version = 4, exportSchema = false)
abstract class LockDatabase : RoomDatabase() {
    abstract fun lockDao(): LockDao

    companion object {
        @Volatile
        private var INSTANCE: LockDatabase? = null

        fun getInstance(context: Context): LockDatabase {
            Log.d("getInstance", "Application context: $context")
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        LockDatabase::class.java,
                        "lock_setting"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    Log.d("getInstance", "$instance")
                    INSTANCE = instance
                }
                return instance
            }
        }    }
}

