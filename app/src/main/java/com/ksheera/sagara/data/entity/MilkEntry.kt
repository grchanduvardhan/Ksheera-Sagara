package com.ksheera.sagara.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "milk_entries")
data class MilkEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,           // timestamp in millis
    val liters: Double,
    val fatPercentage: Double,
    val snfPercentage: Double,
    val ratePerLiter: Double,
    val totalPayment: Double,
    val cowId: Long = 0,      // 0 = general / no specific cow
    val session: String = "Morning" // Morning / Evening
)
