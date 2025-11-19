// android-app/build.gradle.kts (Project Level)
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Connects Firebase services
        classpath("com.google.gms:google-services:4.4.0")
    }
}

plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}