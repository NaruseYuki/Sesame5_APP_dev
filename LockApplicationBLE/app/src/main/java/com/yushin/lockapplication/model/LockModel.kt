package com.yushin.lockapplication.model
import android.icu.text.Transliterator.Position
import com.yushin.lockapplication.repository.LockRepository
import com.yushin.lockapplication.entities.LockEntity

class LockModel private constructor(private val lockRepository: LockRepository) {
    companion object {
        private var instance: LockModel? = null

        fun initialize(lockRepository: LockRepository) {
            if (instance == null) {
                instance = LockModel(lockRepository)
            }
        }

        fun getInstance(): LockModel {
            if (instance == null) {
                throw IllegalStateException("LockModel is not initialized.")
            }
            return instance!!
        }
    }


    suspend fun getAllLocks(): List<LockEntity> {
        return lockRepository.getAllLocks()
    }

    suspend fun insertLock(lockEntity: LockEntity) {
        lockRepository.insertLock(lockEntity)
    }

    suspend fun deleteLock(lockEntity: LockEntity) {
        lockRepository.deleteLock(lockEntity)
    }

    suspend fun updateLock(lockEntity: LockEntity) {
        lockRepository.updateLock(lockEntity)
    }
}