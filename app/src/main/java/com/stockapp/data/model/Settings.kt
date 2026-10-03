package com.stockapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "app_settings")
@Serializable
data class AppSettings(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val KEY_LANGUAGE = "language"
        const val KEY_THEME = "theme"
        const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val KEY_NEWS_NOTIFICATIONS = "news_notifications"
        const val KEY_IPO_NOTIFICATIONS = "ipo_notifications"
        const val KEY_PRICE_ALERTS = "price_alerts"
        const val KEY_AUTO_SYNC = "auto_sync"
        const val KEY_SYNC_INTERVAL = "sync_interval"
        const val KEY_DATA_SAVER = "data_saver"
        const val KEY_CURRENCY = "currency"
        const val KEY_DEFAULT_BROKERAGE = "default_brokerage"
        const val KEY_AI_ENABLED = "ai_enabled"
        const val KEY_AI_MODEL = "ai_model"
        const val KEY_WEEKEND_SUMMARY = "weekend_summary"
        const val KEY_DAILY_RECAP = "daily_recap"
    }
}

enum class Language(@Serializable(with = LanguageSerializer::class) val code: String, val name: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिंदी");

    companion object {
        fun fromCode(code: String): Language = values().firstOrNull { it.code == code } ?: ENGLISH
    }
}

class LanguageSerializer : kotlinx.serialization.KSerializer<Language> {
    override val descriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor("Language", kotlinx.serialization.descriptors.PrimitiveKind.STRING)

    override fun serialize(encoder: kotlinx.serialization.Encoding, value: Language) {
        encoder.encodeString(value.code)
    }

    override fun deserialize(decoder: kotlinx.serialization.Decoding): Language {
        return Language.fromCode(decoder.decodeString())
    }
}

enum class Theme(@Serializable(with = ThemeSerializer::class) val value: String) {
    LIGHT("LIGHT"),
    DARK("DARK"),
    SYSTEM("SYSTEM");

    companion object {
        fun fromString(value: String): Theme = values().firstOrNull { it.value == value } ?: SYSTEM
    }
}

class ThemeSerializer : kotlinx.serialization.KSerializer<Theme> {
    override val descriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor("Theme", kotlinx.serialization.descriptors.PrimitiveKind.STRING)

    override fun serialize(encoder: kotlinx.serialization.Encoding, value: Theme) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: kotlinx.serialization.Decoding): Theme {
        return Theme.fromString(decoder.decodeString())
    }
}

enum class Brokerage(@Serializable(with = BrokerageSerializer::class) val id: String, val name: String, val logoUrl: String, val apiBaseUrl: String) {
    ZERODHA("zerodha", "Zerodha", "https://zerodha.com/favicon.ico", "https://api.zerodha.com"),
    GROWW("groww", "Groww", "https://groww.in/favicon.ico", "https://api.groww.in"),
    UPSTOX("upstox", "Upstox", "https://upstox.com/favicon.ico", "https://api.upstox.com"),
    ANGELONE("angelone", "Angel One", "https://angelone.in/favicon.ico", "https://api.angelone.in"),
    ICICI_DIRECT("icici_direct", "ICICI Direct", "https://icicidirect.com/favicon.ico", "https://api.icicidirect.com"),
    HDFC_SEC("hdfc_sec", "HDFC Securities", "https://hdfcsec.com/favicon.ico", "https://api.hdfcsec.com"),
    KOTAK_SEC("kotak_sec", "Kotak Securities", "https://kotaksecurities.com/favicon.ico", "https://api.kotaksecurities.com"),
    MOTILAL_OSWAL("motilal_oswal", "Motilal Oswal", "https://motilaloswal.com/favicon.ico", "https://api.motilaloswal.com"),
    SHAREKHAN("sharekhan", "Sharekhan", "https://sharekhan.com/favicon.ico", "https://api.sharekhan.com"),
    IIFL("iifl", "IIFL Securities", "https://iifl.com/favicon.ico", "https://api.iifl.com");

    companion object {
        fun fromId(id: String): Brokerage = values().firstOrNull { it.id == id } ?: ZERODHA
        val all: List<Brokerage> = values().toList()
    }
}

class BrokerageSerializer : kotlinx.serialization.KSerializer<Brokerage> {
    override val descriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor("Brokerage", kotlinx.serialization.descriptors.PrimitiveKind.STRING)

    override fun serialize(encoder: kotlinx.serialization.Encoding, value: Brokerage) {
        encoder.encodeString(value.id)
    }

    override fun deserialize(decoder: kotlinx.serialization.Decoding): Brokerage {
        return Brokerage.fromId(decoder.decodeString())
    }
}

@Entity(tableName = "brokerage_credentials")
@Serializable
data class BrokerageCredentials(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val brokerageId: String,
    val apiKey: String, // Encrypted
    val apiSecret: String, // Encrypted
    val accessToken: String? = null, // Encrypted
    val refreshToken: String? = null, // Encrypted
    val tokenExpiry: Long = 0,
    val isActive: Boolean = true,
    val lastSynced: Long = 0
)