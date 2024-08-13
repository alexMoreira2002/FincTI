package com.test.fincti

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update

/**
 * Data Access Object (DAO) interface for accessing transaction data from the database.
 * Provides methods to perform CRUD operations and specific queries related to transactions.
 */
@Dao
interface TransactionDao {

    /**
     * Retrieves all transactions from the database.
     *
     * @return A list of all transactions.
     */
    @Query("SELECT * FROM transactions")
    fun getAll(): List<Transaction>

    /**
     * Inserts one or more transactions into the database.
     *
     * @param transaction One or more Transaction objects to be inserted.
     */
    @Insert
    fun insertAll(vararg transaction: Transaction)

    /**
     * Deletes a specific transaction from the database.
     *
     * @param transaction The Transaction object to be deleted.
     */
    @Delete
    fun delete(transaction: Transaction)

    /**
     * Updates one or more transactions in the database.
     *
     * @param transaction One or more Transaction objects to be updated.
     */
    @Update
    fun update(vararg transaction: Transaction)

    /**
     * Retrieves all transactions that match a specific type and category.
     *
     * @param type The type of transaction (e.g., "0" for expense, "1" for income).
     * @param category The category of the transactions (e.g., "Food", "Entertainment").
     * @return A list of transactions that match the given type and category.
     */
    @Query("SELECT * FROM transactions WHERE type = :type AND category = :category")
    fun getAllByTypeAndCategory(type: String, category: String): List<Transaction>

    /**
     * Retrieves all transactions of a specific type.
     *
     * @param type The type of transaction (e.g., "0" for expense, "1" for income).
     * @return A list of transactions that match the given type.
     */
    @Query("SELECT * FROM transactions WHERE type = :type")
    fun getAllByType(type: String): List<Transaction>

    /**
     * Retrieves all transactions of a specific category.
     *
     * @param category The category of the transactions (e.g., "Food", "Entertainment").
     * @return A list of transactions that match the given category.
     */
    @Query("SELECT * FROM transactions WHERE category = :category")
    fun getAllByCategory(category: String): List<Transaction>
}
