import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

// Load secrets from local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.example.seekshakcom"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.seekshakcom"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Inject secrets into BuildConfig
        buildConfigField("String", "MAPPLS_REST_API_KEY", "\"${localProperties["MAPPLS_REST_API_KEY"] ?: ""}\"")
        buildConfigField("String", "MAPPLS_MAP_SDK_KEY", "\"${localProperties["MAPPLS_MAP_SDK_KEY"] ?: ""}\"")
        buildConfigField("String", "MAPPLS_CLIENT_ID", "\"${localProperties["MAPPLS_CLIENT_ID"] ?: ""}\"")
        buildConfigField("String", "MAPPLS_CLIENT_SECRET", "\"${localProperties["MAPPLS_CLIENT_SECRET"] ?: ""}\"")
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
        viewBinding = true
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            pickFirsts += "META-INF/INDEX.LIST"
        }
    }
}

dependencies {
    // AndroidX core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.play.services.identity)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.analytics)

    // Retrofit & Networking
    implementation(libs.retrofit2.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.okhttp)

    // MapMyIndia SDK
    implementation(libs.mappls.android.sdk.v800)
    implementation(libs.place.widget)

    // Google services
    implementation(libs.play.services.location)
    implementation(libs.places)
    implementation("com.google.android.gms:play-services-auth-api-phone:18.0.1")

    // UI & Effects
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation(libs.androidsvg)
    implementation(libs.filament.android)

    //image
    implementation (libs.ucrop)

}