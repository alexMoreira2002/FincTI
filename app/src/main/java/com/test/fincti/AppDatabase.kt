package com.test.fincti

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database class for Room persistence library.
 *
 * This class defines the database configuration and serves as the main access point
 * for the underlying SQLite database. It contains the entities and DAOs (Data Access Objects)
 * that interact with the database.
 */
@Database(
    version = 1, // Database version. Increment this value and define migrations when making schema changes.
    entities = [Transaction::class], // List of entities that are part of the database.
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provides access to the transaction data access object (DAO).
     * DAOs are responsible for defining the methods used to interact with the database.
     */
    abstract fun transactionDao(): TransactionDao

    companion object {
        // Singleton instance of the database to prevent multiple instances at the same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Gets the singleton instance of the database.
         *
         * @param context Application context.
         * @return The AppDatabase instance.
         */
        fun getDatabase(context: Context): AppDatabase {
            // If the INSTANCE is not null, then return it.
            // If it is null, then create the database instance.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "transactions_database" // Database name
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
