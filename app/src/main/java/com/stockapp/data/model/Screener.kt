package com.stockapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.stockapp.data.local.converters.DateConverter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Entity(tableName = "screeners")
@Serializable
data class Screener(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val category: ScreenerCategory,
    val criteria: String, // JSON string for criteria
    val isDefault: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class ScreenerCategory(@Serializable(with = ScreenerCategorySerializer::class) val value: String) {
    TECHNICAL("TECHNICAL"),
    FUNDAMENTAL("FUNDAMENTAL"),
    MOMENTUM("MOMENTUM"),
    VALUE("VALUE"),
    GROWTH("GROWTH"),
    DIVIDEND("DIVIDEND"),
    CUSTOM("CUSTOM");

    companion object {
        fun fromString(value: String): ScreenerCategory = values().firstOrNull { it.value == value } ?: CUSTOM
    }
}

class ScreenerCategorySerializer : kotlinx.serialization.KSerializer<ScreenerCategory> {
    override val descriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor("ScreenerCategory", kotlinx.serialization.descriptors.PrimitiveKind.STRING)

    override fun serialize(encoder: kotlinx.serialization.Encoding, value: ScreenerCategory) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: kotlinx.serialization.Decoding): ScreenerCategory {
        return ScreenerCategory.fromString(decoder.decodeString())
    }
}

@Entity(tableName = "screener_results")
@Serializable
data class ScreenerResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val screenerId: Long,
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val changePercent: Double,
    val volume: Long,
    val marketCap: Double,
    val peRatio: Double?,
    val pbRatio: Double?,
    val roe: Double?,
    val debtToEquity: Double?,
    val score: Double, // Screener match score
    val matchedCriteria: List<String> = emptyList(),
    val scannedAt: LocalDateTime = LocalDateTime.now()
)

@Entity(tableName = "weekly_stock_picks")
@Serializable
data class WeeklyStockPick(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weekStartDate: String, // YYYY-MM-DD
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val targetPrice: Double,
    val stopLoss: Double,
    val reason: String,
    val riskLevel: RiskLevel,
    val timeHorizon: TimeHorizon,
    val screenerId: Long?,
    val aiGenerated: Boolean = false,
    val confidence: Double = 0.0,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class RiskLevel(@Serializable(with = RiskLevelSerializer::class) val value: String) {
    LOW("LOW"),
    MEDIUM("MEDIUM"),
    HIGH("HIGH");

    companion object {
        fun fromString(value: String): RiskLevel = values().firstOrNull { it.value == value } ?: MEDIUM
    }
}

class RiskLevelSerializer : kotlinx.serialization.KSerializer<RiskLevel> {
    override val descriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor("RiskLevel", kotlinx.serialization.descriptors.PrimitiveKind.STRING)

    override fun serialize(encoder: kotlinx.serialization.Encoding, value: RiskLevel) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: kotlinx.serialization.Decoding): RiskLevel {
        return RiskLevel.fromString(decoder.decodeString())
    }
}

enum class TimeHorizon(@Serializable(with = TimeHorizonSerializer::class) val value: String) {
    SHORT_TERM("SHORT_TERM"), // 1-4 weeks
    MEDIUM_TERM("MEDIUM_TERM"), // 1-3 months
    LONG_TERM("LONG_TERM"); // 3+ months

    companion object {
        fun fromString(value: String): TimeHorizon = values().firstOrNull { it.value == value } ?: MEDIUM_TERM
    }
}

class TimeHorizonSerializer : kotlinx.serialization.KSerializer<TimeHorizon> {
    override val descriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor("TimeHorizon", kotlinx.serialization.descriptors.PrimitiveKind.STRING)

    override fun serialize(encoder: kotlinx.serialization.Encoding, value: TimeHorizon) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: kotlinx.serialization.Decoding): TimeHorizon {
        return TimeHorizon.fromString(decoder.decodeString())
    }
}

@Entity(tableName = "ai_analysis")
@Serializable
data class AIAnalysis(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: AIAnalysisType,
    val symbol: String?,
    val userId: Long?,
    val query: String,
    val response: String,
    val confidence: Double = 0.0,
    val tokensUsed: Int = 0,
    val model: String = "gemini-pro",
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class AIAnalysisType(@Serializable(with = AIAnalysisTypeSerializer::class) val value: String) {
    PORTFOLIO_REVIEW("PORTFOLIO_REVIEW"),
    STOCK_ANALYSIS("STOCK_ANALYSIS"),
    NEWS_SUMMARY("NEWS_SUMMARY"),
    SCREENER_EXPLANATION("SCREENER_EXPLANATION"),
    WEEKLY_SUMMARY("WEEKLY_SUMMARY"),
    IPO_ANALYSIS("IPO_ANALYSIS"),
    GENERAL_QUERY("GENERAL_QUERY");

    companion object {
        fun fromString(value: String): AIAnalysisType = values().firstOrNull { it.value == value } ?: GENERAL_QUERY
    }
}

class AIAnalysisTypeSerializer : kotlinx.serialization.KSerializer<AIAnalysisType> {
    override val descriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor("AIAnalysisType", kotlinx.serialization.descriptors.PrimitiveKind.STRING)

    override fun serialize(encoder: kotlinx.serialization.Encoding, value: AIAnalysisType) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: kotlinx.serialization.Decoding): AIAnalysisType {
        return AIAnalysisType.fromString(decoder.decodeString())
    }
}