package com.ksheera.sagara.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ksheera.sagara.data.dao.CowDao
import com.ksheera.sagara.data.dao.ExpenseEntryDao
import com.ksheera.sagara.data.dao.MilkEntryDao
import com.ksheera.sagara.data.entity.Cow
import com.ksheera.sagara.data.entity.ExpenseEntry
import com.ksheera.sagara.data.entity.MilkEntry

@Database(
    entities = [MilkEntry::class, ExpenseEntry::class, Cow::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun milkEntryDao(): MilkEntryDao
    abstract fun expenseEntryDao(): ExpenseEntryDao
    abstract fun cowDao(): CowDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ksheera_sagara_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
