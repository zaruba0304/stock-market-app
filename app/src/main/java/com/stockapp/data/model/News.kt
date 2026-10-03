package com.stockapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.stockapp.data.local.converters.DateConverter
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Entity(
    tableName = "news",
    indices = [Index("symbol"), Index("publishedAt"), Index("impactScore"), Index("isRead")]
)
@Serializable
data class NewsArticle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val summary: String,
    val content: String?,
    val url: String,
    val imageUrl: String?,
    val source: String,
    val author: String?,
    val publishedAt: LocalDateTime,
    val symbols: List<String> = emptyList(), // Related stock symbols
    val categories: List<String> = emptyList(), // e.g., "earnings", "merger", "dividend", "ipo", "market"
    val sentiment: NewsSentiment = NewsSentiment.NEUTRAL,
    val impactScore: Double = 0.0, // 0-100, higher = more impactful
    val impactLevel: ImpactLevel = ImpactLevel.LOW,
    val isRead: Boolean = false,
    val isBookmarked: Boolean = false,
    val language: String = "en",
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    val formattedPublishedAt: String
        get() = publishedAt.toString()

    val relatedSymbols: String
        get() = symbols.joinToString(", ")
}

enum class NewsSentiment(@Serializable(with = NewsSentimentSerializer::class) val value: String) {
    POSITIVE("POSITIVE"),
    NEGATIVE("NEGATIVE"),
    NEUTRAL("NEUTRAL");

    companion object {
        fun fromString(value: String): NewsSentiment = values().firstOrNull { it.value == value } ?: NEUTRAL
    }
}

class NewsSentimentSerializer : kotlinx.serialization.KSerializer<NewsSentiment> {
    override val descriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor("NewsSentiment", kotlinx.serialization.descriptors.PrimitiveKind.STRING)

    override fun serialize(encoder: kotlinx.serialization.Encoding, value: NewsSentiment) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: kotlinx.serialization.Decoding): NewsSentiment {
        return NewsSentiment.fromString(decoder.decodeString())
    }
}

enum class ImpactLevel(@Serializable(with = ImpactLevelSerializer::class) val value: String) {
    LOW("LOW"),
    MEDIUM("MEDIUM"),
    HIGH("HIGH"),
    CRITICAL("CRITICAL");

    companion object {
        fun fromScore(score: Double): ImpactLevel = when {
            score >= 80 -> CRITICAL
            score >= 60 -> HIGH
            score >= 30 -> MEDIUM
            else -> LOW
        }
    }
}

class ImpactLevelSerializer : kotlinx.serialization.KSerializer<ImpactLevel> {
    override val descriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor("ImpactLevel", kotlinx.serialization.descriptors.PrimitiveKind.STRING)

    override fun serialize(encoder: kotlinx.serialization.Encoding, value: ImpactLevel) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: kotlinx.serialization.Decoding): ImpactLevel {
        return ImpactLevel.values().firstOrNull { it.value == decoder.decodeString() } ?: LOW
    }
}

@Entity(
    tableName = "news_preferences",
    foreignKeys = [ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)]
)
@Serializable
data class NewsPreferences(
    @PrimaryKey val userId: Long,
    val enabledCategories: List<String> = listOf("earnings", "dividend", "merger", "ipo", "market", "regulatory"),
    val minImpactScore: Double = 30.0,
    val enableNotifications: Boolean = true,
    val notificationTime: String = "18:00", // Evening recap time
    val weekendSummaryEnabled: Boolean = true,
    val weekendSummaryTime: String = "10:00",
    val language: String = "en"
)

@Entity(tableName = "daily_news_recap")
@Serializable
data class DailyNewsRecap(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val summary: String,
    val topNews: List<Long> = emptyList(), // News article IDs
    val marketSummary: String,
    val portfolioImpact: String,
    val generatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity(tableName = "weekly_portfolio_summary")
@Serializable
data class WeeklyPortfolioSummary(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weekStartDate: String, // YYYY-MM-DD
    val weekEndDate: String, // YYYY-MM-DD
    val userId: Long,
    val summary: String,
    val totalGainLoss: Double,
    val totalGainLossPercent: Double,
    val topPerformers: List<String> = emptyList(),
    val worstPerformers: List<String> = emptyList(),
    val keyEvents: List<String> = emptyList(),
    val aiInsights: String?,
    val generatedAt: LocalDateTime = LocalDateTime.now()
)