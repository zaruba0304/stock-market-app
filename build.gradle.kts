buildscript {
    ext {
        compose_version = "1.6.10"
        kotlin_version = "2.0.0"
        hilt_version = "2.48"
        room_version = "2.6.1"
        retrofit_version = "2.11.0"
        okhttp_version = "4.12.0"
        coroutines_version = "1.8.0"
        lifecycle_version = "2.7.0"
        navigation_version = "2.7.7"
        workmanager_version = "2.9.0"
        material_version = "1.12.0"
        coil_version = "2.6.0"
        serialization_version = "1.6.3"
        datastore_version = "1.1.1"
        paging_version = "3.2.1"
    }
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.5.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.48")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}