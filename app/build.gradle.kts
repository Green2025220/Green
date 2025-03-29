plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services) // 確保已經引入 google-services 插件
}

android {
    namespace = "tw.edu.pu.csim.s1114702.green"
    compileSdk = 35

    defaultConfig {
        applicationId = "tw.edu.pu.csim.s1114702.green"
        minSdk = 24
        targetSdk = 35
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

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {

    // 基本庫依賴
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)

    // GPS 相關服務
    implementation("com.google.android.gms:play-services-location:18.0.0")

    // Firebase 相關依賴（已優化）
    implementation("com.google.firebase:firebase-analytics-ktx") // Firebase Analytics
    implementation("com.google.firebase:firebase-firestore-ktx") // Firestore KTX 版本
    implementation("com.google.firebase:firebase-auth-ktx") // Firebase Authentication KTX 版本
    implementation("com.google.firebase:firebase-storage-ktx") // Firebase Storage KTX 版本
    implementation("com.google.firebase:firebase-messaging-ktx") // Firebase Messaging KTX 版本

    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    // Firebase 核心
    implementation("com.google.gms:google-services:4.3.15")
    implementation(libs.play.services.maps) // 最新版本的 google-services 插件

    // 測試相關
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
