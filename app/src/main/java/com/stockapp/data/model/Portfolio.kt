package com.stockapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.stockapp.data.local.converters.DateConverter
import com.stockapp.data.local.converters.DoubleConverter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Entity(
    tableName = "holdings",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("symbol")]
)
@Serializable
data class Holding(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val symbol: String,
    val name: String,
    val isin: String,
    val quantity: Int,
    val averagePrice: Double,
    val currentPrice: Double = 0.0,
    val dayChange: Double = 0.0,
    val dayChangePercent: Double = 0.0,
    val totalValue: Double = 0.0,
    val totalGainLoss: Double = 0.0,
    val totalGainLossPercent: Double = 0.0,
    val sector: String? = null,
    val marketCap: String? = null,
    val exchange: String = "NSE",
    val lastUpdated: LocalDateTime = LocalDateTime.now()
) {
    val isProfit: Boolean
        get() = totalGainLoss > 0

    val formattedGainLoss: String
        get() = if (totalGainLoss >= 0) "+₹${String.format("%.2f", totalGainLoss)}" else "₹${String.format("%.2f", totalGainLoss)}"

    val formattedGainLossPercent: String
        get() = if (totalGainLossPercent >= 0) "+${String.format("%.2f", totalGainLossPercent)}%" else "${String.format("%.2f", totalGainLossPercent)}%"
}

@Entity(tableName = "portfolio_summary")
@Serializable
data class PortfolioSummary(
    @PrimaryKey val userId: Long,
    val totalInvested: Double = 0.0,
    val currentValue: Double = 0.0,
    val totalGainLoss: Double = 0.0,
    val totalGainLossPercent: Double = 0.0,
    val dayChange: Double = 0.0,
    val dayChangePercent: Double = 0.0,
    val topGainer: String? = null,
    val topLoser: String? = null,
    val lastSynced: LocalDateTime = LocalDateTime.now()
)

@Entity(tableName = "transactions")
@Serializable
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val symbol: String,
    val name: String,
    val type: TransactionType,
    val quantity: Int,
    val price: Double,
    val totalAmount: Double,
    val charges: Double = 0.0,
    val date: LocalDate,
    val exchange: String = "NSE",
    val orderId: String? = null
)

enum class TransactionType(@Serializable(with = TransactionTypeSerializer::class) val value: String) {
    BUY("BUY"),
    SELL("SELL"),
    DIVIDEND("DIVIDEND"),
    BONUS("BONUS"),
    SPLIT("SPLIT"),
    MERGER("MERGER");

    companion object {
        fun fromString(value: String): TransactionType = values().first { it.value == value }
    }
}

class TransactionTypeSerializer : kotlinx.serialization.KSerializer<TransactionType> {
    override val descriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor("TransactionType", kotlinx.serialization.descriptors.PrimitiveKind.STRING)

    override fun serialize(encoder: kotlinx.serialization.Encoding, value: TransactionType) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: kotlinx.serialization.Decoding): TransactionType {
        return TransactionType.fromString(decoder.decodeString())
    }
}