package com.yushin.lockapplication.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "lock_setting")
data class LockEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "lock_position") val lockPosition: Int,
    @ColumnInfo(name = "unlock_position") val unlockPosition: Int,
    @ColumnInfo(name = "uuid") val uuid: UUID?
)

