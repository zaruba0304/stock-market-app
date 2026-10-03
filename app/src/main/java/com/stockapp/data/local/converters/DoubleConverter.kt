package com.stockapp.data.local.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.json.Json

class DoubleConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromDoubleList(value: List<Double>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toDoubleList(value: String?): List<Double>? {
        return value?.let { gson.fromJson(it, TypeToken.getParameterized(List::class.java, Double::class.java).type) }
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let { gson.fromJson(it, TypeToken.getParameterized(List::class.java, String::class.java).type) }
    }

    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String>? {
        return value?.let { gson.fromJson(it, TypeToken.getParameterized(Map::class.java, String::class.java, String::class.java).type) }
    }
}