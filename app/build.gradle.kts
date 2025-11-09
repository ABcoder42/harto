plugins {

    id("com.android.application")
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.harto"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.harto"
        minSdk = 24
        targetSdk = 36
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

    implementation("com.google.android.material:material:1.10.0")
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database:20.3.0")
    implementation("androidx.security:security-crypto:1.1.0")
// for Realtime
    implementation("com.google.firebase:firebase-firestore:25.1.0")

    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
// for Firestore
    implementation("com.google.firebase:firebase-auth:22.3.0") // <-- Add this line

    implementation("com.android.billingclient:billing:6.0.1")

    // Country Code Picker
    implementation("com.hbb20:ccp:2.7.3")

    // AndroidX libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.wearable)
    implementation(libs.firebase.database)
    implementation(libs.core.splashscreen)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


}

