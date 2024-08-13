package com.test.fincti

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val label: String,
    val amount: Double,
    val description: String,
    val type: String,
    val category: String,
    val photo: ByteArray? // Add a field for the image as a ByteArray
) : Serializable {
}