plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.tvlive"
    compileSdk = 34

    hilt {
        enableAggregatingTask = false
    }

    defaultConfig {
        applicationId = "com.tvlive"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    
    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.navigation.compose)
    
    // Debug
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
    
    // Room
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)
    
    // ExoPlayer
    implementation(libs.bundles.media3)
    
    // Retrofit & OkHttp
    implementation(libs.bundles.retrofit)
    
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    
    // TV Support
    implementation(libs.leanback)
    implementation(libs.leanback.preference)
    
    // Image Loading
    implementation(libs.glide.compose)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
}
