import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val mapsSecrets = Properties().apply {
    val secretsFile = rootProject.file("secrets.properties")
    if (secretsFile.isFile) {
        secretsFile.inputStream().use { load(it) }
    }
}
val mapsApiKey = mapsSecrets.getProperty("MAPS_API_KEY", "").trim()
val geoapifyApiKey = mapsSecrets.getProperty("GEOAPIFY_API_KEY", "").trim()
require(geoapifyApiKey.isEmpty() || geoapifyApiKey.matches(Regex("[a-fA-F0-9]{32}"))) {
    "GEOAPIFY_API_KEY must be a 32-character hexadecimal key in secrets.properties"
}

android {
    namespace = "com.alfredorosas.vet2myfriend"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.alfredorosas.vet2myfriend"
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
        buildConfigField("boolean", "MAPS_CONFIGURED", mapsApiKey.isNotEmpty().toString())
        buildConfigField("String", "GEOAPIFY_API_KEY", "\"$geoapifyApiKey\"")
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
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

dependencies {
    implementation(project(":data:routing"))
    implementation(libs.google.play.services.location)
    implementation(project(":core:domain"))
    implementation(libs.androidx.core.splashscreen)
    implementation(project(":data:directory"))
    implementation(project(":feature:directory"))
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
