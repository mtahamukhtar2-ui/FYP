import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

configure<com.android.build.api.dsl.ApplicationExtension> {
    namespace = "com.example.fyp_app"
    
    //noinspection NewerVersionAvailable
    compileSdk = 35 // Targeting Android 15 (Latest Stable)

    defaultConfig {
        applicationId = "com.example.fyp_app"
        minSdk = 26
        //noinspection OldTargetApi
        targetSdk = 35 // Targeting Android 15 (Latest Stable)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Load API Keys from local.properties or use hardcoded defaults if missing
        val localProperties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }
        
        // Defaults are provided so the app works even if local.properties is not shared
        val groqApiKey = localProperties.getProperty("GROQ_API_KEY") ?: "YOUR_API_KEY_HERE"
        val newsApiKey = localProperties.getProperty("NEWS_API_KEY") ?: "YOUR_API_KEY_HERE"
        val weatherApiKey = localProperties.getProperty("WEATHER_API_KEY") ?: "YOUR_API_KEY_HERE"
        val treatmentApiKey = localProperties.getProperty("TREATMENT_API_KEY") ?: "YOUR_API_KEY_HERE"

        buildConfigField("String", "GROQ_API_KEY", "\"$groqApiKey\"")
        buildConfigField("String", "NEWS_API_KEY", "\"$newsApiKey\"")
        buildConfigField("String", "WEATHER_API_KEY", "\"$weatherApiKey\"")
        buildConfigField("String", "TREATMENT_API_KEY", "\"$treatmentApiKey\"" )
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {

    // Default Android & Compose dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)

    // Activity KTX (provides by viewModels() and other utilities)
    implementation(libs.androidx.activity.ktx)

    // Coil for Image Loading
    implementation(libs.coil.compose)

    // Retrofit for Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // OkHttp (Managed by BOM for version consistency)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Firebase (BOM manages versions for all firebase libraries)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.mlkit.subject.segmentation)
    implementation(libs.google.ai.generativeai)
    implementation(libs.kotlinx.coroutines.play.services)

    // Google Services (for Sign-in)
    implementation(libs.google.services.auth)
    implementation(libs.play.services.location)

    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
