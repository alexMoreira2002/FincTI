package com.test.fincti

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update

@Dao
interface TransactionDao {
    @Query("SELECT * from transactions")
    fun getAll(): List<Transaction>

    @Insert
    fun insertAll(vararg transaction: Transaction)

    @Delete
    fun delete(transaction: Transaction)

    @Update
    fun update(vararg transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE type = :type AND category = :category")
    fun getAllByTypeAndCategory(type: String, category: String): List<Transaction>

    @Query("SELECT * FROM transactions WHERE type = :type")
    fun getAllByType(type: String): List<Transaction>

    @Query("SELECT * FROM transactions WHERE category = :category")
    fun getAllByCategory(category: String): List<Transaction>
}