package com.yushin.lockapplication.repository

import com.yushin.lockapplication.dao.LockDao
import com.yushin.lockapplication.entities.LockEntity

class LockRepository(private val lockDao: LockDao) {
    suspend fun getAllLocks(): List<LockEntity> {
        return lockDao.getAllLocks()
    }

    suspend fun insertLock(lockEntity: LockEntity) {
        lockDao.insertLock(lockEntity)
    }

    suspend fun deleteLock(lockEntity: LockEntity) {
        lockDao.deleteLock(lockEntity)
    }

    suspend fun updateLock(lockEntity: LockEntity) {
        lockDao.updateLock(lockEntity)
    }
}
