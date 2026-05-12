package com.ksheera.sagara.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cows")
data class Cow(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val breed: String = "",
    val tagNumber: String = "",
    val isActive: Boolean = true
)
