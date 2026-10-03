package com.stockapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.stockapp.data.local.converters.DateConverter
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Entity(tableName = "users")
@Serializable
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val panNumber: String, // Encrypted
    val dematAccountNumber: String,
    val brokerageId: String, // e.g., "zerodha", "groww", "upstox", "angelone"
    val brokerageUserId: String, // User ID for the brokerage API
    val brokerageApiKey: String? = null, // Encrypted API key
    val brokerageApiSecret: String? = null, // Encrypted API secret
    val isPrimary: Boolean = false,
    val createdAt: LocalDate = LocalDate.now(),
    val updatedAt: LocalDate = LocalDate.now()
) {
    companion object {
        const val PAN_MASK_PATTERN = "*****"
    }
}