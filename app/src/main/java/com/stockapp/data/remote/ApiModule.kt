package com.stockapp.data.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.stockapp.data.local.converters.DateConverter
import com.stockapp.data.model.LocalDate
import com.stockapp.data.model.LocalDateTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.DateTimeFormatter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiModule @Inject constructor() {

    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, DateConverter())
        .registerTypeAdapter(LocalDateTime::class.java, DateTimeConverter())
        .setLenient()
        .create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    class DateConverter : com.google.gson.JsonDeserializer<LocalDate>, com.google.gson.JsonSerializer<LocalDate> {
        private val formatter = DateTimeFormatter.ISO_DATE

        override fun deserialize(json: com.google.gson.JsonElement, typeOfT: java.lang.reflect.Type, context: com.google.gson.JsonDeserializationContext): LocalDate {
            return LocalDate.parse(json.asString, formatter)
        }

        override fun serialize(src: LocalDate, typeOfSrc: java.lang.reflect.Type, context: com.google.gson.JsonSerializationContext): com.google.gson.JsonElement {
            return com.google.gson.JsonPrimitive(src.format(formatter))
        }
    }

    class DateTimeConverter : com.google.gson.JsonDeserializer<LocalDateTime>, com.google.gson.JsonSerializer<LocalDateTime> {
        private val formatter = DateTimeFormatter.ISO_DATE_TIME

        override fun deserialize(json: com.google.gson.JsonElement, typeOfT: java.lang.reflect.Type, context: com.google.gson.JsonDeserializationContext): LocalDateTime {
            return LocalDateTime.parse(json.asString, formatter)
        }

        override fun serialize(src: LocalDateTime, typeOfSrc: java.lang.reflect.Type, context: com.google.gson.JsonSerializationContext): com.google.gson.JsonElement {
            return com.google.gson.JsonPrimitive(src.format(formatter))
        }
    }
}