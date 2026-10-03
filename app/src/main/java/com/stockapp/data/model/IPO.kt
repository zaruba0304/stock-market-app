package com.stockapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.stockapp.data.local.converters.DateConverter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Entity(
    tableName = "ipos",
    indices = [Index("symbol"), Index("status"), Index("openDate"), Index("closeDate")]
)
@Serializable
data class IPO(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val name: String,
    val isin: String,
    val priceBandMin: Double,
    val priceBandMax: Double,
    val lotSize: Int,
    val issueSize: Double,
    val openDate: LocalDate,
    val closeDate: LocalDate,
    val listingDate: LocalDate?,
    val status: IPOStatus,
    val registrar: String?,
    val gmp: Double? = null, // Grey Market Premium
    val gmpPercent: Double? = null,
    val subscriptionData: String? = null, // JSON string for subscription details
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    val formattedPriceBand: String
        get() = "₹${String.format("%.0f", priceBandMin)} - ₹${String.format("%.0f", priceBandMax)}"

    val daysToClose: Int
        get() = closeDate.difference(LocalDate.now()).days

    val isOpen: Boolean
        get() = status == IPOStatus.OPEN || status == IPOStatus.UPCOMING

    val formattedGMP: String?
        get() = gmp?.let { "₹${String.format("%.2f", it)} (${String.format("%.2f", gmpPercent ?: 0.0)}%)" }
}

enum class IPOStatus(@Serializable(with = IPOStatusSerializer::class) val value: String) {
    UPCOMING("UPCOMING"),
    OPEN("OPEN"),
    CLOSED("CLOSED"),
    ALLOTMENT("ALLOTMENT"),
    LISTED("LISTED"),
    CANCELLED("CANCELLED");

    companion object {
        fun fromString(value: String): IPOStatus = values().firstOrNull { it.value == value } ?: UPCOMING
    }
}

class IPOStatusSerializer : kotlinx.serialization.KSerializer<IPOStatus> {
    override val descriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor("IPOStatus", kotlinx.serialization.descriptors.PrimitiveKind.STRING)

    override fun serialize(encoder: kotlinx.serialization.Encoding, value: IPOStatus) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: kotlinx.serialization.Decoding): IPOStatus {
        return IPOStatus.fromString(decoder.decodeString())
    }
}

@Entity(
    tableName = "ipo_applications",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = IPO::class, parentColumns = ["id"], childColumns = ["ipoId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("userId"), Index("ipoId"), Index(value = ["userId", "ipoId"], unique = true)]
)
@Serializable
data class IPOApplication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val ipoId: Long,
    val lotsApplied: Int,
    val price: Double,
    val amount: Double,
    val applicationNumber: String?,
    val status: ApplicationStatus,
    val allotmentStatus: AllotmentStatus? = null,
    val allottedShares: Int = 0,
    val refundAmount: Double = 0.0,
    val appliedAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class ApplicationStatus(@Serializable(with = ApplicationStatusSerializer::class) val value: String) {
    PENDING("PENDING"),
    SUBMITTED("SUBMITTED"),
    CONFIRMED("CONFIRMED"),
    REJECTED("REJECTED"),
    CANCELLED("CANCELLED");

    companion object {
        fun fromString(value: String): ApplicationStatus = values().firstOrNull { it.value == value } ?: PENDING
    }
}

class ApplicationStatusSerializer : kotlinx.serialization.KSerializer<ApplicationStatus> {
    override val descriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor("ApplicationStatus", kotlinx.serialization.descriptors.PrimitiveKind.STRING)

    override fun serialize(encoder: kotlinx.serialization.Encoding, value: ApplicationStatus) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: kotlinx.serialization.Decoding): ApplicationStatus {
        return ApplicationStatus.fromString(decoder.decodeString())
    }
}

enum class AllotmentStatus(@Serializable(with = AllotmentStatusSerializer::class) val value: String) {
    PENDING("PENDING"),
    ALLOTTED("ALLOTTED"),
    NOT_ALLOTTED("NOT_ALLOTTED"),
    PARTIAL("PARTIAL");

    companion object {
        fun fromString(value: String): AllotmentStatus = values().firstOrNull { it.value == value } ?: PENDING
    }
}

class AllotmentStatusSerializer : kotlinx.serialization.KSerializer<AllotmentStatus> {
    override val descriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor("AllotmentStatus", kotlinx.serialization.descriptors.PrimitiveKind.STRING)

    override fun serialize(encoder: kotlinx.serialization.Encoding, value: AllotmentStatus) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: kotlinx.serialization.Decoding): AllotmentStatus {
        return AllotmentStatus.fromString(decoder.decodeString())
    }
}

@Entity(tableName = "ipo_watchlist")
@Serializable
data class IPOWatchlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val ipoId: Long,
    val addedAt: LocalDateTime = LocalDateTime.now()
)