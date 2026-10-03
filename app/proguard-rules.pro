# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp { *; }
-keep class * extends dagger.hilt.android.HiltApplication { *; }

# Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.Entity { *; }
-keep class * extends androidx.room.Dao { *; }
-keep class * extends androidx.room.TypeConverter { *; }

# Retrofit
-keep class * extends com.squareup.retrofit2.Retrofit { *; }
-keep class * extends com.squareup.retrofit2.Converter { *; }
-keep class * extends com.squareup.retrofit2.CallAdapter { *; }

# OkHttp
-keep class * extends okhttp3.OkHttpClient { *; }
-keep class * extends okhttp3.Interceptor { *; }

# Gson
-keep class * extends com.google.gson.Gson { *; }
-keep class * extends com.google.gson.TypeAdapter { *; }

# Kotlin Coroutines
-keep class * extends kotlinx.coroutines.CoroutineScope { *; }
-keep class * extends kotlinx.coroutines.Job { *; }

# WorkManager
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }

# DataStore
-keep class * extends androidx.datastore.preferences.Preferences { *; }

# Biometric
-keep class * extends androidx.biometric.BiometricPrompt { *; }

# Security Crypto
-keep class * extends androidx.security.crypto.EncryptedSharedPreferences { *; }
-keep class * extends androidx.security.crypto.MasterKeys { *; }

# Coil
-keep class * extends coil.ImageLoader { *; }
-keep class * extends coil.request.ImageRequest { *; }

# Navigation
-keep class * extends androidx.navigation.NavController { *; }
-keep class * extends androidx.navigation.NavHost { *; }

# Compose
-keep class * extends androidx.compose.runtime.Composable { *; }
-keep class * extends androidx.compose.ui.Modifier { *; }

# Keep all model classes
-keep class com.stockapp.data.model.** { *; }

# Keep all repository classes
-keep class com.stockapp.data.repository.** { *; }

# Keep all ViewModel classes
-keep class com.stockapp.ui.screen.** { *; }

# Keep all worker classes
-keep class com.stockapp.worker.** { *; }

# Keep all notification classes
-keep class com.stockapp.notification.** { *; }

# Keep all security classes
-keep class com.stockapp.security.** { *; }

# Keep all util classes
-keep class com.stockapp.util.** { *; }

# Keep all di classes
-keep class com.stockapp.di.** { *; }

# Keep all local data classes
-keep class com.stockapp.data.local.** { *; }

# Keep all remote data classes
-keep class com.stockapp.data.remote.** { *; }