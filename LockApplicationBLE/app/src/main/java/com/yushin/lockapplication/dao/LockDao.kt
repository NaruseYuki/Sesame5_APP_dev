package com.yushin.lockapplication.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.yushin.lockapplication.entities.LockEntity

@Dao
interface LockDao {
    @Query("SELECT * FROM lock_setting")
    suspend fun getAllLocks(): List<LockEntity>

    @Insert
    suspend fun insertLock(lockEntity: LockEntity)

    @Delete
    suspend fun deleteLock(lockEntity: LockEntity)

    @Update
    suspend fun updateLock(lockEntity: LockEntity)
}