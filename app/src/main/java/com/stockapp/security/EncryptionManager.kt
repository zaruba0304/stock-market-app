package com.stockapp.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.io.IOException
import java.security.GeneralSecurityException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionManager @Inject constructor(
    private val context: Context
) {

    private val masterKeyAlias: String by lazy {
        try {
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        } catch (e: GeneralSecurityException) {
            throw RuntimeException("Failed to create master key", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to create master key", e)
        }
    }

    private val encryptedSharedPrefs by lazy {
        try {
            EncryptedSharedPreferences.create(
                "secure_prefs",
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: GeneralSecurityException) {
            throw RuntimeException("Failed to create encrypted shared preferences", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to create encrypted shared preferences", e)
        }
    }

    // EncryptedSharedPreferences methods
    fun putString(key: String, value: String) {
        encryptedSharedPrefs.edit().putString(key, value).apply()
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return encryptedSharedPrefs.getString(key, defaultValue)
    }

    fun putBoolean(key: String, value: Boolean) {
        encryptedSharedPrefs.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return encryptedSharedPrefs.getBoolean(key, defaultValue)
    }

    fun putLong(key: String, value: Long) {
        encryptedSharedPrefs.edit().putLong(key, value).apply()
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return encryptedSharedPrefs.getLong(key, defaultValue)
    }

    fun remove(key: String) {
        encryptedSharedPrefs.edit().remove(key).apply()
    }

    fun clear() {
        encryptedSharedPrefs.edit().clear().apply()
    }

    fun contains(key: String): Boolean {
        return encryptedSharedPrefs.contains(key)
    }

    // AES-GCM encryption for larger data
    private val aesKey: SecretKey by lazy {
        generateAESKey()
    }

    private fun generateAESKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val spec = KeyGenParameterSpec.Builder(
            "aes_key",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).apply {
            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            setKeySize(256)
            setUserAuthenticationRequired(false)
        }.build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    fun encrypt(data: String): EncryptedData {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, aesKey)
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        return EncryptedData(encryptedBytes, iv)
    }

    fun decrypt(encryptedData: EncryptedData): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, encryptedData.iv)
        cipher.init(Cipher.DECRYPT_MODE, aesKey, spec)
        val decryptedBytes = cipher.doFinal(encryptedData.encryptedData)
        return String(decryptedBytes)
    }

    data class EncryptedData(
        val encryptedData: ByteArray,
        val iv: ByteArray
    )

    // Convenience methods for PAN encryption
    fun encryptPAN(pan: String): String {
        return encrypt(pan).let { encrypted ->
            android.util.Base64.encodeToString(encrypted.encryptedData, android.util.Base64.DEFAULT) + "|" +
            android.util.Base64.encodeToString(encrypted.iv, android.util.Base64.DEFAULT)
        }
    }

    fun decryptPAN(encryptedPan: String): String {
        val parts = encryptedPan.split("|")
        if (parts.size != 2) return ""
        val encryptedData = android.util.Base64.decode(parts[0], android.util.Base64.DEFAULT)
        val iv = android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT)
        return decrypt(EncryptedData(encryptedData, iv))
    }

    // Convenience methods for API credentials
    fun encryptAPICredentials(apiKey: String, apiSecret: String): EncryptedCredentials {
        return EncryptedCredentials(
            encrypt(apiKey),
            encrypt(apiSecret)
        )
    }

    fun decryptAPICredentials(encrypted: EncryptedCredentials): Pair<String, String> {
        return decrypt(encrypted.apiKey) to decrypt(encrypted.apiSecret)
    }

    data class EncryptedCredentials(
        val apiKey: EncryptedData,
        val apiSecret: EncryptedData
    )
}