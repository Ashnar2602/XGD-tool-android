plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.xgdtool.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.xgdtool.android"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }

    // libxgdtool.so is pre-built (cross-compiled from the XGDTool C++ core,
    // see ../../XGDTool/android) and placed directly under src/main/jniLibs.
    // No in-project native build step is wired up here.
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-service:2.8.4")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("androidx.activity:activity-ktx:1.9.1")
}
