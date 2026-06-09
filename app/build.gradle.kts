plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.Blackbox.muslim"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.Blackbox.muslim"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

kotlinOptions {
    jvmTarget = "17"
    freeCompilerArgs += listOf(
        "-opt-in=kotlin.contracts.ExperimentalContracts",
        "-Xjvm-default=all-compatibility"
    )
}
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.0")
    implementation("com.batoulapps.adhan:adhan:1.2.1")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("androidx.biometric:biometric:1.1.0")
}
