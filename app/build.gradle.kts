import org.gradle.internal.impldep.jcifs.Config.load
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
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

        // Loading from local.properties file or directly from project properties
        val mapplsRestApiKey = project.findProperty("MAPPLS_REST_API_KEY") ?: ""
        val mapplsMapSdkKey = project.findProperty("MAPPLS_MAP_SDK_KEY") ?: ""
        val mapplsClientId = project.findProperty("MAPPLS_CLIENT_ID") ?: ""
        val mapplsClientSecret = project.findProperty("MAPPLS_CLIENT_SECRET") ?: ""

        // Define the custom BuildConfig fields


        buildConfigField("String", "MAPPLS_REST_API_KEY", "\"$mapplsRestApiKey\"")
        buildConfigField("String", "MAPPLS_MAP_SDK_KEY", "\"$mapplsMapSdkKey\"")
        buildConfigField("String", "MAPPLS_CLIENT_ID", "\"$mapplsClientId\"")
        buildConfigField("String", "MAPPLS_CLIENT_SECRET", "\"$mapplsClientSecret\"")



    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.filament.android)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Firebase BOM for version management
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    // Retrofit and Gson Converter
    implementation(libs.retrofit2.retrofit)
    implementation(libs.converter.gson)



    // Logging and other libraries
    implementation(libs.logging.interceptor)
    implementation(libs.androidsvg)
    implementation(libs.material)

    // Play services and location libraries
    implementation(libs.play.services.location)
    implementation(libs.places)

    // MapMyIndia SDK and Plugin
    implementation (libs.mappls.android.sdk.v800)
    implementation (libs.place.widget)
    implementation (libs.okhttp)

    // other
    implementation ("com.google.android.gms:play-services-auth-api-phone:18.0.1")
    implementation ("com.google.android.material:material:1.12.0")
    implementation ("com.facebook.shimmer:shimmer:0.5.0")


}



