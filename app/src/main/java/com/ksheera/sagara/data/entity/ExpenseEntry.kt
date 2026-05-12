package com.ksheera.sagara.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_entries")
data class ExpenseEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,
    val category: String,   // FODDER, MEDICAL, LABOR
    val description: String,
    val amount: Double,
    val cowId: Long = 0     // 0 = general farm expense
)
