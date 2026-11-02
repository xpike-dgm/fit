import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val appVersionName = "0.2.0"
val appVersionParts = appVersionName.split(".").map(String::toInt)
require(appVersionParts.size == 3 && appVersionParts.all { it >= 0 } && appVersionParts[1] < 1_000 && appVersionParts[2] < 1_000) {
    "Use a semantic version with minor and patch values below 1000 (for example 0.2.0)."
}
val appVersionCodeValue = appVersionParts[0] * 1_000_000L + appVersionParts[1] * 1_000L + appVersionParts[2]
require(appVersionCodeValue in 1L..Int.MAX_VALUE.toLong()) { "The generated Android version code exceeds its supported range." }
val appVersionCode = appVersionCodeValue.toInt()
val releaseSigningProperties = Properties().apply {
    rootProject.file("fit-signing.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

android {
    namespace = "com.xpike.fit"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.xpike.fit"
        minSdk = 26
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName
    }

    buildFeatures {
        compose = true
    }

    signingConfigs {
        create("release") {
            releaseSigningProperties.getProperty("storeFile")?.let { storeFile = rootProject.file(it) }
            storePassword = releaseSigningProperties.getProperty("storePassword")
            keyAlias = releaseSigningProperties.getProperty("keyAlias")
            keyPassword = releaseSigningProperties.getProperty("keyPassword")
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
