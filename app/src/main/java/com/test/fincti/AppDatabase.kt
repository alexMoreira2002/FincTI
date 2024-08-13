package com.test.fincti

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    version = 1,
    entities = [Transaction::class],
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

}