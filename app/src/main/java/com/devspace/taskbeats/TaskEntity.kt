package com.devspace.taskbeats

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity
data class TaskEntity(
    @PrimaryKey
    @ColumnInfo
    val name: String,
    @ColumnInfo
    val category: String
)

