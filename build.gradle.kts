plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.example.questcontrol"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.questcontrol"
        minSdk = 29
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    packaging { resources.excludes += "META-INF/versions/**" }
}
dependencies {
    // ADB protocol client (pairing + connect + shell). Check for newer versions.
    implementation("io.github.muntashirakon:libadb-android:3.0.0")
    implementation("org.conscrypt:conscrypt-android:2.5.3")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.78.1")
}
