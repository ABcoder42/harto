plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.harto"
    compileSdk = 34 // ✅ latest stable SDK as of 2025

    defaultConfig {
        applicationId = "com.example.harto"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // ✅ Core Wear OS libraries (modern)
    implementation("androidx.wear:wear:1.3.0")
    implementation("androidx.wear.tiles:tiles-material:1.2.0")
    implementation("androidx.wear.watchface:watchface:1.2.1")

    // ✅ Play Services for Wearable (DataClient, etc.)
    implementation(libs.play.services.wearable)

    // ✅ Legacy wearable APIs for compatibility (compileOnly — not bundled)
    compileOnly("com.google.android.wearable:wearable:2.9.0")

    // ✅ UI + AndroidX dependencies
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
}
